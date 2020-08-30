package de.lars.openrgbwrapper.network.protocol;

public class Packet {

    public final PacketIdentifier identifier;
    public final int deviceId;
    public final int dataLength;

    public Packet(PacketIdentifier identifier, int deviceId, int dataLength) {
        this.identifier = identifier;
        this.deviceId = deviceId;
        this.dataLength = dataLength;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "identifier=" + identifier +
                ", deviceId=" + deviceId +
                ", dataLength=" + dataLength +
                '}';
    }
}
