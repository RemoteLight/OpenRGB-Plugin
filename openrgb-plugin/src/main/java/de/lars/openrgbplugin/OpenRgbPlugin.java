package de.lars.openrgbplugin;

import de.lars.openrgbplugin.utils.StorageUtil;
import de.lars.openrgbplugin.utils.ValueHolder;
import de.lars.openrgbwrapper.OpenRGB;
import de.lars.remotelightclient.ui.panels.tools.ToolsPanel;
import de.lars.remotelightcore.devices.DeviceManager;
import de.lars.remotelightcore.devices.virtual.VirtualOutput;
import de.lars.remotelightcore.notification.Notification;
import de.lars.remotelightcore.notification.NotificationType;
import de.lars.remotelightcore.out.Output;
import de.lars.remotelightcore.settings.SettingsManager;
import de.lars.remotelightcore.settings.types.SettingObject;
import de.lars.remotelightplugins.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenRgbPlugin extends Plugin {

    public static final String PREFIX = "[OpenRGB] ";
    /** setting key prefix */
    public static final String SETTING_PRE = "openrgb.";
    /** setting key ip */
    public static final String SETTING_IP = SETTING_PRE + "openrgb_ip";
    /** setting key port */
    public static final String SETTING_PORT = SETTING_PRE + "openrgb_port";

    /** a set of output handlers for each device group */
    private Set<OutputHandler> setHandler;
    /** OpenRGB client instance used to communicate with OpenRGB */
    private OpenRGB openRGB;

    /** OpenRGB server ip */
    private String openRgbIP;
    /** OpenRGB server port */
    private int openRgbPort;

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
        // initialize OpenRGB client
        openRGB = new OpenRGB(openRgbIP, openRgbPort, "RemoteLight OpenRGB-Plugin");
        // change default timeout to 3s
        openRGB.getClient().setConnectionTimeout(3000);

        // create entry panel
        OpenRgbEntryPanel entry = new OpenRgbEntryPanel();
        // register entry
        ToolsPanel.getEntryList().add(entry);

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

        // load ip and port
        this.openRgbIP = (String) sm.addSetting(new SettingObject(SETTING_IP, "OpenRGB Server IP", "127.0.0.1"), false).getValue();
        this.openRgbPort = (int) sm.addSetting(new SettingObject(SETTING_PORT, "OpenRGB Server Port", 6742), false).getValue();

        // load value holders from settings
        Set<ValueHolder> setValues = StorageUtil.loadData(sm);

        for(ValueHolder holder : setValues) {
            VirtualOutput output = getVirtualOutput(dm, holder.getOutputId());
            if(output == null) {
                // virtual output does not exist
                print("Error: VirtualOutput with id '" + holder.getOutputId() + "' does not exists. OpenRGB device group '" + holder.getName() + "' will be removed.");
                continue;
            }

            OutputHandler handler = createHandler(holder, output);
            // add to handler set
            setHandler.add(handler);
        }

        print("Loaded " + setHandler.size() + " OpenRGB output groups.");
    }

    protected void storeSettings() {
        SettingsManager sm = getInterface().getSettingsManager();

        // save ip and port
        sm.getSettingObject(SETTING_IP).setValue(openRgbIP);
        sm.getSettingObject(SETTING_PORT).setValue(openRgbPort);

        Set<ValueHolder> setValues = new HashSet<>();

        for(OutputHandler handler : setHandler) {
            // create value holder
            ValueHolder holder = new ValueHolder(
                    handler.getName(),
                    handler.getVirtualOutput().getId(),
                    handler.getDevices());
            // add value holder to set
            setValues.add(holder);
        }

        // store value holders in settings manager
        StorageUtil.storeData(sm, setValues);
        print("Saved " + setValues.size() + " OpenRGB output groups.");
    }

    public String getOpenRgbIP() {
        return openRgbIP;
    }

    public int getOpenRgbPort() {
        return openRgbPort;
    }

    public void setOpenRgbConnection(String openRgbIP, int openRgbPort) {
        this.openRgbIP = openRgbIP;
        this.openRgbPort = openRgbPort;
        if(!openRGB.getClient().setConnectionOptions(openRgbIP, openRgbPort)) {
            getInterface().getNotificationManager().addNotification(
                    new Notification(NotificationType.ERROR, "OpenRGB Plugin", "Cannot set server IP and Port while client is connected. Deactivate client and try again."));
        }
    }

    /**
     * Get the OpenRGB client
     * @return      OpenRGB client instance
     */
    public OpenRGB getOpenRGB() {
        return openRGB;
    }

    /**
     * Try to connect to the OpenRGB SDK server. Will do nothing when already connected.
     * @return      true if successfully connected,
     *              false if connection failed
     */
    public boolean connectOpenRGB() {
        if(openRGB.isConnected())
            return true;
        try {
            OpenRgbPlugin.print(String.format("Connecting to OpenRGB server: %s:%d",
                    openRGB.getClient().getConnectionOptions().getHostString(),
                    openRGB.getClient().getConnectionOptions().getPort()));
            return openRGB.connect();
        } catch (IOException e) {
            String ip = openRGB.getClient().getConnectionOptions().getHostString();
            int port = openRGB.getClient().getConnectionOptions().getPort();
            OpenRgbPlugin.print(String.format("Error while connecting to OpenRGB server %s:%d. Error: %S", ip, port, e.getMessage()));
            OpenRgbPlugin.getInstance().getInterface().getNotificationManager().addNotification(
                    new Notification(NotificationType.ERROR, "OpenRGB Plugin",
                            String.format("Could not connect to %s:%d. Please check OpenRGB plugin configuration and re-activate the output.", ip, port)));
            return false;
        }
    }

    /**
     * Disconnect OpenRGB client and set {@code enabled} to false.
     */
    public void disconnectOpenRGB() {
        try {
            openRGB.disconnect();
        } catch (IOException e) {
            OpenRgbPlugin.print("Error while disconnecting OpenRGB client: " + e.getMessage());
        }
    }

    /**
     * Disconnect and re-connect OpenRGB client.
     */
    public void reconnectOpenRGB() {
        OpenRgbPlugin.print("Reconnecting OpenRGB client...");
        disconnectOpenRGB();
        connectOpenRGB();
    }

    /**
     * Get all registered output handlers
     * @return      a set of all output handlers
     */
    public Set<OutputHandler> getHandlerSet() {
        return setHandler;
    }

    /**
     * Add new output handler to the set
     * @param handler   the handler to add
     */
    public void addHandler(OutputHandler handler) {
        setHandler.add(handler);
    }

    /**
     * Remove existing handler from the set
     * @param handler   the handler to remove
     */
    public void removeHandler(OutputHandler handler) {
        setHandler.remove(handler);
    }

    /**
     * Create a new output handler instance
     * @param holder    values holder
     * @param output    virtual output
     * @return          output handler created using the specified values
     */
    public OutputHandler createHandler(ValueHolder holder, VirtualOutput output) {
        // create output handler
        OutputHandler handler = new OutputHandler(output, holder.getDevices());
        handler.setName(holder.getName());
        // attach handler to output
        handler.attachToOutput();
        return handler;
    }

    /**
     * Check if virtual output id is already used by an handler
     * @param outputId      output id to check for
     * @return              true if some handler uses the output with
     *                      the specified id
     */
    public boolean isVirtualOutputUsed(String outputId) {
        return getHandlerByVirtualOutput(outputId) != null;
    }

    /**
     * Get the handler that uses the virtual output with the specified id
     * @param outputId      output id looking for
     * @return              handler that uses this output or null if output
     *                      is not used by any handler
     */
    public OutputHandler getHandlerByVirtualOutput(String outputId) {
        for(OutputHandler handler : setHandler) {
            System.out.println("Handler output: " + handler.getVirtualOutput().getId() + " output: " + outputId + " equals: " + handler.getVirtualOutput().getId().equals(outputId));
            if(handler.getVirtualOutput().getId().equals(outputId))
                return handler;
        }
        return null;
    }

    public List<OutputHandler> getEnabledHandler() {
        List<OutputHandler> listEnabled = new ArrayList<>();
        for(OutputHandler handler : setHandler) {
            if(handler.isEnabled())
                listEnabled.add(handler);
        }
        return listEnabled;
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

    /**
     * Compare ip address and port
     * @param ip1   ip #1
     * @param port1 port #1
     * @param ip2   ip #2
     * @param port2 port #2
     * @return      true if the address and port is equal
     */
    public static boolean compareConnectionInfo(String ip1, int port1, String ip2, int port2) {
        return ip1.equalsIgnoreCase(ip2) && port1 == port2;
    }

    public static void print(String text) {
        System.out.println(PREFIX + text);
    }

}
