package de.lars.openrgbwrapper.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BufferUtil {

    /**
     * Helper method for reading a string from a ByteBuffer. There should be 2 leading bytes
     * which describes the length of the string. The method will automatically increment the offset.
     * @param buffer        ByteBuffer to read from
     * @param offset        current offset
     * @return              parsed string value and new offset as Pair
     */
    public static Pair<String, Integer> readString(ByteBuffer buffer, int offset) {
        short length = buffer.getShort(offset); // read the string byte length
        offset += 2;
        String text = new String(Arrays.copyOfRange(buffer.array(), offset, offset + length - 1), StandardCharsets.US_ASCII);
        offset += length;
        return new Pair<>(text, offset);
    }

}
