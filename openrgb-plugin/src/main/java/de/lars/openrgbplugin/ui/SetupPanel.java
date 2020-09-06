package de.lars.openrgbplugin.ui;

import de.lars.openrgbplugin.OpenRgbPlugin;
import de.lars.openrgbplugin.OutputHandler;
import de.lars.openrgbplugin.utils.UserInterfaceUtil;
import de.lars.openrgbplugin.utils.ValueHolder;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.components.ListElement;
import de.lars.remotelightclient.ui.panels.tools.ToolsNavListener;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanel;
import de.lars.remotelightclient.utils.ui.UiUtils;
import de.lars.remotelightcore.devices.Device;
import de.lars.remotelightcore.devices.virtual.VirtualOutput;
import de.lars.remotelightcore.notification.Notification;
import de.lars.remotelightcore.notification.NotificationType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SetupPanel extends JPanel implements ToolsNavListener {

    private final OpenRgbPlugin instance;
    private final ToolsPanel context;
    private final OutputHandler handler;
    private final List<Integer> listDevices;
    private final DeviceGroupsPanel devicesPanel;
    private final JPanel panelSettings;
    private JPanel panelDeviceList;

    public SetupPanel(ToolsPanel context, OutputHandler handler, DeviceGroupsPanel devicesPanel) {
        this.context = context;
        this.devicesPanel = devicesPanel;
        instance = OpenRgbPlugin.getInstance();
        this.handler = handler;

        if(handler != null)
            listDevices = handler.getDevices();
        else
            listDevices = new ArrayList<>();

        setBackground(Style.panelBackground);
        setLayout(new BorderLayout());

        panelSettings = new JPanel();
        panelSettings.setBackground(Style.panelDarkBackground);
        panelSettings.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(panelSettings);
        scrollPane.setViewportBorder(null);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);
        add(scrollPane, BorderLayout.CENTER);

        initSettingsPanel();
    }

    /**
     * Create setting components and add it to the panel
     */
    private void initSettingsPanel() {
        JLabel lblName = new JLabel("Name:");
        lblName.setForeground(Style.textColor);

        JTextField fieldName = new JTextField(20);
        panelSettings.add(UserInterfaceUtil.createSettingBgr(lblName, fieldName));

        JLabel lblVirtOutput = new JLabel("VirtualOutput:");
        lblVirtOutput.setForeground(Style.textColor);

        JComboBox<String> comboVirtOutputs = new JComboBox<>();
        for(Device device : instance.getInterface().getDeviceManager().getDevices()) {
            if(device instanceof VirtualOutput) {
                if(handler != null && instance.getHandlerByVirtualOutput(device.getId()) == handler) {
                    comboVirtOutputs.addItem(device.getId());
                    comboVirtOutputs.setSelectedIndex(comboVirtOutputs.getItemCount() - 1);
                } else if(!instance.isVirtualOutputUsed(device.getId())) {
                    comboVirtOutputs.addItem(device.getId());
                }
            }
        }
        panelSettings.add(UserInterfaceUtil.createSettingBgr(lblVirtOutput, comboVirtOutputs));

        JLabel lblDeviceId = new JLabel("OpenRGB Device ID (index):");
        lblDeviceId.setForeground(Style.textColor);

        JFormattedTextField fieldDeviceId = new JFormattedTextField(UserInterfaceUtil.getIntFieldFormatter());
        fieldDeviceId.setColumns(5);
        JButton btnAddDeviceId = new JButton("Add device");
        UiUtils.configureButton(btnAddDeviceId);
        btnAddDeviceId.addActionListener(e -> {
            if(fieldDeviceId.getValue() == null) return;
            int value = (int) fieldDeviceId.getValue();
            if(value < 0 || listDevices.contains(value)) {
                instance.getInterface().getNotificationManager().addNotification(
                        new Notification(NotificationType.ERROR, "OpenRGB Plugin", "Invalid ID or list contains already ID."));
                return;
            }
            listDevices.add(value);
            fieldDeviceId.setText("");
            updateDeviceListPanel();
        });
        panelSettings.add(UserInterfaceUtil.createSettingBgr(lblDeviceId, fieldDeviceId, btnAddDeviceId));

        panelDeviceList = new JPanel();
        panelDeviceList.setBackground(Style.panelDarkBackground);
        panelDeviceList.setLayout(new BoxLayout(panelDeviceList, BoxLayout.Y_AXIS));
        panelDeviceList.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSettings.add(Box.createVerticalStrut(10));
        panelSettings.add(panelDeviceList);
        updateDeviceListPanel();

        if(handler != null) {
            // set stored values
            fieldName.setText(handler.getName());
        }

        JButton btnAdd = new JButton(handler == null ? "Add OpenRGB Device" : "Save OpenRGB Device");
        UiUtils.configureButton(btnAdd);
        btnAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnAdd.setMinimumSize(new Dimension(100, 50));

        btnAdd.addActionListener(e -> {
            // create value holder
            ValueHolder holder = new ValueHolder(
                    fieldName.getText(),
                    (String) comboVirtOutputs.getSelectedItem(),
                    listDevices);

            if(validateInput(holder.getName(), holder.getOutputId())) {
                // get output
                VirtualOutput output = OpenRgbPlugin.getVirtualOutput(instance.getInterface().getDeviceManager(), holder.getOutputId());
                if (output == null) {
                    instance.getInterface().getNotificationManager().addNotification(
                            new Notification(NotificationType.ERROR, "OpenRGB Plugin", "Could not find virtual output for id " + holder.getOutputId()));
                    return;
                }

                if(handler != null) { // set values to output handler
                    handler.setName(holder.getName());
                    handler.setDevices(listDevices);
                    handler.setVirtualOutput(output);
                } else { // create new output handler
                    // create new handler
                    OutputHandler handler = instance.createHandler(holder, output);
                    // add handler to set
                    instance.addHandler(handler);
                }

                // go back
                context.navigateDown();
            }
        });

        panelSettings.add(Box.createVerticalGlue());
        panelSettings.add(btnAdd);
    }

    public boolean validateInput(String name, String outputId) {
        String errMsg = "Invalid input: ";
        boolean valid = true;

        if(name == null || name.trim().isEmpty()) {
            valid = false;
            errMsg += "name field is empty, ";
        }
        if(outputId == null || outputId.trim().isEmpty()) {
            valid = false;
            errMsg += "no VirtualOutput selected, ";
        }
        if(listDevices.isEmpty()) {
            valid = false;
            errMsg += "you must add at least one device.";
        }

        if(!valid) {
            // show error notification
            Notification noti = new Notification(NotificationType.ERROR, "OpenRGB Plugin", errMsg);
            instance.getInterface().getNotificationManager().addNotification(noti);
        }
        return valid;
    }

    private void updateDeviceListPanel() {
        panelDeviceList.removeAll();
        boolean isConnected = instance.getOpenRGB().isConnected();
        int controllerCount = isConnected ? instance.getOpenRGB().getControllerCount() : -1;

        for(int deviceId : listDevices) {
            ListElement el = new ListElement(40);
            panelDeviceList.add(el);

            JLabel lblDeviceId = new JLabel("Device #" + deviceId);
            lblDeviceId.setForeground(Style.textColor);
            el.add(lblDeviceId);

            if(isConnected) {
                el.add(Box.createHorizontalStrut(5));
                // show extra information
                if(deviceId >= controllerCount) {
                    // invalid device id
                    JLabel lblError = new JLabel("Invalid device ID. Available devices up to ID " + (controllerCount-1));
                    lblError.setForeground(Style.error);
                    el.add(lblError);
                } else {
                    de.lars.openrgbwrapper.Device device = instance.getOpenRGB().getControllerData(deviceId);
                    String text = String.format("%s (%s), %d LEDs",
                            device.name, device.type.name(), device.leds.length);

                    JLabel lblInfo = new JLabel(text);
                    lblInfo.setForeground(Style.textColorDarker);
                    lblInfo.setMaximumSize(new Dimension(800, lblInfo.getPreferredSize().height));
                    el.add(lblInfo);
                }
            }

            // remove device button
            JButton btnRemove = new JButton("X");
            UiUtils.configureButton(btnRemove);
            btnRemove.setToolTipText("Remove device from list");
            btnRemove.addActionListener(e -> {
                listDevices.remove((Integer) deviceId);
                updateDeviceListPanel();
            });
            el.add(Box.createHorizontalGlue());
            el.add(btnRemove);
        }
        panelDeviceList.updateUI();
    }

    @Override
    public void onBack() {
        // update devices panel
        devicesPanel.updateDeviceEntryPanels();
        // update settings panel
        devicesPanel.setupSettingsPanel();
    }

    @Override
    public void onShow() {}
}
