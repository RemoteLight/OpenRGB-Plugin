package de.lars.openrgbplugin.ui;

import de.lars.openrgbplugin.OpenRgbPlugin;
import de.lars.openrgbplugin.OutputHandler;
import de.lars.openrgbplugin.utils.UserInterfaceUtil;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.components.ListElement;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanel;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanelNavItem;
import de.lars.remotelightclient.utils.ui.MenuIconFont;
import de.lars.remotelightclient.utils.ui.UiUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DeviceGroupsPanel extends JPanel {

    private final ToolsPanel context;
    private final OpenRgbPlugin instance;
    private final JPanel panelSettings;
    private final JPanel panelDeviceList;

    public DeviceGroupsPanel(ToolsPanel context, OpenRgbPlugin instance) {
        this.context = context;
        this.instance = instance;
        setBackground(Style.panelBackground);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelSettings = new JPanel();
        panelSettings.setBackground(Style.panelDarkBackground);
        panelSettings.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.Y_AXIS));

        panelDeviceList = new JPanel();
        panelDeviceList.setBackground(Style.panelDarkBackground);
        panelDeviceList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelDeviceList.setLayout(new BoxLayout(panelDeviceList, BoxLayout.Y_AXIS));

        JPanel panelWrapper = new JPanel();
        panelWrapper.setBackground(getBackground());
        panelWrapper.setLayout(new BorderLayout());
        panelWrapper.add(panelSettings, BorderLayout.NORTH);
        panelWrapper.add(panelDeviceList, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(panelWrapper);
        scrollPane.setViewportBorder(null);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);
        add(scrollPane, BorderLayout.CENTER);

        setupSettingsPanel();
        updateDeviceEntryPanels();
    }

    public void setupSettingsPanel() {
        panelSettings.removeAll();
        JLabel lblServerIp = new JLabel("OpenRGB Server IP:");
        lblServerIp.setForeground(Style.textColor);
        JLabel lblServerPort = new JLabel("OpenRGB Port:");
        lblServerPort.setForeground(Style.textColor);

        JTextField fieldServerIp = new JTextField();
        fieldServerIp.setColumns(20);
        JFormattedTextField fieldServerPort = new JFormattedTextField(UserInterfaceUtil.getIntFieldFormatter());
        fieldServerPort.setColumns(5);

        // set stored values
        fieldServerIp.setText(instance.getOpenRgbIP());
        fieldServerPort.setValue(instance.getOpenRgbPort());

        panelSettings.add(UserInterfaceUtil.createSettingBgr(panelSettings.getBackground(), lblServerIp, fieldServerIp));
        panelSettings.add(UserInterfaceUtil.createSettingBgr(panelSettings.getBackground(), lblServerPort, fieldServerPort));

        boolean isConnected = instance.getOpenRGB().isConnected();
        JButton btnToggleClient = new JButton(isConnected ? "Disconnect" : "Connect");
        UiUtils.configureButton(btnToggleClient);
        btnToggleClient.addActionListener(e -> {
            if(OpenRgbPlugin.getInstance().getOpenRGB().isConnected())
                OpenRgbPlugin.getInstance().disconnectOpenRGB();
            else
                OpenRgbPlugin.getInstance().connectOpenRGB();
            // reload settings panel
            setupSettingsPanel();
        });

        JLabel lblConnectionState = new JLabel("Client not connected");
        lblConnectionState.setForeground(Style.textColor);
        panelSettings.add(UserInterfaceUtil.createSettingBgr(panelSettings.getBackground(), btnToggleClient, lblConnectionState));

        if(isConnected) {
            // disable if OpenRGB client is connected
            fieldServerIp.setEnabled(false);
            fieldServerPort.setEnabled(false);

            // set info label text
            lblConnectionState.setText(String.format("Connected to %s:%d",
                    instance.getOpenRGB().getClient().getHostname(),
                    instance.getOpenRGB().getClient().getPort()));
        }

        // add value change listener
        fieldServerIp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                valueChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                valueChanged();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                valueChanged();
            }
            void valueChanged() {
                instance.setOpenRgbConnection(fieldServerIp.getText(), (Integer) fieldServerPort.getValue());
            }
        });
        fieldServerPort.addPropertyChangeListener("value", e -> instance.setOpenRgbConnection(fieldServerIp.getText(), (Integer) fieldServerPort.getValue()));
        panelSettings.updateUI();
    }

    public void updateDeviceEntryPanels() {
        panelDeviceList.removeAll();
        for(final OutputHandler handler : instance.getHandlerSet()) {
            ListElement el = new ListElement();
            el.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    editHandler(handler);
                }
            });

            boolean activeDevice = instance.getEnabledHandler().contains(handler);
            if(activeDevice) {
                el.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Style.accent), el.getBorder()));
            }

            JLabel lblName = new JLabel(handler.getName());
            lblName.setForeground(Style.textColor);
            el.add(lblName);
            el.add(Box.createHorizontalStrut(10));

            if(activeDevice) {
                JLabel lblConnection = new JLabel(String.format("Enabled and attached to %s (%d LEDs)",
                        handler.getVirtualOutput().getId(),
                        handler.getTotalPixelNumber()));
                lblConnection.setForeground(Style.textColorDarker);
                el.add(lblConnection);
            }
            el.add(Box.createHorizontalGlue());

            JButton btnDelete = new JButton("Delete");
            configureBorderlessButton(btnDelete);
            btnDelete.addActionListener(e -> removeHandler(handler));
            el.add(btnDelete);

            JButton btnEdit = new JButton("Edit");
            configureBorderlessButton(btnEdit);
            btnEdit.addActionListener(e -> editHandler(handler));
            el.add(btnEdit);

            panelDeviceList.add(el);
            panelDeviceList.add(Box.createVerticalStrut(5));
        }

        // 'Add' button
        ListElement elAdd = new ListElement();
        elAdd.add(new JLabel(Style.getFontIcon(MenuIconFont.MenuIcon.ADD)));
        elAdd.add(Box.createHorizontalStrut(5));

        JLabel lblAdd = new JLabel("Add OpenRGB Device Group");
        lblAdd.setForeground(Style.textColor);
        elAdd.add(lblAdd);

        elAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editHandler(null);
            }
        });
        panelDeviceList.add(elAdd);
        panelDeviceList.updateUI();
    }

    /**
     * Open handler edit panel
     * @param handler  the playlist to edit (set to null to create a new playlist)
     */
    private void editHandler(OutputHandler handler) {
        SetupPanel setupPanel = new SetupPanel(context, handler, this);
        ToolsPanelNavItem navItem = new ToolsPanelNavItem("OpenRGB Configuration", setupPanel, setupPanel);

        context.navigateUp(navItem);
    }

    /**
     * Remove handler from set and update device list
     * @param handler   the handler to remove
     */
    private void removeHandler(OutputHandler handler) {
        instance.removeHandler(handler);
        updateDeviceEntryPanels();
    }

    public static void configureBorderlessButton(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFocusable(true);
        btn.setOpaque(true);
        btn.setBackground(null);
        btn.setForeground(Style.textColor);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

}
