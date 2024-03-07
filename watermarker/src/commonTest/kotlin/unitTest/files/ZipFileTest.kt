/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.files

import de.fraunhofer.isst.trend.watermarker.files.ZipFileHeader
import kotlin.test.Test
import kotlin.test.assertEquals

class ZipFileTest {
    @Test
    fun extraFieldNotEnoughBytesError_string_success() {
        // Arrange
        val error = ZipFileHeader.ExtraField.NotEnoughBytesError()
        val expected =
            "Error (ZipFileExtraField): Ran out of data trying to parse extra fields."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun extraFieldOversizedHeaderError_string_success() {
        // Arrange
        val error = ZipFileHeader.ExtraField.OversizedHeaderError(65536)
        val expected =
            "Error (ZipFileExtraField.addExtraField): The new header size (65536 Bytes) would " +
                "exceed the maximum header size (65535 Bytes)!"

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun notEnoughBytesError_string_success() {
        // Arrange
        val error = ZipFileHeader.NotEnoughBytesError()
        val expected =
            "Error (ZipFileHeader): Not enough bytes."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidMagicBytesError_string_success() {
        // Arrange
        val error = ZipFileHeader.InvalidMagicBytesError()
        val expected =
            "Error (ZipFileHeader): Invalid magic bytes."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }
}
