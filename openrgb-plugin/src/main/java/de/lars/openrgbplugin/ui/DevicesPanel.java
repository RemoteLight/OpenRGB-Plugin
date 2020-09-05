package de.lars.openrgbplugin.ui;

import de.lars.openrgbplugin.OpenRgbPlugin;
import de.lars.openrgbplugin.OutputHandler;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.components.ListElement;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanel;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanelNavItem;
import de.lars.remotelightclient.utils.ui.MenuIconFont;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DevicesPanel extends JPanel {

    private final ToolsPanel context;
    private final OpenRgbPlugin instance;
    private final JPanel panelDeviceList;

    public DevicesPanel(ToolsPanel context, OpenRgbPlugin instance) {
        this.context = context;
        this.instance = instance;
        setBackground(Style.panelBackground);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelDeviceList = new JPanel();
        panelDeviceList.setBackground(Style.panelDarkBackground);
        panelDeviceList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelDeviceList.setLayout(new BoxLayout(panelDeviceList, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(panelDeviceList);
        scrollPane.setViewportBorder(null);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);
        add(scrollPane, BorderLayout.CENTER);

        updateDeviceEntryPanels();
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

            boolean activeDevice = handler.getOpenRGB().getClient().isConnected();
            if(activeDevice) {
                el.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Style.accent), el.getBorder()));
            }

            JLabel lblName = new JLabel(handler.getName());
            lblName.setForeground(Style.textColor);
            el.add(lblName);
            el.add(Box.createHorizontalStrut(10));

            if(activeDevice) {
                JLabel lblConnection = new JLabel(String.format("Connected to %s:%d Device ID: %d",
                        handler.getOpenRGB().getClient().getConnectionOptions().getHostString(),
                        handler.getOpenRGB().getClient().getConnectionOptions().getPort(),
                        handler.getDeviceId()));
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

        JLabel lblAdd = new JLabel("Add OpenRGB Device");
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
