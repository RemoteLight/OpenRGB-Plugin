package de.lars.openrgbplugin;

import de.lars.openrgbwrapper.Device;
import de.lars.openrgbwrapper.models.Color;
import de.lars.remotelightcore.devices.ConnectionState;
import de.lars.remotelightcore.devices.virtual.PixelStreamReceiver;
import de.lars.remotelightcore.devices.virtual.VirtualOutput;
import de.lars.remotelightcore.devices.virtual.VirtualOutputListener;
import de.lars.remotelightcore.notification.Notification;
import de.lars.remotelightcore.notification.NotificationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputHandler implements VirtualOutputListener, PixelStreamReceiver {

    private final OpenRgbPlugin plugin;
    private String name;
    private VirtualOutput virtualOutput;
    private List<Integer> devices;
    private final List<Device> cachedDeviceControllers;
    /** enable or disable pixel output */
    private boolean enabled = false;
    /** true if client lost connection and awaiting to reconnect */
    private boolean awaitingReconnect = false;

    /**
     * Create a new OutputHandler that forwards received data from a virtual output to
     * the OpenRGB SDK server.
     * @param output        virtual output to listen to
     * @param devices       list of OpenRGB devices that should be controlled
     */
    public OutputHandler(VirtualOutput output, List<Integer> devices) {
        this.plugin = OpenRgbPlugin.getInstance();
        this.virtualOutput = output;
        this.devices = devices;
        cachedDeviceControllers = new ArrayList<>();
    }

    /**
     * Register pixel output stream receiver and virtual output listener.
     */
    public void attachToOutput() {
        virtualOutput.addListener(this);
        virtualOutput.getOutputStream().addReceiver(this);
        // check if output is already activated
        if(virtualOutput.getConnectionState() == ConnectionState.CONNECTED) {
            // simulate onActivate()
            onActivate(virtualOutput);
        }
    }

    /**
     * Unregister pixel output stream receiver and virtual output listener.
     */
    public void detachFromOutput() {
        virtualOutput.removeListener(this);
        virtualOutput.getOutputStream().removeReceiver(this);
    }

    /**
     * Get the amount of OpenRGB devices/controllers
     * @return      the number of devices
     */
    public int getOpenRGBDeviceCount() {
        return plugin.getOpenRGB().getControllerCount();
    }

    /**
     * Updates local cached OpenRGB devices/controller data (only if client is connected)
     */
    public void updateOpenRgbDevices() {
        if(plugin.getOpenRGB().getClient().isConnected()) {
            // get controller count
            int controllerCount = getOpenRGBDeviceCount();
            if(controllerCount == -1)
                return; // error while reading from server
            // clear cached list
            cachedDeviceControllers.clear();
            // loop through all device ids
            for (int deviceId : devices) {
                // check if device is valid
                if (deviceId < controllerCount) {
                    // add to cache list
                    cachedDeviceControllers.add(plugin.getOpenRGB().getControllerData(deviceId));
                } else {
                    // print error message and remove device id from list
                    OpenRgbPlugin.print(String.format("(%s) Found invalid device ID: %d There are only %d OpenRGB devices (max device ID: %d). Removing device from list.",
                            getName(), deviceId, controllerCount, controllerCount - 1));
                    OpenRgbPlugin.getInstance().getInterface().getNotificationManager().addNotification(
                            new Notification(NotificationType.WARN, "OpenRGB Plugin (" + getName() + ")",
                                    String.format("Invalid device ID: %d. Max device ID is %d. Ignoring device.", deviceId, controllerCount - 1)));
                }
            }
        }
    }

    /**
     * Get the OpenRGB devices/controller data
     * @return      list of devices or empty list if client is not connected
     */
    public List<Device> getOpenRgbDevices() {
        if(cachedDeviceControllers.isEmpty())
            updateOpenRgbDevices();
        return cachedDeviceControllers;
    }

    /**
     * Get the total pixel number of all devices in the list
     * @return      total amount of pixels
     */
    public int getTotalPixelNumber() {
        int sum = 0;
        for(Device device : cachedDeviceControllers)
            sum += device.leds.length;
        return sum;
    }

    /**
     * Check if the OpenRGB pixel number is equal to the virtual output pixel number.
     * If it is not so, update pixel number of the virtual output.
     */
    public void updateOutputPixel() {
        if(getOpenRgbDevices().isEmpty())
            return;
        int pix = getTotalPixelNumber();
        if(virtualOutput.getPixels() != pix) {
            virtualOutput.setPixels(pix);
            OpenRgbPlugin.print("Updated pixel number for '" + virtualOutput.getId() + "'. New pixel number: " + pix);
        }
    }

    /**
     * Get the virtual output used by this handler.
     * @return      VirtualOutput instance
     */
    public VirtualOutput getVirtualOutput() {
        return virtualOutput;
    }

    @Override
    public void receivedPixelData(java.awt.Color[] colors) {
        // check if client was reconnected
        if(awaitingReconnect && plugin.getOpenRGB().isConnected()) {
            enabled = true;
            awaitingReconnect = false;
        }
        // check if output is enabled
        if(!enabled) return;
        // check if client is still connected
        if(!plugin.getOpenRGB().isConnected()) {
            OpenRgbPlugin.getInstance().getInterface().getNotificationManager().addNotification(
                new Notification(NotificationType.ERROR, "OpenRGB Plugin (" + name + ")", "Lost connection to OpenRGB SDK server."));
            enabled = false;
            awaitingReconnect = true;

            // trigger auto connect timer
            if(plugin.isAutoConnectEnabled()) {
                plugin.setAutoConnectEnabled(true);
            }
            return;
        }
        // check if there are still output devices
        if(getOpenRgbDevices().isEmpty()) {
            OpenRgbPlugin.getInstance().getInterface().getNotificationManager().addNotification(
                    new Notification(NotificationType.ERROR, "OpenRGB Plugin (" + name + ")", "Device group is empty. Please check plugin configuration and re-enable the output."));
            enabled = false;
            return;
        }
        // check if pixel array length is valid
        if(getTotalPixelNumber() != colors.length) {
            // virtual output pixel is not equal to OpenRGB led count
            // update cached controller data
            updateOpenRgbDevices();
            // update virtual output pixel number
            updateOutputPixel();
            return; // skip this received data
        }
        // send data for each device to OpenRGB
        int index = 0;
        for(Device device : getOpenRgbDevices()) {
            // split led data in to peaces for each device
            Color[] subArr = Arrays.copyOfRange(convertColors(colors), index, index + device.leds.length);
            // increment index
            index += device.leds.length;
            plugin.getOpenRGB().updateLeds(device.deviceId, subArr);
        }
    }

    /**
     * Convert {@link java.awt.Color} Array to {@link de.lars.openrgbwrapper.models.Color} Array.
     * @param awtColors     AWT Color array
     * @return              OpenRGB color model
     */
    protected Color[] convertColors(java.awt.Color[] awtColors) {
        return Arrays.stream(awtColors).map(c -> new Color(c.getRed(), c.getGreen(), c.getBlue())).toArray(Color[]::new);
    }

    @Override
    public void onActivate(VirtualOutput virtualOutput) {
        // connect orgb client
        if(!plugin.connectOpenRGB()) {
            awaitingReconnect = true;
            return;
        }
        // check for valid device id and update cached devices
        updateOpenRgbDevices();
        if(cachedDeviceControllers.isEmpty()) {
            OpenRgbPlugin.getInstance().getInterface().getNotificationManager().addNotification(
                    new Notification(NotificationType.ERROR, "OpenRGB Plugin (" + name + ")",
                            "Empty device group. Please check OpenRGB plugin configuration and re-activate the output."));
            // disable pixel output
            enabled = false;
            return;
        }
        enabled = true;
        updateOutputPixel();
    }

    @Override
    public void onDeactivate(VirtualOutput virtualOutput) {
        enabled = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVirtualOutput(VirtualOutput virtualOutput) {
        if(this.virtualOutput == virtualOutput)
            return;
        detachFromOutput();
        this.virtualOutput = virtualOutput;
        updateOutputPixel();
        attachToOutput();
    }

    public List<Integer> getDevices() {
        return devices;
    }

    /**
     * Set the list of device ids and update cached devices and pixel number
     * @param devices       new list of device ids
     */
    public void setDevices(List<Integer> devices) {
        if(this.devices == devices)
            return;
        this.devices = devices;
        updateOpenRgbDevices();
        updateOutputPixel();
    }

    /**
     * Add a single device id and update cached devices and pixel number
     * @param deviceId      device id to add
     */
    public void addDeviceId(int deviceId) {
        if(this.devices.contains(deviceId))
            return;
        this.devices.add(deviceId);
        updateOpenRgbDevices();
        updateOutputPixel();
    }

    public boolean isEnabled() {
        return enabled;
    }

}
