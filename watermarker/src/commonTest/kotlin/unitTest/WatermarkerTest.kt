/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest

import de.fraunhofer.isst.trend.watermarker.SupportedFileType
import de.fraunhofer.isst.trend.watermarker.Watermarker
import de.fraunhofer.isst.trend.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.trend.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark.FailedTextWatermarkExtractionsWarning
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark.FailedTrendmarkExtractionsWarning
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import platform
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WatermarkerTest {
    private val watermarker = Watermarker()

    private val textWithoutWatermark =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor i" +
            "nvidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et a" +
            "ccusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
            "sanctus est Lorem ipsum dolor sit amet."

    private val watermarkString = "Test"
    private val watermark = watermarkString.encodeToByteArray().asList()

    private val textWithWatermark =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor i" +
            "nvidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et a" +
            "ccusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
            "sanctus est Lorem ipsum dolor sit amet."

    private val differentWatermarks = listOf("Test", "Okay", "Okay", "Okay", "Okay", "Okay")

    private val longTextWithDifferentWatermarks =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididun" +
            "t ut labore et dolore magna aliqua. Commodo sed egestas egestas fringilla phasellus " +
            "faucibus scelerisque eleifend. Adipiscing diam donec adipiscing tristique risus nec " +
            "feugiat. Duis ut diam quam nulla porttitor massa. Elementum tempus egestas sed sed. " +
            "Amet commodo nulla facilisi nullam vehicula ipsum. Ornare aenean euismod elementum n" +
            "isi quis eleifend quam adipiscing.  Non tellus orci ac auctor augue. Tristique et eg" +
            "estas quis ipsum suspendisse. Ut pharetra sit amet aliquam. In iaculis nunc sed augu" +
            "e. Cursus sit amet dictum sit amet justo donec enim diam. Ultricies lacus sed turpis" +
            " tincidunt id aliquet risus feugiat in. Risus commodo viverra maecenas accumsan"

    private val textWithTrendmarks =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor i" +
            "nvidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et a" +
            "ccusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
            "sanctus est Lorem ipsum dolor sit amet."

    private val textWithWatermarksAndTrendmarks =
        textWithWatermark +
            DefaultTranscoding.SEPARATOR_CHAR +
            textWithTrendmarks

    private val textWithInvalidUTF8Trendmark =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor i" +
            "nvidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et a" +
            "ccusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
            "sanctus est Lorem ipsum dolor sit amet."

    private val textWithInvalidandValidUTF8Trendmarks =
        textWithInvalidUTF8Trendmark +
            DefaultTranscoding.SEPARATOR_CHAR +
            textWithTrendmarks

    @Test
    fun textAddWatermark_successfulAddition_successAndWatermarkedString() {
        // Arrange

        // Act
        val result = watermarker.textAddWatermark(textWithoutWatermark, watermark)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(textWithWatermark, result.value)
    }

    @Test
    fun textAddWatermark_watermarkTooLong_warningAndWatermarkedString() {
        // Arrange
        val watermark = "Hello, world!".encodeToByteArray().asList()
        val expectedMessage = TextWatermarker.OversizedWatermarkWarning(53, 49).into().toString()
        val expected =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod temp" +
                "or invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero " +
                "eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no s" +
                "ea takimata sanctus est Lorem ipsum dolor sit amet."

        // Act
        val result = watermarker.textAddWatermark(textWithoutWatermark, watermark)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedMessage, result.toString())
        assertEquals(expected, result.value)
    }

    @Test
    fun textAddWatermark_textContainsAlphabetChars_error() {
        // Arrange
        val expectedMessage =
            TextWatermarker.ContainsAlphabetCharsError(
                sequence {
                    yield(DefaultTranscoding.SEPARATOR_CHAR)
                    yieldAll(DefaultTranscoding.alphabet)
                },
            ).into().toString()

        // Act
        val result = watermarker.textAddWatermark(textWithWatermark, watermark)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun textContainsWatermark_watermark_true() {
        // Arrange

        // Act
        val result = watermarker.textContainsWatermark(textWithWatermark)

        // Assert
        assertTrue(result)
    }

    @Test
    fun textContainsWatermark_noWatermark_false() {
        // Arrange

        // Act
        val result = watermarker.textContainsWatermark(textWithoutWatermark)

        // Assert
        assertFalse(result)
    }

    @Test
    fun textGetWatermarks_watermark_successAndWatermark() {
        // Arrange
        val expected =
            listOf(
                Watermark.fromString(watermarkString),
            )

        // Act
        val result = watermarker.textGetWatermarks(textWithWatermark)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_differentWatermarks_successAndNotSquashedWatermarks() {
        // Arrange
        val expected =
            differentWatermarks.map {
                Watermark.fromString(it)
            }

        // Act
        val result = watermarker.textGetWatermarks(longTextWithDifferentWatermarks, false)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_differentWatermarks_successAndWatermarks() {
        // Arrange
        val expected =
            differentWatermarks.toSet().map {
                Watermark.fromString(it)
            }

        // Act
        val result = watermarker.textGetWatermarks(longTextWithDifferentWatermarks)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_partialWatermark_WarningAndWatermark() {
        // Arrange
        val text =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed" +
                " diam nonumy eirmod tempor invidunt ut labore et"
        val expected =
            listOf(
                Watermark.fromString(watermarkString),
            )
        val expectedMessage =
            TextWatermarker.IncompleteWatermarkWarning()
                .into().toString()

        // Act
        val result = watermarker.textGetWatermarks(text)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedMessage, result.toString())
        assertContentEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_noWatermark_successAndEmptyList() {
        // Arrange

        // Act
        val result = watermarker.textGetWatermarks(textWithoutWatermark)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
    }

    @Test
    fun textRemoveWatermarks_watermark_successAndCleanedString() {
        // Arrange

        // Act
        val result = watermarker.textRemoveWatermarks(textWithWatermark)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(textWithoutWatermark, result.value)
    }

    @Test
    fun textRemoveWatermarks_partialWatermark_WarningAndWatermark() {
        // Arrange
        val text =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed" +
                " diam nonumy eirmod tempor invidunt ut labore et"
        val expected =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
                "tempor invidunt ut labore et"
        val status = TextWatermarker.IncompleteWatermarkWarning().into()
        status.addEvent(TextWatermarker.RemoveWatermarksGetProblemWarning(), true)
        val expectedMessage = status.toString()

        // Act
        val result = watermarker.textRemoveWatermarks(text)

        // Assert
        assertTrue(result.isWarning)
        assertEquals(expectedMessage, result.toString())
        assertEquals(expected, result.value)
    }

    @Test
    fun textRemoveWatermarks_noWatermark_successAndUnchangedString() {
        // Arrange

        // Act
        val result = watermarker.textRemoveWatermarks(textWithoutWatermark)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(textWithoutWatermark, result.value)
    }

    @Test
    fun noFileTypeError_string_success() {
        // Arrange
        val error = SupportedFileType.NoFileTypeError("/some/path")
        val expected = "Error (SupportedFileType): Could not determine file type of /some/path!"

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun unsupportedTypeError_string_success() {
        // Arrange
        val error = SupportedFileType.UnsupportedTypeError("unsupported")
        val expected = "Error (SupportedFileType): Unsupported file type: unsupported!"

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun textGetTrendmarks_trendmarkedString_success() {
        // Arrange
        val expectedTrendmarks = listOf(SizedTrendmark.fromString(watermarkString))

        // Act
        val trendmarks = watermarker.textGetTrendmarks(textWithTrendmarks)

        // Assert
        assertTrue(trendmarks.isSuccess)
        assertEquals(expectedTrendmarks, trendmarks.value)
    }

    @Test
    fun textGetTrendmarks_watermarkedString_error() {
        // Arrange
        val expectedError = Trendmark.UnknownTagError(0x54u).into()

        // Act
        val trendmarks = watermarker.textGetTrendmarks(textWithWatermark)

        // Assert
        assertTrue(trendmarks.isError)
        assertEquals(expectedError.toString(), trendmarks.toString())
        assertNull(trendmarks.value)
    }

    @Test
    fun textGetTrendmarks_watermarkedAndTrendmarkedString_warning() {
        // Arrange
        val unkownTagError = Trendmark.UnknownTagError(0x54u)
        val expectedStatus = unkownTagError.into()
        expectedStatus.addEvent(unkownTagError)
        expectedStatus.addEvent(FailedTrendmarkExtractionsWarning("Watermarker.textGetTrendmarks"))
        val expectedTrendmarks = listOf(SizedTrendmark.fromString(watermarkString))

        // Act
        val trendmarks = watermarker.textGetTrendmarks(textWithWatermarksAndTrendmarks)

        // Assert
        assertTrue(trendmarks.isWarning)
        assertEquals(expectedStatus.toString(), trendmarks.toString())
        assertEquals(expectedTrendmarks, trendmarks.value)
    }

    @Test
    fun textGetTextWatermarks_trendmarkedString_success() {
        // Arrange
        val expectedTextWatermark = TextWatermark.sized(watermarkString)
        val expectedTextWatermarks = listOf(expectedTextWatermark)

        // Act
        val textWatermarks = watermarker.textGetTextWatermarks(textWithTrendmarks)

        // Assert
        assertTrue(textWatermarks.isSuccess)
        assertEquals(expectedTextWatermarks, textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_watermarkedString_error() {
        // Arrange
        val expectedError = Trendmark.UnknownTagError(0x54u).into()

        // Act
        val textWatermarks = watermarker.textGetTextWatermarks(textWithWatermark)

        // Assert
        assertTrue(textWatermarks.isError)
        assertEquals(expectedError.toString(), textWatermarks.toString())
        assertNull(textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_watermarkedAndTrendmarkedString_warning() {
        // Arrange
        val unknownTagError = Trendmark.UnknownTagError(0x54u)
        val expectedStatus = unknownTagError.into()
        expectedStatus.addEvent(unknownTagError)
        expectedStatus.addEvent(
            FailedTrendmarkExtractionsWarning("Watermarker.textGetTextWatermarks"),
        )
        val expectedTextWatermarks = listOf(TextWatermark.sized(watermarkString))

        // Act
        val textWatermarks = watermarker.textGetTextWatermarks(textWithWatermarksAndTrendmarks)

        // Assert
        assertTrue(textWatermarks.isWarning)
        assertEquals(expectedStatus.toString(), textWatermarks.toString())
        assertEquals(expectedTextWatermarks, textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_invalidUTF8Watermarks_error() {
        // Arrange
        val expectedExceptionMessage =
            when (platform) {
                Platform.Jvm -> "Input length = 1"
                Platform.Js -> "Malformed sequence starting at 0"
            }
        val expectedStatus = TextWatermark.DecodeToStringError(expectedExceptionMessage).into()

        // Act
        val textWatermarks =
            watermarker.textGetTextWatermarks(
                textWithInvalidUTF8Trendmark,
                errorOnInvalidUTF8 = true,
            )

        // Assert
        assertTrue(textWatermarks.isError)
        assertEquals(expectedStatus.toString(), textWatermarks.toString())
        assertNull(textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_invalidAndValidUTF8Trendmarks_warning() {
        // Arrange
        val expectedExceptionMessage =
            when (platform) {
                Platform.Jvm -> "Input length = 1"
                Platform.Js -> "Malformed sequence starting at 0"
            }
        val expectedStatus = TextWatermark.DecodeToStringError(expectedExceptionMessage).into()
        expectedStatus.addEvent(
            FailedTextWatermarkExtractionsWarning("Watermarker.textGetTextWatermarks"),
            overrideSeverity = true,
        )
        val expectedTextWatermarks =
            listOf(
                TextWatermark.new("0"),
                TextWatermark.sized(watermarkString),
            )

        // Act
        val textWatermarks =
            watermarker.textGetTextWatermarks(
                textWithInvalidandValidUTF8Trendmarks,
                errorOnInvalidUTF8 = true,
            )

        // Assert
        assertTrue(textWatermarks.isWarning)
        assertEquals(expectedStatus.toString(), textWatermarks.toString())
        assertEquals(expectedTextWatermarks, textWatermarks.value)
    }
}
