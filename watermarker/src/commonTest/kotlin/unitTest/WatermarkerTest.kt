/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest

import Platform
import de.fraunhofer.isst.innamark.watermarker.SupportedFileType
import de.fraunhofer.isst.innamark.watermarker.Watermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.watermarks.Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.RawInnamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedInnamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark.FailedTextWatermarkExtractionsWarning
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
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

    private val textWithInnamarks =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor i" +
            "nvidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et a" +
            "ccusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
            "sanctus est Lorem ipsum dolor sit amet."

    private val differentInnamarks = listOf("Test", "Okay", "Okay", "Yeah")

    private val textWithDifferentRawInnamarks =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempo" +
            "r invidunt ut labore et dolore magna. Lorem ipsum dolor sitLorem ipsum dolor sit " +
            "amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labo" +
            "re et dolore magna. Lorem ipsum dolor sitLorem ipsum dolor sit amet, consetetur s" +
            "adipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna" +
            ". Lorem ipsum dolor sitLorem ipsum dolor sit amet, consetetur sadipscing elitr, s" +
            "ed diam nonumy eirmod tempor invidunt ut labore et dolore magna. Lorem ipsum dolo" +
            "r sit"

    private val differentTextWatermarks = listOf("Test", "Okay", "Okay", "Yeah")

    private val textWithDifferentTextWatermarks =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempo" +
            "r invidunt ut labore et dolore magna. Lorem ipsum dolor sitLorem ipsum dolor sit " +
            "amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labo" +
            "re et dolore magna. Lorem ipsum dolor sitLorem ipsum dolor sit amet, consetetur s" +
            "adipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna" +
            ". Lorem ipsum dolor sitLorem ipsum dolor sit amet, consetetur sadipscing elitr, s" +
            "ed diam nonumy eirmod tempor invidunt ut labore et dolore magna. Lorem ipsum dolo" +
            "r sit"
    private val textWithWatermarksAndInnamarks =
        textWithWatermark +
            DefaultTranscoding.SEPARATOR_CHAR +
            textWithInnamarks

    private val textWithInvalidUTF8Innamark =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor i" +
            "nvidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et a" +
            "ccusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata " +
            "sanctus est Lorem ipsum dolor sit amet."

    private val textWithInvalidandValidUTF8Innamarks =
        textWithInvalidUTF8Innamark +
            DefaultTranscoding.SEPARATOR_CHAR +
            textWithInnamarks

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
        val expectedMessage = TextWatermarker.OversizedWatermarkWarning(54, 49).into().toString()
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
    fun textGetWatermarks_exactlyOneWatermark_warningAndWatermark() {
        // Arrange
        val expected =
            listOf(
                Watermark(listOf(0, 97)),
            )

        // Act
        val result = watermarker.textGetWatermarks("a a a a a a a a a a")

        // Assert
        assertTrue(result.isWarning)
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
        val result =
            watermarker.textGetWatermarks(
                longTextWithDifferentWatermarks,
                squash = false,
                singleWatermark = false,
            )

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
        val result =
            watermarker.textGetWatermarks(
                longTextWithDifferentWatermarks,
                singleWatermark = false,
            )

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
    fun textGetWatermarks_SingleWatermark_Success() {
        // Arrange

        // Act
        val result =
            watermarker.textGetWatermarks(
                longTextWithDifferentWatermarks,
                squash = false,
                singleWatermark = true,
            )
        val expected = differentWatermarks.drop(1).map { Watermark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_SingleWatermark_SuccessAndSquash() {
        // Arrange

        // Act
        val result =
            watermarker.textGetWatermarks(
                longTextWithDifferentWatermarks,
                squash = true,
                singleWatermark = true,
            )
        val expected = differentWatermarks.takeLast(1).map { Watermark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_MultipleWatermark_Success() {
        // Arrange

        // Act
        val result =
            watermarker.textGetWatermarks(
                longTextWithDifferentWatermarks,
                squash = false,
                singleWatermark = false,
            )
        val expected = differentWatermarks.map { Watermark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetWatermarks_MultipleWatermark_SuccessAndSquash() {
        // Arrange

        // Act
        val result =
            watermarker.textGetWatermarks(
                longTextWithDifferentWatermarks,
                squash = true,
                singleWatermark = false,
            )
        val expected = differentWatermarks.distinct().map { Watermark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
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
    fun textGetInnamarks_innamarkedString_success() {
        // Arrange
        val expectedInnamarks = listOf(SizedInnamark.fromString(watermarkString))

        // Act
        val innamarks = watermarker.textGetInnamarks(textWithInnamarks)

        // Assert
        assertTrue(innamarks.isSuccess)
        assertEquals(expectedInnamarks, innamarks.value)
    }

    @Test
    fun textGetInnamarks_watermarkedString_error() {
        // Arrange
        val expectedError = Innamark.UnknownTagError(0x54u).into()

        // Act
        val innamarks = watermarker.textGetInnamarks(textWithWatermark)

        // Assert
        assertTrue(innamarks.isError)
        assertEquals(expectedError.toString(), innamarks.toString())
        assertNull(innamarks.value)
    }

    @Test
    fun textGetInnamarks_watermarkedAndInnamarkedString_warning() {
        // Arrange
        val unkownTagError = Innamark.UnknownTagError(0x54u)
        val expectedStatus = unkownTagError.into()
        expectedStatus.addEvent(unkownTagError)
        expectedStatus.addEvent(
            Innamark.FailedInnamarkExtractionsWarning
                ("Watermarker.textGetInnamarks"),
        )
        val expectedInnamarks = listOf(SizedInnamark.fromString(watermarkString))

        // Act
        val innamarks =
            watermarker.textGetInnamarks(
                textWithWatermarksAndInnamarks,
                singleWatermark = false,
            )

        // Assert
        assertTrue(innamarks.isWarning)
        assertEquals(expectedStatus.toString(), innamarks.toString())
        assertEquals(expectedInnamarks, innamarks.value)
    }

    @Test
    fun textGetInnamarks_SingleWatermark_Success() {
        // Arrange

        // Act
        val result =
            watermarker.textGetInnamarks(
                textWithDifferentRawInnamarks,
                squash = false,
                singleWatermark = true,
            )
        val expected = differentInnamarks.drop(1).dropLast(1).map { RawInnamark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetInnamarks_SingleWatermark_SuccessAndSquash() {
        // Arrange

        // Act
        val result =
            watermarker.textGetInnamarks(
                textWithDifferentRawInnamarks,
                squash = true,
                singleWatermark = true,
            )
        val expected = differentInnamarks.drop(1).take(1).map { RawInnamark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetInnamarks_MultipleWatermark_Success() {
        // Arrange

        // Act
        val result =
            watermarker.textGetInnamarks(
                textWithDifferentRawInnamarks,
                squash = false,
                singleWatermark = false,
            )
        val expected = differentInnamarks.map { RawInnamark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetInnamarks_MultipleWatermark_SuccessAndSquash() {
        // Arrange

        // Act
        val result =
            watermarker.textGetInnamarks(
                textWithDifferentRawInnamarks,
                squash = true,
                singleWatermark = false,
            )
        val expected = differentInnamarks.distinct().map { RawInnamark.fromString(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetTextWatermarks_innamarkedString_success() {
        // Arrange
        val expectedTextWatermark = TextWatermark.sized(watermarkString)
        val expectedTextWatermarks = listOf(expectedTextWatermark)

        // Act
        val textWatermarks = watermarker.textGetTextWatermarks(textWithInnamarks)

        // Assert
        assertTrue(textWatermarks.isSuccess)
        assertEquals(expectedTextWatermarks, textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_watermarkedString_error() {
        // Arrange
        val expectedError = Innamark.UnknownTagError(0x54u).into()

        // Act
        val textWatermarks = watermarker.textGetTextWatermarks(textWithWatermark)

        // Assert
        assertTrue(textWatermarks.isError)
        assertEquals(expectedError.toString(), textWatermarks.toString())
        assertNull(textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_watermarkedAndInnamarkedString_warning() {
        // Arrange
        val unknownTagError = Innamark.UnknownTagError(0x54u)
        val expectedStatus = unknownTagError.into()
        expectedStatus.addEvent(unknownTagError)
        expectedStatus.addEvent(
            Innamark.FailedInnamarkExtractionsWarning("Watermarker.textGetTextWatermarks"),
        )
        val expectedTextWatermarks = listOf(TextWatermark.sized(watermarkString))

        // Act
        val textWatermarks =
            watermarker.textGetTextWatermarks(
                textWithWatermarksAndInnamarks,
                singleWatermark = false,
            )

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
                textWithInvalidUTF8Innamark,
                errorOnInvalidUTF8 = true,
            )

        // Assert
        assertTrue(textWatermarks.isError)
        assertEquals(expectedStatus.toString(), textWatermarks.toString())
        assertNull(textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_invalidAndValidUTF8Innamarks_warning() {
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
                textWithInvalidandValidUTF8Innamarks,
                singleWatermark = false,
                errorOnInvalidUTF8 = true,
            )

        // Assert
        assertTrue(textWatermarks.isWarning)
        assertEquals(expectedStatus.toString(), textWatermarks.toString())
        assertEquals(expectedTextWatermarks, textWatermarks.value)
    }

    @Test
    fun textGetTextWatermarks_SingleWatermark_Success() {
        // Arrange

        // Act
        val result =
            watermarker.textGetTextWatermarks(
                textWithDifferentTextWatermarks,
                squash = false,
                singleWatermark = true,
            )
        val expected = differentTextWatermarks.drop(1).dropLast(1).map { TextWatermark.new(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetTextWatermarks_SingleWatermark_SuccessAndSquash() {
        // Arrange

        // Act
        val result =
            watermarker.textGetTextWatermarks(
                textWithDifferentTextWatermarks,
                squash = true,
                singleWatermark = true,
            )
        val expected = differentTextWatermarks.drop(1).take(1).map { TextWatermark.new(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetTextWatermarks_MultipleWatermark_Success() {
        // Arrange

        // Act
        val result =
            watermarker.textGetTextWatermarks(
                textWithDifferentTextWatermarks,
                squash = false,
                singleWatermark = false,
            )
        val expected = differentTextWatermarks.map { TextWatermark.new(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun textGetTextWatermarks_MultipleWatermark_SuccessAndSquash() {
        // Arrange

        // Act
        val result =
            watermarker.textGetTextWatermarks(
                textWithDifferentTextWatermarks,
                squash = true,
                singleWatermark = false,
            )
        val expected = differentTextWatermarks.distinct().map { TextWatermark.new(it) }

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }
}
