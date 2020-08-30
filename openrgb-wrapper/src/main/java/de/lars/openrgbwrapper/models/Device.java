package de.lars.openrgbwrapper.models;

import de.lars.openrgbwrapper.types.DeviceType;
import de.lars.openrgbwrapper.utils.Pair;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static de.lars.openrgbwrapper.utils.BufferUtil.readString;

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
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int offset = 4;

        int typeIndex = buffer.getInt(offset);
        offset += 4;
        DeviceType[] deviceTypes = DeviceType.values();
        DeviceType type = DeviceType.Unknown;
        if(typeIndex < deviceTypes.length)
            type = deviceTypes[typeIndex];

        Pair<String, Integer> namePair = readString(buffer, offset);
        String name = namePair.first;
        offset += namePair.second;

        Pair<String, Integer> descPair = readString(buffer, offset);
        String description = descPair.first;
        offset += descPair.second;

        Pair<String, Integer> versionPair = readString(buffer, offset);
        String version = versionPair.first;
        offset += versionPair.second;

        Pair<String, Integer> serialPair = readString(buffer, offset);
        String serial = serialPair.first;
        offset += serialPair.second;

        Pair<String, Integer> locPair = readString(buffer, offset);
        String location = locPair.first;
        offset += locPair.second;

        short modeCount = buffer.getShort(offset);
        offset += 2;
        int activeMode = buffer.getInt(offset);
        offset += 4;
        Pair<Mode[], Integer> modesPair = Mode.Companion.decode(buffer.array(), offset, modeCount);
        Mode[] modes = modesPair.first;
        offset = modesPair.second; // in this case offset of Mode.decode is absolute

        short zoneCount = buffer.getShort(offset);
        offset += 2;
        Pair<Zone[], Integer> zonesPair = Zone.Companion.decode(buffer.array(), offset, zoneCount);
        Zone[] zones = zonesPair.first;
        offset = zonesPair.second; // returns absolute buffer offset

        short ledCount = buffer.getShort(offset);
        offset += 2;
        Pair<Led[], Integer> ledsPair = Led.Companion.decode(buffer.array(), offset, ledCount);
        Led[] leds = ledsPair.first;
        offset = ledsPair.second;

        short colorCount = buffer.getShort(offset);
        offset += 2;
        Pair<Color[], Integer> colorsPair = Color.Companion.decode(buffer.array(), offset, colorCount);
        Color[] colors = colorsPair.first;
        offset = colorsPair.second;

        return new Device(type, name, description, version, serial,
                location, activeMode, modes, zones, leds, colors);
    }

}
