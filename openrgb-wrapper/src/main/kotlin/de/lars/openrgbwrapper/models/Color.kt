package de.lars.openrgbwrapper.models

import de.lars.openrgbwrapper.utils.Pair
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Color constructor(var red: Int, var green: Int, var blue: Int) {

    /**
     * Encode color to a byte array with a length of 4
     * @return      byte array (red, green, blue, 0)
     */
    @ExperimentalUnsignedTypes
    fun encode() = byteArrayOf(red.toByte(), green.toByte(), blue.toByte(), 0)

    companion object {
        /**
         * Decode byte buffer to a specified amount of colors
         * @param buffer        byte buffer to read from
         * @param bufOffset     the start index (inclusive)
         * @param colorCount    the number of colors to decode from the buffer
         * @return              Pair containing an array of colors with length of colorCount
         *                      and the absolute buffer offset
         */
        @ExperimentalUnsignedTypes
        fun decode(buffer: ByteArray, bufOffset: Int, colorCount: Int): Pair<Array<Color>, Int> {
            val buf: ByteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            var offset: Int = bufOffset

            val colors: Array<Color> = Array(colorCount) {
                val color = decode(buf.array().copyOfRange(offset, offset + 4))
                offset += 4
                return@Array color
            }
            return Pair(colors, offset)
        }

        /**
         * Decode a single color from byte array
         * @param data      byte array of length 4
         * @return          color instance decoded from the array
         */
        @ExperimentalUnsignedTypes
        fun decode(data: ByteArray): Color {
            require(data.size > 3) {"Could not decode color from byte array. Expected minimum length of 3 but is ${data.size}."}
            return Color(data[0].toUByte().toInt(), data[1].toUByte().toInt(), data[2].toUByte().toInt())
        }
    }

}