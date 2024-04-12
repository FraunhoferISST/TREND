/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.helper

actual object CRC32 {
    private val table =
        (0u..255u).map { n ->
            var c = n
            repeat(8) {
                c =
                    if (c and 1u != 0u) {
                        0xedb88320u xor (c shr 1)
                    } else {
                        c shr 1
                    }
            }
            c
        }.toList()

    actual fun checksum(bytes: List<Byte>): UInt {
        var c = 0xffffffffu
        for (byte in bytes) {
            val index = (c xor byte.toUInt() and 0xffu).toInt()
            c = table[index] xor (c shr 8)
        }
        return c xor 0xffffffffu
    }
}
