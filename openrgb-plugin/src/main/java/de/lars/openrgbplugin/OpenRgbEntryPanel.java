package de.lars.openrgbplugin;

import de.lars.openrgbplugin.ui.DeviceGroupsPanel;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanel;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanelEntry;

import javax.swing.*;

public class OpenRgbEntryPanel extends ToolsPanelEntry {

    public OpenRgbEntryPanel() {
    }

    @Override
    public String getName() {
        return "OpenRGB Plugin";
    }

    @Override
    public JPanel getMenuPanel(ToolsPanel context) {
        return new DeviceGroupsPanel(context, OpenRgbPlugin.getInstance());
    }
}
