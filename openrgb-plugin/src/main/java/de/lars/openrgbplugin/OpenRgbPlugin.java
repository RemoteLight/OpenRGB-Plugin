package de.lars.openrgbplugin;

import de.lars.openrgbplugin.utils.StorageUtil;
import de.lars.openrgbplugin.utils.ValueHolder;
import de.lars.openrgbwrapper.OpenRGB;
import de.lars.remotelightcore.devices.DeviceManager;
import de.lars.remotelightcore.devices.virtual.VirtualOutput;
import de.lars.remotelightcore.out.Output;
import de.lars.remotelightcore.settings.SettingsManager;
import de.lars.remotelightcore.settings.types.SettingString;
import de.lars.remotelightplugins.Plugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OpenRgbPlugin extends Plugin {

    public static final String PREFIX = "[OpenRGB] ";
    /** setting key prefix */
    public static final String SETTING_PRE = "openrgb.";

    private Set<OutputHandler> setHandler;

    private static OpenRgbPlugin instance;

    public static OpenRgbPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        setHandler = new HashSet<>();
        // load settings
        loadSettings();

       print("Enabled OpenRGB Plugin.");
    }

    @Override
    public void onDisable() {
        storeSettings();
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    /**
     * Load saved output handlers from data file
     */
    protected void loadSettings() {
        SettingsManager sm = getInterface().getSettingsManager();
        DeviceManager dm = getInterface().getDeviceManager();

        // load value holders from settings
        Set<ValueHolder> setValues = StorageUtil.loadData(sm);

        for(ValueHolder holder : setValues) {
            VirtualOutput output = getVirtualOutput(dm, holder.getOutputId());
            if(output == null) {
                // virtual output does not exist
                print("Error: VirtualOutput with id '" + holder.getOutputId() + "' does not exists. OpenRGB output '" + holder.getName() + "' will be removed.");
                continue;
            }

            // create OpenRGB client
            OpenRGB orgb = new OpenRGB(holder.getOrgbIp(), holder.getOrgbPort(), holder.getName());
            // create output handler
            OutputHandler handler = new OutputHandler(output, orgb, holder.getDeviceId());
            handler.setName(holder.getName());
            // attach handler to output
            handler.attachToOutput();
            // add to handler set
            setHandler.add(handler);
        }

        print("Loaded " + setHandler.size() + " OpenRGB output devices.");
    }

    protected void storeSettings() {
        SettingsManager sm = getInterface().getSettingsManager();
        Set<ValueHolder> setValues = new HashSet<>();

        for(OutputHandler handler : setHandler) {
            // create value holder
            ValueHolder holder = new ValueHolder(
                    handler.getName(),
                    handler.getVirtualOutput().getId(),
                    handler.getOpenRGB().getClient().getConnectionOptions().getHostString(),
                    handler.getOpenRGB().getClient().getConnectionOptions().getPort(),
                    handler.getDeviceId());
            // add value holder to set
            setValues.add(holder);
        }

        // store value holders in settings manager
        StorageUtil.storeData(sm, setValues);
        print("Saved " + setValues.size() + " OpenRGB output devices.");
    }

    /**
     * Find virtual output by the specified id
     * @param id    id of the virtual output
     * @return      the virtual output or null
     */
    public static VirtualOutput getVirtualOutput(DeviceManager dm, String id) {
        // find output by id
        Output o = dm.getDevice(id);
        if(o instanceof VirtualOutput) {
            return (VirtualOutput) o;
        }
        return null;
    }

    public static void print(String text) {
        System.out.println(PREFIX + text);
    }

}
