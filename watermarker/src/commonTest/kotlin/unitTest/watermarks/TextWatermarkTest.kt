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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun raw_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = RawTrendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.raw(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun small_loremIpsum_compression() {
        // Arrange
        val customText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr"
        val customTextBytes = customText.encodeToByteArray().asList()
        val expectedTrendmark = CompressedRawTrendmark.new(customTextBytes)

        // Act
        val textWatermark = TextWatermark.small(customText)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(customText, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(customTextBytes, content.value)
    }

    @Test
    fun small_loremIpsum_noCompression() {
        // Arrange
        val customText = "Lorem"
        val customTextBytes = customText.encodeToByteArray().asList()
        val expectedTrendmark = RawTrendmark.new(customTextBytes)

        // Act
        val textWatermark = TextWatermark.small(customText)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(customText, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(customTextBytes, content.value)
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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun checked_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CRC32Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.checked(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun hashed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SHA3256Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.hashed(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressedAndChecked_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedCRC32Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedAndChecked(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressedAndHashed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSHA3256Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedAndHashed(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun sizedAndChecked_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedCRC32Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.sizedAndChecked(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun sizedAndHashed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedSHA3256Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.sizedAndHashed(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressedSizedChecked_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedCRC32Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedSizedChecked(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertEquals(textBytes, content.value)
    }

    @Test
    fun compressedSizedHashed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedSHA3256Trendmark.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedSizedHashed(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
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
        assertFalse(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = CRC32Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedCRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = SizedCRC32Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedCRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedCRC32Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedCRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedCRC32Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isChecked())
        assertFalse(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = SHA3256Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedSHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = SizedSHA3256Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSHA3256Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedSHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedSHA3256Trendmark.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromTrendmark(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isChecked())
        assertTrue(textWatermark.isHashed())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun compressed_true_true() {
        // Arrange
        val textWatermark = TextWatermark.compressed(text)

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

    @Test
    fun checked_true_true() {
        // Arrange
        val textWatermark = TextWatermark.checked(text)

        // Act
        textWatermark.checked()

        // Assert
        assertTrue(textWatermark.isChecked())
    }

    @Test
    fun hashed_true_true() {
        // Arrange
        val textWatermark = TextWatermark.hashed(text)

        // Act
        textWatermark.hashed()

        // Assert
        assertTrue(textWatermark.isHashed())
    }
}
