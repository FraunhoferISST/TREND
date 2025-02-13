/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.trend.watermarker.fileWatermarker.SeparatorStrategy
import de.fraunhofer.isst.trend.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.trend.watermarker.fileWatermarker.TextWatermarkerBuilder
import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultTranscodingTest {
    private val loremIpsum =
        listOf<Byte>(
            0x00, 0x01, 0x0F, 0x41, 0x62, 0xAA.toByte(), 0xF0.toByte(), 0xFE.toByte(),
            0xFF.toByte(), 0x42, 0xef.toByte(), 0xfc.toByte(), 0x2d, 0xe3.toByte(), 0xc9.toByte(),
            0xfa.toByte(), 0x08, 0x87.toByte(), 0x2d,
        )
    private val loremIpsumEncoded =
        listOf<Char>(
            '\u2008', '\u2008', '\u2008', '\u2008', '\u2009', '\u2008', '\u2008', '\u2008',
            '\u205F', '\u205F', '\u2008', '\u2008', '\u2009', '\u2008', '\u2008', '\u2009',
            '\u202F', '\u2008', '\u202F', '\u2009', '\u202F', '\u202F', '\u202F', '\u202F',
            '\u2008', '\u2008', '\u205F', '\u205F', '\u202F', '\u205F', '\u205F', '\u205F',
            '\u205F', '\u205F', '\u205F', '\u205F', '\u202F', '\u2008', '\u2008', '\u2009',
            '\u205F', '\u205F', '\u202F', '\u205F', '\u2008', '\u205F', '\u205F', '\u205F',
            '\u2009', '\u205F', '\u202F', '\u2008', '\u205F', '\u2008', '\u202F', '\u205F',
            '\u2009', '\u202F', '\u2008', '\u205F', '\u202F', '\u202F', '\u205F', '\u205F',
            '\u2008', '\u202F', '\u2008', '\u2008', '\u205F', '\u2009', '\u2008', '\u202F',
            '\u2009', '\u205F', '\u202F', '\u2008',
        )

    @Test
    fun encode_loremIpsum_success() {
        // Act
        val result = DefaultTranscoding.encode(loremIpsum)

        // Assert
        assertEquals(loremIpsumEncoded, result.toList())
    }

    @Test
    fun decode_loremIpsum_success() {
        // Act
        val result = DefaultTranscoding.decode(loremIpsumEncoded.asSequence())

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(loremIpsum, result.value?.toList())
    }
}

class TextWatermarkerTest {
    private val textWatermarker = TextWatermarker.default()
    private val textFileDifferentWatermarks =
        TextFile.fromString(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr," +
                " sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat," +
                " sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum" +
                ".Lorem ipsum dolor sit amet, consetetur sadipscing elitr," +
                " sed diam nonumy eirmod tempor invidunt ut labore et dolore magna",
        )

