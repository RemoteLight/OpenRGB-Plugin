package de.lars.openrgbplugin;

import de.lars.openrgbwrapper.Device;
import de.lars.openrgbwrapper.OpenRGB;
import de.lars.openrgbwrapper.models.Color;
import de.lars.remotelightcore.devices.virtual.PixelStreamReceiver;
import de.lars.remotelightcore.devices.virtual.VirtualOutput;
import de.lars.remotelightcore.devices.virtual.VirtualOutputListener;

import java.io.IOException;
import java.util.Arrays;

public class OutputHandler implements VirtualOutputListener, PixelStreamReceiver {

    private String name;
    private final OpenRGB openRGB;
    private VirtualOutput virtualOutput;
    private int deviceId;
    private Device cachedDevice;

    /**
     * Create a new OutputHandler that forwards received data from a virtual output to
     * the OpenRGB SDK server.
     * @param output        virtual output to listen to
     * @param openRGB       OpenRGB client instance
     * @param deviceId      OpenRGB device id
     */
    public OutputHandler(VirtualOutput output, OpenRGB openRGB, int deviceId) {
        this.virtualOutput = output;
        this.openRGB = openRGB;
        this.deviceId = deviceId;
    }

    /**
     * Register pixel output stream receiver and virtual output listener.
     */
    public void attachToOutput() {
        virtualOutput.addListener(this);
        virtualOutput.getOutputStream().addReceiver(this);
    }

    /**
     * Unregister pixel output stream receiver and virtual output listener.
     */
    public void detachFromOutput() {
        virtualOutput.removeListener(this);
        virtualOutput.getOutputStream().removeReceiver(this);
    }

    /**
     * Updates local cached OpenRGB device/controller data
     */
    public void updateOpenRgbDevice() {
        cachedDevice = openRGB.getControllerData(deviceId);
    }

    /**
     * Get the OpenRGB device/controller data
     * @return      Device instance or null if client is not connected
     */
    public Device getOpenRgbDevice() {
        if(cachedDevice == null)
            updateOpenRgbDevice();
        return cachedDevice;
    }

    /**
     * Check if the OpenRGB pixel number is equal to the virtual output pixel number.
     * If it is not so, update pixel number of the virtual output.
     */
    public void updateOutputPixel() {
        if(getOpenRgbDevice() == null)
            return;
        int pix = getOpenRgbDevice().leds.length;
        if(virtualOutput.getPixels() != pix) {
            virtualOutput.setPixels(pix);
            OpenRgbPlugin.print("Updated pixel number for '" + virtualOutput.getId() + "'. New pixel number: " + pix);
        }
    }

    /**
     * Get the OpenRGB client used by this handler.
     * @return      OpenRGB client instance
     */
    public OpenRGB getOpenRGB() {
        return openRGB;
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
        if(getOpenRgbDevice() != null && getOpenRgbDevice().leds.length != colors.length) {
            // virtual output pixel is not equal to OpenRGB led count
            updateOpenRgbDevice();
            return; // skip this received data
        }
        // send to OpenRGB
        openRGB.updateLeds(deviceId, convertColors(colors));
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
        OpenRgbPlugin.print(String.format("(%s) Connecting to OpenRGB server: %s:%d",
                virtualOutput.getId(),
                openRGB.getClient().getConnectionOptions().getHostString(),
                openRGB.getClient().getConnectionOptions().getPort()));
        // connect orgb client
        openRGB.connect();
        updateOutputPixel();
    }

    @Override
    public void onDeactivate(VirtualOutput virtualOutput) {
        OpenRgbPlugin.print(String.format("(%s) Disconnecting from OpenRGB server: %s:%d",
                virtualOutput.getId(),
                openRGB.getClient().getConnectionOptions().getHostString(),
                openRGB.getClient().getConnectionOptions().getPort()));
        // disconnect orgb client
        try {
            openRGB.getClient().disconnect();
        } catch (IOException e) {
            OpenRgbPlugin.print("Error while disconnecting OpenRGB client.");
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVirtualOutput(VirtualOutput virtualOutput) {
        detachFromOutput();
        this.virtualOutput = virtualOutput;
        updateOutputPixel();
        attachToOutput();
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
        updateOpenRgbDevice();
        updateOutputPixel();
    }
}
