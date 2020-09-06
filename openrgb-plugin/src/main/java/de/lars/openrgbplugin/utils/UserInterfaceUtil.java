package de.lars.openrgbplugin.utils;

import de.lars.remotelightclient.ui.Style;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;

public class UserInterfaceUtil {

    public static JPanel createSettingBgr(Color background, JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(background);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        for(JComponent comp : components) {
            panel.add(comp);
            panel.add(Box.createHorizontalStrut(5));
        }
        return panel;
    }

    public static JPanel createSettingBgr(JComponent... components) {
        return createSettingBgr(Style.panelBackground, components);
    }

    public static NumberFormatter getIntFieldFormatter() {
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

}
