package de.lars.openrgbwrapper.models;

import de.lars.openrgbwrapper.types.DeviceType;

public class Device {

    public final DeviceType type;
    public final String name;
    public final String description;
    public final String version;
    public final String serial;
    public final String location;
    public final int activeMode;
    public final Mode[] modes;
    public final Zone[] zones;
    public final Led[] leds;
    public final Color[] colors;

    public Device(DeviceType type, String name, String description, String version, String serial, String location, int activeMode, Mode[] modes, Zone[] zones, Led[] leds, Color[] colors) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.version = version;
        this.serial = serial;
        this.location = location;
        this.activeMode = activeMode;
        this.modes = modes;
        this.zones = zones;
        this.leds = leds;
        this.colors = colors;
    }

    public static Device decode(byte[] data) {
        // TODO
        return null;
    }

}
