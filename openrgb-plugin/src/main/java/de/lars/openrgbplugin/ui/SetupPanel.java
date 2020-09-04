package de.lars.openrgbplugin.ui;

import de.lars.openrgbplugin.OpenRgbPlugin;
import de.lars.openrgbplugin.OutputHandler;
import de.lars.openrgbplugin.utils.ValueHolder;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.panels.tools.ToolsNavListener;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanel;
import de.lars.remotelightclient.utils.ui.UiUtils;
import de.lars.remotelightcore.devices.Device;
import de.lars.remotelightcore.devices.virtual.VirtualOutput;
import de.lars.remotelightcore.notification.Notification;
import de.lars.remotelightcore.notification.NotificationType;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;

public class SetupPanel extends JPanel implements ToolsNavListener {

    private final OpenRgbPlugin instance;
    private final ToolsPanel context;
    private OutputHandler handler;
    private final DevicesPanel devicesPanel;
    private final JPanel panelSettings;

    public SetupPanel(ToolsPanel context, OutputHandler handler, DevicesPanel devicesPanel) {
        this.context = context;
        this.devicesPanel = devicesPanel;
        instance = OpenRgbPlugin.getInstance();
        this.handler = handler;

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
        panelSettings.add(createSettingBgr(lblName, fieldName));

        JLabel lblVirtOutput = new JLabel("VirtualOutput:");
        lblVirtOutput.setForeground(Style.textColor);

        JComboBox<String> comboVirtOutputs = new JComboBox<>();
        for(Device device : instance.getInterface().getDeviceManager().getDevices()) {
            if(device instanceof VirtualOutput) {
                comboVirtOutputs.addItem(device.getId());
                if(handler != null && handler.getVirtualOutput().getId().equals(device.getId()))
                    comboVirtOutputs.setSelectedIndex(comboVirtOutputs.getItemCount() - 1);
            }
        }
        panelSettings.add(createSettingBgr(lblVirtOutput, comboVirtOutputs));

        JLabel lblServerIp = new JLabel("OpenRGB Server IP:");
        lblServerIp.setForeground(Style.textColor);
        JLabel lblServerPort = new JLabel("Port:");
        lblServerPort.setForeground(Style.textColor);

        JTextField fieldServerIp = new JTextField(20);
        JFormattedTextField fieldServerPort = new JFormattedTextField(getIntFieldFormatter());
        fieldServerPort.setColumns(5);
        panelSettings.add(createSettingBgr(lblServerIp, fieldServerIp, lblServerPort, fieldServerPort));

        JLabel lblDeviceId = new JLabel("OpenRGB Device ID:");
        lblDeviceId.setForeground(Style.textColor);

        JFormattedTextField fieldDeviceId = new JFormattedTextField(getIntFieldFormatter());
        fieldDeviceId.setColumns(10);
        panelSettings.add(createSettingBgr(lblDeviceId, fieldDeviceId));

        if(handler != null) {
            // set stored values
            fieldName.setText(handler.getName());
            fieldServerIp.setText(handler.getOpenRGB().getClient().getConnectionOptions().getHostString());
            fieldServerPort.setValue(handler.getOpenRGB().getClient().getConnectionOptions().getPort());
            fieldDeviceId.setValue(handler.getDeviceId());
        } else {
            // set default values
            fieldServerIp.setText("127.0.0.1");
            fieldServerPort.setValue(6742);
            fieldDeviceId.setValue(0);
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
                    fieldServerIp.getText(),
                    (int) fieldServerPort.getValue(),
                    (int) fieldDeviceId.getValue());

            if(validateInput(holder.getName(), holder.getOutputId(), holder.getOrgbIp(), holder.getOrgbPort(), holder.getDeviceId())) {
                // get output
                VirtualOutput output = OpenRgbPlugin.getVirtualOutput(instance.getInterface().getDeviceManager(), holder.getOutputId());
                if (output == null) {
                    instance.getInterface().getNotificationManager().addNotification(
                            new Notification(NotificationType.ERROR, "OpenRGB Plugin", "Could not find virtual output for id " + holder.getOutputId()));
                    return;
                }

                if(handler != null) { // set values to output handler
                    handler.setName(holder.getName());
                    handler.setDeviceId(holder.getDeviceId());
                    handler.setVirtualOutput(output);
                    // compare ip and port and set only connection values if they were changed
                    if(!OpenRgbPlugin.compareConnectionInfo(holder.getOrgbIp(), holder.getOrgbPort(),
                            handler.getOpenRGB().getClient().getConnectionOptions().getHostString(),
                            handler.getOpenRGB().getClient().getConnectionOptions().getPort())) {

                        if (!handler.getOpenRGB().getClient().setConnectionOptions(holder.getOrgbIp(), holder.getOrgbPort())) {
                            instance.getInterface().getNotificationManager().addNotification(
                                    new Notification(NotificationType.ERROR, "OpenRGB Plugin", "Cannot set server IP and Port while client is connected. Deactivate the output and try again."));
                        }
                    }
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

    private JPanel createSettingBgr(JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(Style.panelBackground);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        for(JComponent comp : components) {
            panel.add(comp);
            panel.add(Box.createHorizontalStrut(5));
        }
        return panel;
    }

    private NumberFormatter getIntFieldFormatter() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        format.setParseIntegerOnly(true);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);
        return formatter;
    }

    public boolean validateInput(String name, String outputId, String ip, int port, int deviceId) {
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
        if(ip == null || ip.trim().isEmpty()) {
            valid = false;
            errMsg += "ip field is empty, ";
        }
        if(port <= 0) {
            valid = false;
            errMsg += "port cannot be negative, ";
        }
        if(deviceId < 0) {
            valid = false;
            errMsg += "device id must be >= 0";
        }

        if(!valid) {
            // show error notification
            Notification noti = new Notification(NotificationType.ERROR, "OpenRGB Plugin", errMsg);
            instance.getInterface().getNotificationManager().addNotification(noti);
        }
        return valid;
    }

    @Override
    public void onBack() {
        // update devices panel
        devicesPanel.updateDeviceEntryPanels();
    }

    @Override
    public void onShow() {}
}
