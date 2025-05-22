/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.helper

import de.fraunhofer.isst.innamark.watermarker.helper.fromBytesLittleEndian
import de.fraunhofer.isst.innamark.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.innamark.watermarker.helper.toHexString
import de.fraunhofer.isst.innamark.watermarker.helper.toIntUnsigned
import de.fraunhofer.isst.innamark.watermarker.helper.toUnicodeRepresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExtensionFunctionsTest {
    @Test
    fun uIntCompanionFromBytesLittleEndian_4Bytes_success() {
        // Arrange
        val bytes = listOf<Byte>(0x12, 0x34, 0x56, 0x78)
        val expected = 0x78563412u

        // Act
        val result = UInt.fromBytesLittleEndian(bytes)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uIntCompanionFromBytesLittleEndian_2Bytes_success() {
        // Arrange
        val bytes = listOf<Byte>(0x12, 0x34)
        val expected = 0x00003412u

        // Act
        val result = UInt.fromBytesLittleEndian(bytes)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uIntCompanionFromBytesLittleEndian_0Bytes_success() {
        // Arrange
        val bytes = listOf<Byte>()
        val expected = 0x0u

        // Act
        val result = UInt.fromBytesLittleEndian(bytes)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uIntCompanionFromBytesLittleEndian_5Bytes_AssertionException() {
        // Arrange
        val bytes = listOf<Byte>(0x12, 0x34, 0x56, 0x78, 0x9A.toByte())

        // Act + Assert
        assertFailsWith<IllegalArgumentException> {
            UInt.fromBytesLittleEndian(bytes)
        }
    }

    @Test
    fun uShortCompanionFromBytesLittleEndian_2Bytes_success() {
        // Arrange
        val bytes = listOf<Byte>(0x12, 0x34)
        val expected = 0x3412u.toUShort()

        // Act
        val result = UShort.fromBytesLittleEndian(bytes)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uShortCompanionFromBytesLittleEndian_1Byte_success() {
        // Arrange
        val bytes = listOf<Byte>(0x12)
        val expected = 0x0012u.toUShort()

        // Act
        val result = UShort.fromBytesLittleEndian(bytes)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uShortCompanionFromBytesLittleEndian_0Bytes_success() {
        // Arrange
        val bytes = listOf<Byte>()
        val expected = 0x0u.toUShort()

        // Act
        val result = UShort.fromBytesLittleEndian(bytes)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uShortCompanionFromBytesLittleEndian_3Bytes_AssertionException() {
        // Arrange
        val bytes = listOf<Byte>(0x12, 0x34, 0x56)

        // Act + Assert
        assertFailsWith<IllegalArgumentException> {
            UShort.fromBytesLittleEndian(bytes)
        }
    }

    @Test
    fun uIntToBytesLittleEndian_largeNumber_success() {
        // Arrange
        val uInt = 0x12345678u
        val expected = listOf<Byte>(0x78, 0x56, 0x34, 0x12)

        // Act
        val result = uInt.toBytesLittleEndian()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uIntToBytesLittleEndian_smallNumber_success() {
        // Arrange
        val uInt = 0x00000012u
        val expected = listOf<Byte>(0x12, 0x00, 0x00, 0x00)

        // Act
        val result = uInt.toBytesLittleEndian()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uShortToBytesLittleEndian_largeNumber_success() {
        // Arrange
        val uShort = 0x1234u.toUShort()
        val expected = listOf<Byte>(0x34, 0x12)

        // Act
        val result = uShort.toBytesLittleEndian()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun uShortToBytesLittleEndian_smallNumber_success() {
        // Arrange
        val uShort = 0x0012u.toUShort()
        val expected = listOf<Byte>(0x12, 0x00)

        // Act
        val result = uShort.toBytesLittleEndian()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun byteToIntUnsigned_positiveByte_success() {
        // Arrange
        val byte = 120.toByte()
        val expected = 120

        // Act
        val result = byte.toIntUnsigned()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun byteToIntUnsigned_negativeByte_success() {
        // Arrange
        val byte = (-120).toByte()
        val expected = 136

        // Act
        val result = byte.toIntUnsigned()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun charToUnicodeRepresentation_char_success() {
        // Arrange
        val char = '\uABCD'
        val expected = "\\uABCD"

        // Act
        val result = char.toUnicodeRepresentation()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun byteArrayToHexString_bytes_success() {
        // Arrange
        val bytes = byteArrayOf(0x12, 0x34, 0xAB.toByte(), 0xCD.toByte())
        val expected = "[12, 34, AB, CD]"

        // Act
        val result = bytes.toHexString()

        // Assert
        assertEquals(expected, result)
    }
}
