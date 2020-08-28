package de.lars.openrgbwrapper.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Color {

    private byte red;
    private byte green;
    private byte blue;

    public Color(byte red, byte green, byte blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public byte getRed() {
        return red;
    }

    public void setRed(byte red) {
        this.red = red;
    }

    public byte getGreen() {
        return green;
    }

    public void setGreen(byte green) {
        this.green = green;
    }

    public byte getBlue() {
        return blue;
    }

    public void setBlue(byte blue) {
        this.blue = blue;
    }

    /**
     * Encode color to a byte array with a length of 4
     * @return      byte array (red, green, blue, 0)
     */
    public byte[] encode() {
        return new byte[] {red, green, blue, 0};
    }

    /**
     * Decode byte buffer to a specified amount of colors
     * @param buffer        byte buffer to read from
     * @param offset        the start index (inclusive)
     * @param colorCount    the number of colors to decode from the buffer
     * @return              array of colors with length of colorCount
     */
    public static Color[] decode(byte[] buffer, int offset, int colorCount) {
        if(buffer.length < offset + 4*colorCount)
            throw new IllegalArgumentException("Could not decode color array from byte buffer."
                + "Expected buffer size of " + (offset + 4*colorCount) + " but is " + buffer.length);

        Color[] colors = new Color[colorCount];
        for(int i = 0; i < colorCount; i++) {
            int pos = offset + i*4;
            colors[i] = decode(Arrays.copyOfRange(buffer, pos, pos+4));
        }
        return colors;
    }

    /**
     * Decode a single color from byte array
     * @param data      byte array of length 4
     * @return          color instance decoded from the array
     */
    public static Color decode(byte[] data) {
        if(data.length < 3) throw new IllegalArgumentException("Could not decode color from byte array. Expected minimum length of 3 but is " + data.length + ".");
        return new Color(data[0], data[1], data[3]);
    }

}
