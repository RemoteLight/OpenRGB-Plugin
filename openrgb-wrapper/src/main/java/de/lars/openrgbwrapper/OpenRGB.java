package de.lars.openrgbwrapper;

import de.lars.openrgbwrapper.network.Client;
import de.lars.openrgbwrapper.network.protocol.Packet;
import de.lars.openrgbwrapper.network.protocol.PacketIdentifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OpenRGB {

    /** the byte size of the header for a request */
    public static final int HEADER_SIZE = 16;
    /** client name */
    private final String clientName;
    /** client used to communicate with the OpenRGB server */
    private final Client client;

    /**
     * Create a new OpenRGB instance to communicate with the OpenRGB server
     * @param hostname      server hostname
     * @param port          server port
     * @param clientName    client name
     */
    public OpenRGB(String hostname, int port, String clientName) {
        this.clientName = clientName;
        // create new client instance
        client = new Client(hostname, port);
    }

    /**
     * Create a new OpenRGB instance with the default client name to communicate
     * with the OpenRGB server
     * @param hostname          server hostname
     * @param port              server port
     */
    public OpenRGB(String hostname, int port) {
        this(hostname, port, "Java Client");
    }

    /**
     * Attempt to connect to the OpenRGB server
     * @return          true if the socket could connect,
     *                  false otherwise
     */
    public boolean connect() {
        boolean connected = false;
        try {
            // connect client socket
            connected = client.connect();
            // send client name
            sendMessage(PacketIdentifier.SET_CLIENT_NAME, (clientName+'\0').getBytes(StandardCharsets.US_ASCII), 0);
        } catch (IOException e) {
            System.err.println("Could not connect to the server: ");
            e.printStackTrace();
        }
        return connected;
    }

    /**
     * Return the client used by this OpenRGB instance
     * <p>Please use the connect method of the {@link OpenRGB} class and
     * not the client connect method for sending the client name after connecting.</p>
     * @return          client used to send and receive data
     */
    public Client getClient() {
        return client;
    }

    /**
     * Request the number of controllers
     * @return              number of controllers as int
     */
    public int getControllerCount() {
        sendMessage(PacketIdentifier.REQUEST_CONTROLLER_COUNT, 0);
        byte[] data = readMessage();
        return data != null ? ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt() : 0;
    }

    /**
     * Request the controller data for a given device id
     * @param deviceId      device id (starting at 0)
     * @return              the controller data / device
     */
    public Device getControllerData(int deviceId) {
        sendMessage(PacketIdentifier.REQUEST_CONTROLLER_DATA, deviceId);
        byte[] data = readMessage();
        return data != null ? Device.decode(data) : null;
    }

    /**
     * Get the controller data for all controllers (0 to {@link #getControllerCount()})
     * @return              array with all controller data / devices
     */
    public Device[] getAllControllerData() {
        int n = getControllerCount();
        Device[] devices = new Device[n];
        for(int i = 0; i < n; i++)
            devices[i] = getControllerData(i);
        return devices;
    }

    /**
     * Send a message/packet to the server
     * @param identifier    packet identifier/type
     * @param dataBuffer    data to send as byte array, set to null if not needed
     * @param deviceId      target device id, set it to 0 if not needed for the request
     */
    public void sendMessage(PacketIdentifier identifier, byte[] dataBuffer, int deviceId) {
        if(!client.isConnected() || client.getOutStream() == null) return;
        // encode header
        byte[] header = encodeHeader(new Packet(identifier, deviceId, dataBuffer != null ? dataBuffer.length : 0));
        try {
            DataOutputStream out = client.getOutStream();
            // send header
            out.write(header);
            // send data
            if(dataBuffer != null) out.write(dataBuffer);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error while sending packet to the server:");
            e.printStackTrace();
        }
    }

    /**
     * Wrapper method for {@link #sendMessage(PacketIdentifier, byte[], int)}
     * @param identifier        packet identifier/type
     * @param deviceId          target device id, set to 0 if not needed for the request
     */
    public void sendMessage(PacketIdentifier identifier, int deviceId) {
        sendMessage(identifier, null, deviceId);
    }

    /**
     * Read from the client input stream, decode the data and return it
     * @return          received data as byte array or null if data could
     *                  not be read
     */
    public byte[] readMessage() {
        if(!client.isConnected() || client.getInStream() == null) return null;
        DataInputStream in = client.getInStream();
        try {
            byte[] header = new byte[HEADER_SIZE];
            // read header
            if(in.read(header) == -1) return null;
            // decode header
            Packet packet = decodeHeader(header);
            // check if data length is 0
            if(packet.dataLength <= 0) return null;

            byte[] data = new byte[packet.dataLength];
            // read data
            if(in.read(data) == -1) return null;
            return data;
        } catch (IOException e) {
            System.err.println("Error while reading from the server:");
            e.printStackTrace();
        }
        return null;
    }

    private byte[] encodeHeader(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        // put magic header bytes
        buffer.put("ORGB".getBytes(StandardCharsets.US_ASCII));
        // put device id, identifier id and the data length
        buffer.putInt(packet.deviceId);
        buffer.putInt(packet.identifier.id);
        buffer.putInt(packet.dataLength);
        // return the buffer as byte array
        return buffer.array();
        //out.write("ORGB".getBytes(StandardCharsets.US_ASCII));
        //out.writeByte(deviceId);
    }

    private Packet decodeHeader(byte[] header) {
        if(header.length != HEADER_SIZE)
            throw new IllegalArgumentException("Wrong header length! Expected a header length of " + HEADER_SIZE + " but is " + header.length + ".");

        ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        // read the magic header bytes
        String magic = new String(header, 0, 4, StandardCharsets.US_ASCII);
        if(!magic.equals("ORGB"))
            throw new IllegalArgumentException("Invalid header. Header does not contains magic byte 'ORGB'.");

        // create and return packet
        return new Packet(
                PacketIdentifier.fromId(buffer.getInt(4)),
                buffer.getInt(8),
                buffer.getInt(12));
    }

}
