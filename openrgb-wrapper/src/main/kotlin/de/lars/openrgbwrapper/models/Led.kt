package de.lars.openrgbwrapper.models

import de.lars.openrgbwrapper.utils.Pair
import java.nio.ByteBuffer
import java.nio.ByteOrder

import de.lars.openrgbwrapper.utils.BufferUtil.readString

data class Led(val name: String, val color: Color) {

    companion object {

        /**
         * Decode an array of leds from a byte buffer
         * @return a Pair with the led array and the absolute buffer offset
         */
        fun decode(buffer: ByteArray, bufOffset: Int, ledCount: Short): Pair<Array<Led>, Int> {
            val buf: ByteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            var offset: Int = bufOffset

            val leds: Array<Led> = Array(ledCount.toInt()) {
                val namePair: Pair<String, Int> = readString(buf, offset)
                val name = namePair.first
                offset += namePair.second

                val color = Color.decode(buf.array().copyOfRange(offset, offset + 4))
                offset += 4

                Led(name, color)
            }
            return Pair(leds, offset)
        }

    }

}