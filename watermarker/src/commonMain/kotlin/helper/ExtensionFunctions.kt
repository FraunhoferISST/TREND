/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

package de.fraunhofer.isst.innamark.watermarker.helper

/**
 * Returns an UInt created from 4 bytes in little endian order
 * If less than 4 bytes are supplied they get filled with zeros from MSB to LSB
 */
fun UInt.Companion.fromBytesLittleEndian(bytes: List<Byte>): UInt {
    require(bytes.size <= 4)
    var result = 0u
    for ((index, byte) in bytes.withIndex()) {
        result = result or ((byte.toUInt() and 255u) shl (index * 8))
    }
    return result
}

/**
 * Returns an UShort created from 2 bytes in little endian order
 * If less than 2 bytes are supplied they get filled with zeros from MSB to LSB
 */
fun UShort.Companion.fromBytesLittleEndian(bytes: List<Byte>): UShort {
    require(bytes.size <= 2)
    var result = 0u
    for ((index, byte) in bytes.withIndex()) {
        result = result or ((byte.toUInt() and 255u) shl (index * 8))
    }
    return result.toUShort()
}

/** Converts an UInt into a list of 4 bytes in little endian order */
fun UInt.toBytesLittleEndian(): List<Byte> =
    listOf(
        this.toByte(),
        this.shr(8).toByte(),
        this.shr(16).toByte(),
        this.shr(24).toByte(),
    )

/** Converts an UShort into a list of 2 bytes in little endian order */
fun UShort.toBytesLittleEndian(): List<Byte> =
    listOf(
        this.toByte(),
        this.toUInt().shr(8).toByte(),
    )

/** Coverts a Byte into an UByte and returns it as Int */
fun Byte.toIntUnsigned(): Int = this.toInt() and 0xff

/** Returns the unicode representation of the character */
fun Char.toUnicodeRepresentation(): String =
    "\\u" + this.code.toString(16).uppercase().padStart(4, '0')

/**
 * Represents a List of bytes in hex
 * E.g.: [0xde, 0xad, 0xbe, 0xef]
 */
fun List<Byte>.toHexString(): String =
    "[" +
        joinToString(separator = ", ") { eachByte ->
            eachByte.toIntUnsigned().toString(16).uppercase().padStart(2, '0')
        } +
        "]"
