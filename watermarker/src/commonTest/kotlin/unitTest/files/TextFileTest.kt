/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.files

import de.fraunhofer.isst.trend.watermarker.files.TextFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextFileTest {
    private val loremIpsum =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
            "volutpat blandit aliquam etiam erat velit."
    private val loremIpsumBytes = loremIpsum.encodeToByteArray()

    @Test
    fun fromBytes_validBytes_success() {
        // Act
        val result = TextFile.fromBytes(loremIpsumBytes)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(loremIpsum, result.value?.content)
    }

    @Test
    fun fromBytes_invalidBytes_error() {
        // Arrange
        val bytes =
            byteArrayOf(
                0x4c, 0x6f, 0x72, 0x65, 0x6d, 0x20, 0x69, 0x70, 0x73, 0x75, 0x6d,
                0xff.toByte(),
            )
        val expectedMessage = TextFile.InvalidByteError().into().toString()

        // Act
        val result = TextFile.fromBytes(bytes)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun invalidByteError_string_success() {
        // Arrange
        val error = TextFile.InvalidByteError()
        val expected = "Error (TextFile): Cannot parse text file: File contains invalid byte(s)."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }
}
