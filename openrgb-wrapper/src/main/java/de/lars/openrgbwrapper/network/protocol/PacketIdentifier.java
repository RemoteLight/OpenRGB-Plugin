package de.lars.openrgbwrapper.network.protocol;

import java.util.Arrays;

/**
 * OpenRGB Network Packet IDs
 * <p>Reference: https://gitlab.com/CalcProgrammer1/OpenRGB/-/blob/master/NetworkProtocol.h </p>
 */
public enum PacketIdentifier {

    /*-------------------
     * Network requests
     *-------------------*/
    REQUEST_CONTROLLER_COUNT(0),
    REQUEST_CONTROLLER_DATA(1),
    SET_CLIENT_NAME(50),

    /*-------------------
     * RGB controller functions
     *-------------------*/
    RGBCONTROLLER_RESIZEZONE(1000),

    RGBCONTROLLER_UPDATELEDS(1050),
    RGBCONTROLLER_UPDATEZONELEDS(1051),
    RGBCONTROLLER_UPDATESINGLELED(1052),

    RGBCONTROLLER_SETCUSTOMMODE(1100),
    RGBCONTROLLER_UPDATEMODE(1101);

    /** packet id */
    public final int id;

    private PacketIdentifier(int id) {
        this.id = id;
    }

    public static PacketIdentifier fromId(int id) {
        return Arrays.stream(PacketIdentifier.values()).filter(x -> x.id == id).findFirst().orElse(null);
    }

}