    @Test
    fun placement_loremIpsum_success() {
        // Arrange
        val text =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
                "volutpat blandit aliquam etiam erat velit."
        val expected =
            sequenceOf(
                5, 11, 17, 21, 27, 39, 50, 56, 60, 63, 71, 78, 89, 92, 99, 102, 109, 115, 123, 131,
                140, 149, 158, 166, 174, 180, 185,
            ).toList()

        // Act
        val result = textWatermarker.placement(text).toList()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun placement_empty_success() {
        // Arrange
        val text = ""
        val expected = sequenceOf<Int>().toList()

        // Act
        val result = textWatermarker.placement(text).toList()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun placement_noSpace_success() {
        // Arrange
        val text =
            "Loremdipsumdolorsitamet,consecteturadipiscingelit,seddoeiusmodtemporincididuntutlabo" +
                "reetdoloremagnaaliqua.Blanditvolutpatmaecenasvolutpatblanditaliquametiameratvelit."
        val expected = sequenceOf<Int>().toList()

        // Act
        val result = textWatermarker.placement(text).toList()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun incompleteWatermarkWarning_string_success() {
        // Arrange
        val error = TextWatermarker.IncompleteWatermarkWarning()
        val expected =
            "Warning (TextWatermarker.getWatermark): Could not restore a complete watermark!"

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun oversizedWatermarkWarning_string_success() {
        // Arrange
        val error = TextWatermarker.OversizedWatermarkWarning(10, 20)
        val expected =
            "Warning (TextWatermarker.addWatermark): Could only insert 20 of 10 bytes from the " +
                "Watermark into the text file."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun containsAlphabetCharsError_string_success() {
        // Arrange
        val error = TextWatermarker.ContainsAlphabetCharsError(sequenceOf('a', 'b'))
        val expected =
            "Error (TextWatermarker.addWatermark): The input contains characters of the watermark" +
                " transcoding alphabet. It is only possible to add a watermark to and input that" +
                " doesn't contain any watermark. Adding another watermark would potentially make " +
                "the input unusable! Maybe the input already contains a watermark?\n" +
                "\n" +
                "Contained Chars:\n" +
                "['\\u0061','\\u0062']."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun success_string_success() {
        // Arrange
        val error = TextWatermarker.Success(listOf(1, 2, 3))
        val expected = "Success: Added Watermark 3 times. Positions: [1, 2, 3]."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun getWatermarks_singleWatermark_Success() {
        // Arrange
        val expectedWatermark = Watermark.fromString("Okay")
        val expected = listOf(expectedWatermark, expectedWatermark)

        // Act
        val result =
            textWatermarker.getWatermarks(
                textFileDifferentWatermarks,
                singleWatermark = true,
            )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_multipleWatermark_Success() {
        // Arrange
        val firstWatermark = Watermark.fromString("Okay")
        val secondWatermark = Watermark.fromString("Test")
        val expected = listOf(firstWatermark, firstWatermark, secondWatermark)

        // Act
        val result =
            textWatermarker.getWatermarks(
                textFileDifferentWatermarks,
                singleWatermark = false,
            )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun removeWatermarksGetProblemWarning_string_success() {
        // Arrange
        val error = TextWatermarker.RemoveWatermarksGetProblemWarning()
        val expected =
            "Warning (TextWatermarker.removeWatermarks): There was a problem extracting the " +
                "watermarks. They got removed anyways."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }
}

class TextWatermarkerBuilderTest {
    @Test
    fun build_alphabetContainsSeparatorChar_error() {
        // Arrange
        val alphabetChar = DefaultTranscoding.alphabet[0]
        val error = TextWatermarkerBuilder.AlphabetContainsSeparatorError(listOf(alphabetChar))
        val expected = error.getMessage()

        // Act
        val result =
            TextWatermarker
                .builder()
                .setSeparatorStrategy(SeparatorStrategy.SingleSeparatorChar(alphabetChar))
                .build()

        // Assert
        assertTrue(result.isError)
        assertEquals(expected, result.getMessage())
        assertNull(result.value)
    }

    @Test
    fun buildSecondStrategy_alphabetContainsSeparatorChar_error() {
        // Arrange
        val alphabetChar1 = DefaultTranscoding.alphabet[0]
        val alphabetChar2 = DefaultTranscoding.alphabet[1]
        val validChar = '\u3164' // hangul filler, chosen at random
        val errorEither =
            TextWatermarkerBuilder.AlphabetContainsSeparatorError(listOf(alphabetChar1))
        val errorBoth =
            TextWatermarkerBuilder.AlphabetContainsSeparatorError(
                listOf(alphabetChar1, alphabetChar2),
            )
        val expectedEither = errorEither.getMessage()
        val expectedBoth = errorBoth.getMessage()

        // Act
        val resultStart =
            TextWatermarker
                .builder()
                .setSeparatorStrategy(
                    SeparatorStrategy.StartEndSeparatorChars(alphabetChar1, validChar),
                )
                .build()
        val resultEnd =
            TextWatermarker
                .builder()
                .setSeparatorStrategy(
                    SeparatorStrategy.StartEndSeparatorChars(validChar, alphabetChar1),
                )
                .build()
        val resultBoth =
            TextWatermarker
                .builder()
                .setSeparatorStrategy(
                    SeparatorStrategy.StartEndSeparatorChars(alphabetChar1, alphabetChar2),
                )
                .build()

        // Assert
        // Start char assertions
        assertTrue(resultStart.isError)
        assertEquals(expectedEither, resultStart.getMessage())
        assertNull(resultStart.value)
        // End char assertions
        assertTrue(resultEnd.isError)
        assertEquals(expectedEither, resultEnd.getMessage())
        assertNull(resultEnd.value)
        // Both char assertions
        assertTrue(resultBoth.isError)
        assertEquals(expectedBoth, resultBoth.getMessage())
        assertNull(resultBoth.value)
    }

    @Test
    fun alphabetContainsSeparatorCharError_string_success() {
        // Arrange
        val error = TextWatermarkerBuilder.AlphabetContainsSeparatorError(listOf('a'))
        val expected =
            "Error (TextWatermarkerBuilder): The alphabet contains separator char(s): ['\\u0061']"

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }
}
