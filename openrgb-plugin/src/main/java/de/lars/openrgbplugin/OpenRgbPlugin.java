package de.lars.openrgbplugin;

import de.lars.remotelightplugins.Plugin;

public class OpenRgbPlugin extends Plugin {

    public static final String PREFIX = "[OpenRGB] ";

    private static OpenRgbPlugin instance;

    public static OpenRgbPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
       print("Enabled OpenRGB Plugin.");
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    public static void print(String text) {
        System.out.println(PREFIX + text);
    }

}
