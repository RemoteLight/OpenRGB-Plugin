package de.lars.openrgbwrapper;

import de.lars.openrgbwrapper.models.Color;
import de.lars.openrgbwrapper.network.Client;
import de.lars.openrgbwrapper.network.protocol.Packet;
import de.lars.openrgbwrapper.network.protocol.PacketIdentifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class OpenRGB {

    /** the byte size of the header for a request */
    public static final int HEADER_SIZE = 16;
    /** client name */
    private final String clientName;
    /** client used to communicate with the OpenRGB server */
    private final Client client;
    /** should connect, disconnect methods throw or catch exceptions */
    private boolean catchExceptionMode = false;

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
     * Set whether connect, disconnect methods should catch or throw exceptions.
     * @param catchExceptions       set to true if no exceptions should be thrown,
     *                              set to false if you want to handle exceptions yourself
     */
    public void setCatchExceptionMode(boolean catchExceptions) {
        this.catchExceptionMode = catchExceptions;
    }

    /**
     * Get whether exceptions are caught and handled by the client or thrown.
     * @return      true if exceptions are caught, false if exceptions are thrown
     */
    public boolean isCatchExceptionMode() {
        return catchExceptionMode;
    }

    /**
     * Attempt to connect to the OpenRGB server
     * @return          true if the socket could connect,
     *                  false otherwise
     */
    public boolean connect() throws IOException {
        boolean connected = false;
        try {
            // connect client socket
            connected = client.connect();
            // send client name
            sendMessage(PacketIdentifier.SET_CLIENT_NAME, (clientName+'\0').getBytes(StandardCharsets.US_ASCII), 0);
        } catch (IOException e) {
            if(!isCatchExceptionMode())
                throw e;
            System.err.println("Could not connect to the server: ");
            e.printStackTrace();
        }
        return connected;
    }

    /**
     * Wrapper method of {@link Client#disconnect()}
     * @return              true if the client was connected and could be closed,
     *                      false otherwise
     * @throws IOException  if an error occurs while closing the socket
     */
    public boolean disconnect() throws IOException {
        boolean disconnected = false;
        try {
            disconnected = client.disconnect();
        } catch (IOException e) {
            if(!isCatchExceptionMode())
                throw e;
            System.err.println("Error while disconnecting client: ");
            e.printStackTrace();
        }
        return disconnected;
    }

    /**
     * Wrapper method of {@link Client#isConnected()}
     * @return              true if socket is connected,
     *                      false otherwise
     */
    public boolean isConnected() {
        return client.isConnected();
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
     * Update the color for each led for the specified device id
     * @param deviceId          target device
     * @param colors            colors for the leds (length must match the led count)
     */
    public void updateLeds(int deviceId, Color[] colors) {
        if(colors == null)
            throw new IllegalArgumentException("Colors cannot be null!");
        if(colors.length >= Short.MAX_VALUE)
            throw new IllegalArgumentException("The length of the color array exceeds the maximum byte capacity (16-bit).");

        int size = 4 + 2 + (4 * colors.length);
        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(size);                    // 4 bytes for buffer length
        buffer.putShort((short) colors.length); // 2 bytes for color count

        for (Color color : colors) {            // 4 bytes for each color
            buffer.put(color.encode());
        }

        sendMessage(PacketIdentifier.RGBCONTROLLER_UPDATELEDS, buffer.array(), deviceId);
    }

    /**
     * Update the colors for the specified zone and device.
     * @param deviceId          target device
     * @param zoneId            target zone
     * @param colors            colors for the leds (length must match the led count of the zone)
     */
    public void updateZone(int deviceId, int zoneId, Color[] colors) {
        if(colors == null)
            throw new IllegalArgumentException("Colors cannot be null!");
        if(colors.length >= Short.MAX_VALUE)
            throw new IllegalArgumentException("The length of the color array exceeds the maximum byte capacity (16-bit).");

        int size = 4 + 4 + 2 + (4 * colors.length);
        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(size);                    // 4 bytes for buffer length
        buffer.putInt(zoneId);                  // 4 bytes for zone id
        buffer.putShort((short) colors.length); // 2 bytes for color count

        for (Color color : colors) {            // 4 bytes for each color
            buffer.put(color.encode());
        }

        sendMessage(PacketIdentifier.RGBCONTROLLER_UPDATEZONELEDS, buffer.array(), deviceId);
    }

    /**
     * Sets the mode of the specified to custom
     * @param deviceId              target device
     */
    public void setCustomMode(int deviceId) {
        sendMessage(PacketIdentifier.RGBCONTROLLER_SETCUSTOMMODE, deviceId);
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
            System.out.println("Disconnecting client due to exception.");
            try {
                disconnect();
            } catch (IOException ioException) {
                System.err.println("Error while disconnecting: ");
                ioException.printStackTrace();
            }
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
