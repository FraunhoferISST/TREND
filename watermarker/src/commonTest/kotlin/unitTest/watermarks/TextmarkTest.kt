/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.trend.watermarker.watermarks.CRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedCRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedRawWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedCRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedSHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.RawWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedCRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedSHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.Textmark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextmarkTest {
    private val text = "Lorem Ipsum"
    private val textBytes = text.encodeToByteArray().asList()

    @Test
    fun new_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = RawWatermark.new(textBytes)

        // Act
        val textmark = Textmark.new(text)
        val trendmark = textmark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textmark.text)
        assertFalse(textmark.isSized())
        assertFalse(textmark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedRawWatermark.new(textBytes)

        // Act
        val textmark = Textmark.compressed(text)
        val trendmark = textmark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textmark.text)
        assertFalse(textmark.isSized())
        assertTrue(textmark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun sized_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedWatermark.new(textBytes)

        // Act
        val textmark = Textmark.sized(text)
        val trendmark = textmark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textmark.text)
        assertTrue(textmark.isSized())
        assertFalse(textmark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressedAndSized_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedWatermark.new(textBytes)

        // Act
        val textmark = Textmark.compressedAndSized(text)
        val trendmark = textmark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textmark.text)
        assertTrue(textmark.isSized())
        assertTrue(textmark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun fromTrendmark_RawWatermark_success() {
        // Arrange
        val initialTrendmark = RawWatermark.new(textBytes)

        // Act
        val textmarkResult = Textmark.fromTrendmark(initialTrendmark)
        val trendmark = textmarkResult.value?.finish()

        // Assert
        assertTrue(textmarkResult.isSuccess)
        val textmark = textmarkResult.value!!
        assertEquals(text, textmark.text)
        assertFalse(textmark.isSized())
        assertFalse(textmark.isCompressed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedWatermark_success() {
        // Arrange
        val initialTrendmark = SizedWatermark.new(textBytes)

        // Act
        val textmarkResult = Textmark.fromTrendmark(initialTrendmark)
        val trendmark = textmarkResult.value?.finish()

        // Assert
        assertTrue(textmarkResult.isSuccess)
        val textmark = textmarkResult.value!!
        assertEquals(text, textmark.text)
        assertTrue(textmark.isSized())
        assertFalse(textmark.isCompressed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedWatermark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedWatermark.new(textBytes)

        // Act
        val textmarkResult = Textmark.fromTrendmark(initialTrendmark)
        val trendmark = textmarkResult.value?.finish()

        // Assert
        assertTrue(textmarkResult.isSuccess)
        val textmark = textmarkResult.value!!
        assertEquals(text, textmark.text)
        assertTrue(textmark.isSized())
        assertTrue(textmark.isCompressed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CRC32Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CRC32Watermark.new(textBytes)
        val expectedStatus = Textmark.UnsupportedTrendmarkError(CRC32Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_SizedCRC32Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = SizedCRC32Watermark.new(textBytes)
        val expectedStatus = Textmark.UnsupportedTrendmarkError(SizedCRC32Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_CompressedCRC32Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedCRC32Watermark.new(textBytes)
        val expectedStatus =
            Textmark.UnsupportedTrendmarkError(CompressedCRC32Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_CompressedSizedCRC32Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedSizedCRC32Watermark.new(textBytes)
        val expectedStatus =
            Textmark.UnsupportedTrendmarkError(CompressedSizedCRC32Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_SHA3256Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = SHA3256Watermark.new(textBytes)
        val expectedStatus = Textmark.UnsupportedTrendmarkError(SHA3256Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_SizedSHA3256Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = SizedSHA3256Watermark.new(textBytes)
        val expectedStatus = Textmark.UnsupportedTrendmarkError(SizedSHA3256Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_CompressedSHA3256Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedSHA3256Watermark.new(textBytes)
        val expectedStatus =
            Textmark.UnsupportedTrendmarkError(CompressedSHA3256Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun fromTrendmark_CompressedSizedSHA3256Watermark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedSizedSHA3256Watermark.new(textBytes)
        val expectedStatus =
            Textmark.UnsupportedTrendmarkError(CompressedSizedSHA3256Watermark.SOURCE).into()

        // Act
        val textmarkResult = Textmark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textmarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textmarkResult.status.getMessage())
    }

    @Test
    fun compressed_true_true() {
        // Arrange
        val textmark = Textmark.new(text)

        // Act
        textmark.compressed()

        // Assert
        assertTrue(textmark.isCompressed())
    }

    @Test
    fun sized_true_true() {
        // Arrange
        val textmark = Textmark.sized(text)

        // Act
        textmark.sized()

        // Assert
        assertTrue(textmark.isSized())
    }
}
