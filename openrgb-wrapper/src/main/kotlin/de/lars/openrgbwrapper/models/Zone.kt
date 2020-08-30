package de.lars.openrgbwrapper.models

import java.nio.ByteBuffer
import java.nio.ByteOrder

import de.lars.openrgbwrapper.utils.BufferUtil.readString
import de.lars.openrgbwrapper.utils.Pair

data class Zone(val name: String, val zoneType: Int, val ledsMin: Int, val ledsMax: Int,  val ledCount: Int, val matrixMap: MatrixMap?) {

    companion object {

        /**
         * Decode zones from a byte buffer
         * @return a Pair with the zone array and the absolute buffer offset
         */
        fun decode(buffer: ByteArray, bufOffset: Int, zoneCount: Int): Pair<Array<Zone>, Int> {
            val buf: ByteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            var offset: Int = bufOffset

            val zones: Array<Zone> = Array(zoneCount) {
                val namePair: Pair<String, Int> = readString(buf, offset)
                val name = namePair.first
                offset = namePair.second

                val type = buf.getInt(offset)
                offset += 4

                val ledsMin = buf.getInt(offset)
                offset += 4

                val ledsMax = buf.getInt(offset)
                offset += 4

                val ledCount = buf.getInt(offset)
                offset += 4

                val matrixLength: Int = buf.getShort(offset).toInt()
                offset += 2
                val matrixMap: MatrixMap? = when(matrixLength) {
                    0 -> null
                    else -> {
                        val matrixPair: Pair<MatrixMap, Int> = MatrixMap.decode(buf.array(), offset)
                        offset = matrixPair.second
                        matrixPair.first
                    }
                }

                Zone(name, type, ledsMin, ledsMax, ledCount, matrixMap)
            }
            return Pair(zones, offset)
        }

    }

}