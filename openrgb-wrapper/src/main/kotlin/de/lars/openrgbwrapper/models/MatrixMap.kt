package de.lars.openrgbwrapper.models

import de.lars.openrgbwrapper.utils.Pair
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MatrixMap(val height: Int, val width: Int, val matrix: Array<IntArray>) {

    companion object {

        /**
         * Decode and build a MatrixMap from a byte buffer
         * @return a Pair with the MatrixMap and the absolut offset after decoding the MatrixMap
         */
        fun decode(buffer: ByteArray, bufOffset: Int): Pair<MatrixMap, Int> {
            val buf: ByteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            var offset = bufOffset

            val height = buf.getInt(offset)
            offset += 4

            val width = buf.getInt(offset)
            offset += 4

            val matrix = Array(height) {
                IntArray(width) {
                    val value = buf.getInt(offset)
                    offset += 4
                    return@IntArray value
                }
            }
            return Pair(MatrixMap(height, width, matrix), offset)
        }

    }

}