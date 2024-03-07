/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.fileWatermarker.ZipWatermark
import de.fraunhofer.isst.trend.watermarker.fileWatermarker.ZipWatermarker
import de.fraunhofer.isst.trend.watermarker.files.ZipFileHeader
import openZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZipWatermarkerTestJvm {
    private val zipWatermarker = ZipWatermarker

    @Test
    fun addWatermark_valid_success() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")
        val watermarkText = "Lorem ipsum dolor sit amet"
        val watermark = ZipWatermark(watermarkText.encodeToByteArray().asList())
        val expected = openZipFile("src/jvmTest/resources/multiple_files_watermarked.zip")

        // Act
        val result = zipWatermarker.addWatermark(file, watermark)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, file)
    }

    @Test
    fun addWatermark_oversizedWatermark_error() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")
        val watermark = ZipWatermark(List<Byte>(UShort.MAX_VALUE.toInt()) { 0 })
        val expectedMessage = ZipFileHeader.ExtraField.OversizedHeaderError(65567).into().toString()

        // Act
        val status = zipWatermarker.addWatermark(file, watermark)

        // Assert
        assertTrue(status.isError)
        assertEquals(expectedMessage, status.toString())
    }

    @Test
    fun containsWatermark_watermark_true() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files_watermarked.zip")

        // Act
        val result = zipWatermarker.containsWatermark(file)

        // Assert
        assertTrue(result)
    }

    @Test
    fun containsWatermark_noWatermark_false() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")

        // Act
        val result = zipWatermarker.containsWatermark(file)

        // Assert
        assertFalse(result)
    }

    @Test
    fun getWatermarks_watermark_successAndWatermark() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files_watermarked.zip")
        val expected =
            listOf(
                ZipWatermark("Lorem ipsum dolor sit amet".encodeToByteArray().asList()),
            )

        // Act
        val result = zipWatermarker.getWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_noWatermark_successAndEmptyList() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")

        // Act
        val result = zipWatermarker.getWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() ?: false)
    }

    @Test
    fun removeWatermarks_watermark_successAndWatermark() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files_watermarked.zip")
        val expected =
            listOf(
                ZipWatermark("Lorem ipsum dolor sit amet".encodeToByteArray().asList()),
            )

        // Act
        val result = zipWatermarker.removeWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun removeWatermarks_noWatermark_successAndEmptyList() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")

        // Act
        val result = zipWatermarker.removeWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() ?: false)
    }
}
