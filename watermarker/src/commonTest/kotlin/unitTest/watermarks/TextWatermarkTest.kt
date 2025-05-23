/*
 * Copyright (c) 2024-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.innamark.watermarker.watermarks.CRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedCRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedRawInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedCRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedSHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.RawInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedCRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedSHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextWatermarkTest {
    private val text = "Lorem Ipsum"
    private val textBytes = text.encodeToByteArray()

    @Test
    fun new_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = RawInnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.new(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun raw_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = RawInnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.raw(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun compressed_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedRawInnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressed(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun small_loremIpsum_compression() {
        // Arrange
        val customText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr"
        val customTextBytes = customText.encodeToByteArray()
        val expectedTrendmark = CompressedRawInnamarkTag.new(customTextBytes)

        // Act
        val textWatermark = TextWatermark.small(customText)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(customText, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(customTextBytes, content.value)
    }

    @Test
    fun small_loremIpsum_noCompression() {
        // Arrange
        val customText = "Lorem"
        val customTextBytes = customText.encodeToByteArray()
        val expectedTrendmark = RawInnamarkTag.new(customTextBytes)

        // Act
        val textWatermark = TextWatermark.small(customText)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(customText, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(customTextBytes, content.value)
    }

    @Test
    fun sized_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedInnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.sized(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun CRC32_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.CRC32(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun SHA3256_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.SHA3256(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun compressedSized_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedInnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedSized(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun compressedCRC32_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedCRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedCRC32(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun compressedSHA3256_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedSHA3256(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun sizedCRC32_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedCRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.sizedCRC32(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun sizedSHA3256_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = SizedSHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.sizedSHA3256(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun compressedSizedCRC32_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedCRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedSizedCRC32(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun compressedSizedSHA3256_loremIpsum_success() {
        // Arrange
        val expectedTrendmark = CompressedSizedSHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermark = TextWatermark.compressedSizedSHA3256(text)
        val trendmark = textWatermark.finish()
        val content = trendmark.getContent()

        // Assert
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertEquals(expectedTrendmark, trendmark)
        assertTrue(content.isSuccess)
        assertContentEquals(textBytes, content.value)
    }

    @Test
    fun fromTrendmark_RawTrendmark_success() {
        // Arrange
        val initialTrendmark = RawInnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedTrendmark_success() {
        // Arrange
        val initialTrendmark = SizedInnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedTrendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedInnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = CRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedCRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = SizedCRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedCRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedCRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedCRC32Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedCRC32InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertTrue(textWatermark.isCRC32())
        assertFalse(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = SHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_SizedSHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = SizedSHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertFalse(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertFalse(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
        assertNotNull(trendmark)
        assertEquals(initialTrendmark, trendmark)
    }

    @Test
    fun fromTrendmark_CompressedSizedSHA3256Trendmark_success() {
        // Arrange
        val initialTrendmark = CompressedSizedSHA3256InnamarkTag.new(textBytes)

        // Act
        val textWatermarkResult = TextWatermark.fromInnamarkTag(initialTrendmark)
        val trendmark = textWatermarkResult.value?.finish()

        // Assert
        assertTrue(textWatermarkResult.isSuccess)
        val textWatermark = textWatermarkResult.value!!
        assertEquals(text, textWatermark.text)
        assertTrue(textWatermark.isSized())
        assertTrue(textWatermark.isCompressed())
        assertFalse(textWatermark.isCRC32())
        assertTrue(textWatermark.isSHA3256())
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
    @Suppress("ktlint:standard:function-naming")
    fun CRC32_true_true() {
        // Arrange
        val textWatermark = TextWatermark.CRC32(text)

        // Act
        textWatermark.CRC32()

        // Assert
        assertTrue(textWatermark.isCRC32())
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun SHA3256_true_true() {
        // Arrange
        val textWatermark = TextWatermark.SHA3256(text)

        // Act
        textWatermark.SHA3256()

        // Assert
        assertTrue(textWatermark.isSHA3256())
    }
}
