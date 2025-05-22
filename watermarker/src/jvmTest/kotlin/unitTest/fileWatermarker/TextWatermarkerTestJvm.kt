/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.fileWatermarker

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import openTextFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextWatermarkerTestJvm {
    private val textWatermarker = TextWatermarker.default()

    @Test
    fun addWatermark_valid_success() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum.txt")
        val watermark = Watermark.fromString("Hello World")
        val expected = openTextFile("src/jvmTest/resources/lorem_ipsum_watermarked.txt")
        val expectedMessage = TextWatermarker.Success(listOf(5, 273, 538)).into().toString()

        // Act
        val result = textWatermarker.addWatermark(file, watermark)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected.content, file.content)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addWatermark_containsAlphabetChars_error() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum_watermarked.txt")
        val watermarkBytes = "Hello World".encodeToByteArray()
        val watermark = Watermark(watermarkBytes)
        val expectedMessage =
            TextWatermarker.ContainsAlphabetCharsError(
                sequence {
                    yield(DefaultTranscoding.SEPARATOR_CHAR)
                    yieldAll(DefaultTranscoding.alphabet)
                },
            ).into().toString()

        // Act
        val result = textWatermarker.addWatermark(file, watermark)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addWatermark_oversizedWatermark_warning() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum.txt")
        val watermark = Watermark.fromString("This is a watermark that does not fit")
        val expectedMessage = TextWatermarker.OversizedWatermarkWarning(150, 96).into().toString()

        // Act
        val result = textWatermarker.addWatermark(file, watermark)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun containsWatermark_watermark_true() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum_watermarked.txt")

        // Act
        val result = textWatermarker.containsWatermark(file)

        // Assert
        assertTrue(result)
    }

    @Test
    fun containsWatermark_noWatermark_false() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum.txt")

        // Act
        val result = textWatermarker.containsWatermark(file)

        // Assert
        assertTrue(!result)
    }

    @Test
    fun getWatermarks_watermarks_successAndWatermarks() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum_watermarked.txt")
        val expected =
            listOf(
                Watermark.fromString("Hello World"),
                Watermark.fromString("Hello World"),
            )

        // Act
        val result = textWatermarker.getWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_noWatermark_successAndEmptyList() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum.txt")

        // Act
        val result = textWatermarker.getWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
    }

    @Test
    fun getWatermarks_partialWatermark_warningAndWatermark() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum_partial_watermark.txt")
        val expectedMessage = TextWatermarker.IncompleteWatermarkWarning().into().toString()

        // Act
        val result = textWatermarker.getWatermarks(file)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedMessage, result.toString())
        assertFalse(result.value?.isEmpty() != false)
    }

    @Test
    fun removeWatermarks_watermarks_successAndWatermarks() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum_watermarked.txt")
        val expected =
            listOf(
                Watermark.fromString("Hello World"),
                Watermark.fromString("Hello World"),
            )

        // Act
        val result = textWatermarker.removeWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun removeWatermarks_noWatermark_successAndEmptyList() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum.txt")

        // Act
        val result = textWatermarker.removeWatermarks(file)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
    }

    @Test
    fun removeWatermarks_partialWatermark_warningAndWatermark() {
        // Arrange
        val file = openTextFile("src/jvmTest/resources/lorem_ipsum_partial_watermark.txt")
        val status = TextWatermarker.IncompleteWatermarkWarning().into()
        status.addEvent(TextWatermarker.RemoveWatermarksGetProblemWarning(), true)
        val expectedMessage = status.toString()

        // Act
        val result = textWatermarker.removeWatermarks(file)

        // Assert
        assertTrue(result.isWarning)
        assertFalse(result.value?.isEmpty() != false)
        assertEquals(expectedMessage, result.toString())
    }
}
