package de.lars.openrgbwrapper.models

data class Color(var red: Byte, var green: Byte, var blue: Byte) {

    /**
     * Encode color to a byte array with a length of 4
     * @return      byte array (red, green, blue, 0)
     */
    fun encode() = byteArrayOf(red, green, blue)

    companion object {
        /**
         * Decode byte buffer to a specified amount of colors
         * @param buffer        byte buffer to read from
         * @param offset        the start index (inclusive)
         * @param colorCount    the number of colors to decode from the buffer
         * @return              array of colors with length of colorCount
         */
        fun decode(buffer: Array<Byte>, offset: Int, colorCount: Int): Array<Color> {
            require(buffer.size >= offset + 4 * colorCount) {
                ("Could not decode color array from byte buffer. Expected buffer size of ${(offset + 4 * colorCount)} but is  ${buffer.size}.")
            }

            return Array(colorCount) { i ->
                val pos: Int = offset + i * 4
                decode(buffer.copyOfRange(pos, pos + 4))
            }
        }

        /**
         * Decode a single color from byte array
         * @param data      byte array of length 4
         * @return          color instance decoded from the array
         */
        fun decode(data: Array<Byte>): Color {
            require(data.size > 3) {"Could not decode color from byte array. Expected minimum length of 3 but is ${data.size}."}
            return Color(data[0], data[1], data[2])
        }
    }

}