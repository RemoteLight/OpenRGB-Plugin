package de.lars.openrgbwrapper.models

import com.sun.org.apache.xpath.internal.operations.Mod
import java.nio.ByteBuffer
import java.nio.ByteOrder

import de.lars.openrgbwrapper.utils.BufferUtil.readString
import de.lars.openrgbwrapper.utils.Pair


data class Mode(val name: String, val value: Int, val flags: Int, val speedMin: Int, val speedMax: Int,
                val colorMin: Int, val colorMax: Int, val speed: Int, val direction: Int, val colorMode: Int, val colors: Array<Color>) {

    companion object {

        /**
         * Decode a byte buffer to an array of modes
         */
        fun decode(buffer: ByteArray, bufOffset: Int, numModes: Int): Pair<Array<Mode>, Int> {
            val buf: ByteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            var offset: Int = bufOffset

            val modes: Array<Mode> = Array(numModes) {
                val namePair: Pair<String, Int> = readString(buf, offset)
                val name = namePair.first
                offset += namePair.second

                val value = buf.getInt(offset)
                offset += 4

                val flags = buf.getInt(offset)
                offset += 4

                val speedMin = buf.getInt(offset)
                offset += 4

                val speedMax = buf.getInt(offset)
                offset += 4

                val colorMin = buf.getInt(offset)
                offset += 4

                val colorMax = buf.getInt(offset)
                offset += 4

                val speed = buf.getInt(offset)
                offset += 4

                val direction = buf.getInt(offset)
                offset += 4

                val colorMode = buf.getInt(offset)
                offset += 4

                val colorCount: Int = buf.getShort(offset).toInt()
                offset += 2
                val colors = Color.decode(buf.array(), offset, colorCount)
                offset += colorCount - 1

                Mode(name, value, flags, speedMin, speedMax, colorMin, colorMax, speed, direction, colorMode, colors)
            }
            return Pair(modes, offset)
        }
    }

}
