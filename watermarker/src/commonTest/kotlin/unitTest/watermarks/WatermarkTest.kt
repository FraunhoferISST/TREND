/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.trend.watermarker.watermarks.RawTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatermarkTest {
    @Test
    fun mostFrequent_success() {
        // Arrange
        val firstWatermark = Watermark.fromString("test")
        val secondWatermark = Watermark.fromString("okay")
        val thirdWatermark = Watermark.fromString("yeah")
        val watermarks =
            listOf(
                firstWatermark,
                firstWatermark,
                secondWatermark,
                thirdWatermark,
            )
        val expectedResult = listOf(firstWatermark, firstWatermark)

        // Act
        val result = Watermark.mostFrequent(watermarks)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.value)
    }

    @Test
    fun mostFrequent_bytes_success() {
        // Arrange
        val firstWatermark = Watermark(listOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8))
        val secondWatermark = Watermark(listOf(0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18))
        val thirdWatermark = Watermark(listOf(0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28))
        val watermarks =
            listOf(
                firstWatermark,
                firstWatermark,
                secondWatermark,
                thirdWatermark,
            )
        val expectedResult = listOf(firstWatermark, firstWatermark)

        // Act
        val result = Watermark.mostFrequent(watermarks)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.value)
    }

    @Test
    fun mostFrequent_empty_success() {
        // Arrange
        val watermarks = listOf<Watermark>()
        val expectedResult = listOf<Watermark>()

        // Act
        val result = Watermark.mostFrequent(watermarks)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.value)
    }

    @Test
    fun mostFrequent_trendmark_success() {
        // Arrange
        val firstTrendmark = RawTrendmark.fromString("test")
        val secondTrendmark = RawTrendmark.fromString("okay")
        val thirdTrendmark = RawTrendmark.fromString("yeah")
        val watermarks =
            listOf(
                firstTrendmark,
                firstTrendmark,
                secondTrendmark,
                thirdTrendmark,
            )
        val expectedResult = listOf(firstTrendmark, firstTrendmark)

        // Act
        val result = Watermark.mostFrequent(watermarks)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.value)
    }

    @Test
    fun mostFrequent_warning() {
        // Arrange
        val firstWatermark = Watermark.fromString("test")
        val secondWatermark = Watermark.fromString("okay")
        val thirdWatermark = Watermark.fromString("yeah")
        val watermarks =
            listOf(
                firstWatermark,
                firstWatermark,
                secondWatermark,
                secondWatermark,
                thirdWatermark,
            )
        val expectedResult =
            listOf(
                firstWatermark,
                firstWatermark,
                secondWatermark,
                secondWatermark,
            )

        // Act
        val result = Watermark.mostFrequent(watermarks)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedResult, result.value)
    }

    @Test
    fun multipleMostFrequentWarning_string_success() {
        // Arrange
        val warning = Watermark.MultipleMostFrequentWarning(9)
        val expected = "Warning (Watermark.mostFrequent): 9 most frequent watermarks found!"

        // Act
        val result = warning.toString()

        // Assert
        assertEquals(result, expected)
    }

    @Test
    fun frequencyAnalysisError_string_success() {
        // Arrange
        val error = Watermark.FrequencyAnalysisError("ErrorType")
        val expected =
            "Error (Watermark.mostFrequent): Frequency analysis failed with exception: ErrorType!"

        // Act
        val result = error.toString()

        // Assert
        assertEquals(result, expected)
    }
}
