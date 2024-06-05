/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.trend.watermarker.watermarks.CRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedCRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedRawTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedCRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedSHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.RawTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedCRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedSHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextWatermarkTest {
    private val text = "Lorem Ipsum"
    private val textBytes = text.encodeToByteArray().asList()

    @Test
    fun new_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = RawTrendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.new(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedRawTrendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressed(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun sized_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedTrendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.sized(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressedAndSized_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedTrendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedAndSized(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun fromTrendmark_RawTrendmark_success() {
        // Arrange
        val initialTrendmark = RawTrendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedTrendmark_success() {
        // Arrange
        val initialTrendmark = SizedTrendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedTrendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedTrendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CRC32Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CRC32Trendmark.new(textBytes)
        val expectedStatus = TextWatermark.UnsupportedTrendmarkError(CRC32Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_SizedCRC32Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = SizedCRC32Trendmark.new(textBytes)
        val expectedStatus =
            TextWatermark.UnsupportedTrendmarkError(SizedCRC32Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_CompressedCRC32Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedCRC32Trendmark.new(textBytes)
        val expectedStatus =
            TextWatermark.UnsupportedTrendmarkError(CompressedCRC32Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_CompressedSizedCRC32Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedSizedCRC32Trendmark.new(textBytes)
        val expectedStatus =
            TextWatermark.UnsupportedTrendmarkError(CompressedSizedCRC32Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_SHA3256Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = SHA3256Trendmark.new(textBytes)
        val expectedStatus = TextWatermark.UnsupportedTrendmarkError(SHA3256Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_SizedSHA3256Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = SizedSHA3256Trendmark.new(textBytes)
        val expectedStatus =
            TextWatermark.UnsupportedTrendmarkError(SizedSHA3256Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_CompressedSHA3256Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedSHA3256Trendmark.new(textBytes)
        val expectedStatus =
            TextWatermark.UnsupportedTrendmarkError(CompressedSHA3256Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun fromTrendmark_CompressedSizedSHA3256Trendmark_UnsupportedTrendmarkError() {
        // Arrange
        val trendmark = CompressedSizedSHA3256Trendmark.new(textBytes)
        val expectedStatus =
            TextWatermark.UnsupportedTrendmarkError(CompressedSizedSHA3256Trendmark.SOURCE).into()

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(trendmark)

        // Assert
        assertTrue(textWatermarkResult.isError)
        assertEquals(expectedStatus.getMessage(), textWatermarkResult.status.getMessage())
        assertNull(textWatermarkResult.value)
    }

    @Test
    fun compressed_true_true() {
        // Arrange
        val textWatermark = TextWatermark.new(text)

        // Act
        textWatermark.compressed()

        // Assert
        assertTrue(textWatermark.isCompressed())
    }

    @Test
    fun sized_true_true() {
        // Arrange
        val textWatermark = TextWatermark.sized(text)

        // Act
        textWatermark.sized()

        // Assert
        assertTrue(textWatermark.isSized())
    }
}
