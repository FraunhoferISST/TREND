/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.textWatermarkers

import de.fraunhofer.isst.innamark.watermarker.textWatermarkers.PlainTextWatermarker
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlainTextWatermarkerTest {
    val watermarker = PlainTextWatermarker()
    val loremIpsum =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonu" +
            "my eirmod tempor invidunt ut labore et dolore magna aliquyam erat," +
            " sed diam voluptua. At vero eos et accusam et justo duo dolores et" +
            " ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur" +
            " sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labor" +
            "e et dolore magna aliquyam erat, sed diam voluptua. At vero eos et" +
            " accusam et justo duo dolores et ea rebum. Stet clita kasd gubergr" +
            "en, no sea takimata sanctus est Lorem ipsum dolor sit amet."
    val watermarkedText =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonu" +
            "my eirmod tempor invidunt ut labore et dolore magna aliquyam erat," +
            " sed diam voluptua. At vero eos et accusam et justo duo dolores et" +
            " ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur" +
            " sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labor" +
            "e et dolore magna aliquyam erat, sed diam voluptua. At vero eos et" +
            " accusam et justo duo dolores et ea rebum. Stet clita kasd gubergr" +
            "en, no sea takimata sanctus est Lorem ipsum dolor sit amet."
    val watermarkedText2 =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonu" +
            "my eirmod tempor invidunt ut labore et dolore magna aliquyam erat," +
            " sed diam voluptua. At vero eos et accusam et justo duo dolores et" +
            " ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur" +
            " sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labor" +
            "e et dolore magna aliquyam erat, sed diam voluptua. At vero eos et" +
            " accusam et justo duo dolores et ea rebum. Stet clita kasd gubergr" +
            "en, no sea takimata sanctus est Lorem ipsum dolor sit amet."
    val combinedTextEqual = watermarkedText + watermarkedText2
    val combinedTextUnequal = watermarkedText + watermarkedText2 + watermarkedText2
    val watermark = "Test"
    val watermarkBytes = watermark.encodeToByteArray()
    val watermark2 = "Okay"
    val watermarkBytes2 = watermark2.encodeToByteArray()
    val malformedBytes = byteArrayOf(0x54.toByte(), 0x65.toByte(), 0x73.toByte(), 0xFF.toByte())

    @Test
    fun getWatermarkAsString_unmarkedText_Success() {
        // Arrange
        val expected = ""

        // Act
        val result = watermarker.getWatermarkAsString(loremIpsum)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsString_singleWatermark_Success() {
        // Arrange
        val expected = watermark

        // Act
        val result = watermarker.getWatermarkAsString(watermarkedText)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsString_multipleWatermarks_equalAndWarning() {
        // Arrange
        val expected = watermark
        val expectedStatus = Watermark.MultipleMostFrequentWarning(2)

        // Act
        val result = watermarker.getWatermarkAsString(combinedTextEqual)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(result.status.getMessage(), expectedStatus.getMessage())
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsString_multipleWatermarks_unequalAndSuccess() {
        // Arrange
        val expected = watermark2

        // Act
        val result = watermarker.getWatermarkAsString(combinedTextUnequal)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsString_singleWatermark_StringDecodeWarning() {
        // Arrange
        val expected = "Tes" + '\uFFFD'
        val expectedStatus = Watermark.StringDecodeWarning("PlainTextWatermarker")
        val watermarked = watermarker.addWatermark(loremIpsum, malformedBytes)

        // Act
        val result = watermarker.getWatermarkAsString(watermarked.value!!)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedStatus.getMessage(), result.getMessage())
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsByteArray_unmarkedText_Success() {
        // Arrange
        val expected = byteArrayOf()

        // Act
        val result = watermarker.getWatermarkAsByteArray(loremIpsum)

        // Assert
        assertTrue(result.isSuccess)
        assertContentEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsByteArray_singleWatermark_Success() {
        // Arrange
        val expected = watermarkBytes

        // Act
        val result = watermarker.getWatermarkAsByteArray(watermarkedText)

        // Assert
        assertTrue(result.isSuccess)
        assertContentEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsByteArray_multipleWatermarks_equalAndWarning() {
        // Arrange
        val expected = watermarkBytes
        val expectedStatus = Watermark.MultipleMostFrequentWarning(2)

        // Act
        val result = watermarker.getWatermarkAsByteArray(combinedTextEqual)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(result.status.getMessage(), expectedStatus.getMessage())
        assertContentEquals(expected, result.value)
    }

    @Test
    fun getWatermarkAsByteArray_multipleWatermarks_unequalAndSuccess() {
        // Arrange
        val expected = watermarkBytes2

        // Act
        val result = watermarker.getWatermarkAsByteArray(combinedTextUnequal)

        // Assert
        assertTrue(result.isSuccess)
        assertContentEquals(expected, result.value)
    }
}
