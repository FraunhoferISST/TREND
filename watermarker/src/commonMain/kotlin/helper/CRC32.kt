/*
 * Copyright (c) 2024-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.helper

expect object CRC32 {
    /** Calculates the CRC32 checksum of [bytes] */
    fun checksum(bytes: ByteArray): UInt
}
