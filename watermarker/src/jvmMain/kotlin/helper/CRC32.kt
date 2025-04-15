/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.helper

import java.util.zip.CRC32

actual object CRC32 {
    actual fun checksum(bytes: List<Byte>): UInt {
        val crc = CRC32()
        crc.update(bytes.toByteArray())
        return crc.value.toUInt()
    }
}
