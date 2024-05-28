/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest

import areFilesEqual
import de.fraunhofer.isst.trend.watermarker.JvmWatermarker
import de.fraunhofer.isst.trend.watermarker.SupportedFileType
import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.trend.watermarker.files.ZipFileHeader
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmWatermarkerTest {
    private val watermarker = JvmWatermarker()

    @Test
    fun addWatermark_zipAllValid_success() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val watermark = Watermark("Lorem ipsum dolor sit amet".encodeToByteArray().asList())
        val expected = "src/jvmTest/resources/multiple_files_watermarked.zip"

        // Act
        val result =
            watermarker.addWatermark(
                source,
                target,
                watermark,
            )

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(areFilesEqual(target, expected))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun addWatermark_txtAllValid_success() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val target = "src/jvmTest/resources/lorem_ipsum_test.txt"
        val watermark = Watermark.fromString("Hello World")
        val expected = "src/jvmTest/resources/lorem_ipsum_watermarked.txt"

        // Act
        val result =
            watermarker.addWatermark(
                source,
                target,
                watermark,
            )

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(areFilesEqual(target, expected))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun addWatermark_wrongFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val target = "src/jvmTest/resources/lorem_ipsum_test.txt"
        val watermark = Watermark("Lorem ipsum dolor sit amet".encodeToByteArray().asList())
        val fileType = "zip"
        val expectedMessage = ZipFileHeader.InvalidMagicBytesError().into().toString()

        // Act
        val result =
            watermarker.addWatermark(
                source,
                target,
                watermark,
                fileType,
            )

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addWatermark_unsupportedFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.unsupported"
        val target = "src/jvmTest/resources/lorem_ipsum_test.unsupported"
        val watermark = Watermark("Hello".encodeToByteArray().asList())
        val expectedMessage =
            SupportedFileType.UnsupportedTypeError("unsupported").into().toString()

        // Act
        val result = watermarker.addWatermark(source, target, watermark)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addWatermark_noFileType_error() {
        // Arrange
        val source = ""
        val target = "src/jvmTest/resources/lorem_ipsum_test.unsupported"
        val watermark = Watermark("Hello".encodeToByteArray().asList())
        val expectedMessage = SupportedFileType.NoFileTypeError(source).into().toString()

        // Act
        val result = watermarker.addWatermark(source, target, watermark)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addWatermark_invalidSource_error() {
        // Arrange
        val source = ""
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val fileType = "zip"
        val watermark = Watermark("Hello".encodeToByteArray().asList())
        val expectedMessage =
            WatermarkableFile.ReadError(source, " (No such file or directory)").into().toString()

        // Act
        val result = watermarker.addWatermark(source, target, watermark, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addWatermark_invalidTarget_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val target = ""
        val watermark = Watermark("Lorem ipsum dolor sit amet".encodeToByteArray().asList())
        val expectedMessage =
            WatermarkableFile.WriteError(
                target,
                " (No such file or directory)",
            ).into().toString()

        // Act
        val result = watermarker.addWatermark(source, target, watermark)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun containsWatermark_zipWatermark_successAndTrue() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"

        // Act
        val result = watermarker.containsWatermark(source)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value == true)
    }

    @Test
    fun containsWatermark_zipNoWatermark_successAndFalse() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"

        // Act
        val result = watermarker.containsWatermark(source)

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.value != false)
    }

    @Test
    fun containsWatermark_txtWatermark_successAndTrue() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum_watermarked.txt"

        // Act
        val result = watermarker.containsWatermark(source)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value == true)
    }

    @Test
    fun containsWatermark_txtNoWatermark_successAndFalse() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"

        // Act
        val result = watermarker.containsWatermark(source)

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.value != false)
    }

    @Test
    fun containsWatermark_wrongFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val fileType = "txt"
        val expectedMessage = TextFile.InvalidByteError().into().toString()

        // Act
        val result = watermarker.containsWatermark(source, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun containsWatermark_unsupportedFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val fileType = "unsupported"
        val expectedMessage = SupportedFileType.UnsupportedTypeError(fileType).into().toString()

        // Act
        val result = watermarker.containsWatermark(source, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun containsWatermark_noFileType_error() {
        // Arrange
        val source = ""
        val expectedMessage = SupportedFileType.NoFileTypeError(source).into().toString()

        // Act
        val result = watermarker.containsWatermark(source)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun containsWatermark_invalidSource_error() {
        // Arrange
        val source = ""
        val fileType = "zip"
        val expectedMessage =
            WatermarkableFile.ReadError(source, " (No such file or directory)")
                .into().toString()

        // Act
        val result = watermarker.containsWatermark(source, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun getWatermarks_zipWatermark_successAndWatermark() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val expected =
            listOf(
                Watermark.fromString("Lorem ipsum dolor sit amet"),
            )

        // Act
        val result = watermarker.getWatermarks(source)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_zipNoWatermark_successAndEmptyList() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"

        // Act
        val result = watermarker.getWatermarks(source)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
    }

    @Test
    fun getWatermarks_txtWatermark_successAndWatermarks() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum_watermarked.txt"
        val expected =
            listOf(
                Watermark.fromString("Hello World"),
            )

        // Act
        val result = watermarker.getWatermarks(source)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_differentWatermarks_successAndNotSquashedWatermarks() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum_long_different_watermarks.txt"
        val expected =
            listOf("Test", "Okay", "Okay", "Okay", "Okay", "Okay").map {
                Watermark.fromString(it)
            }

        // Act
        val result = watermarker.getWatermarks(source, null, false)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_differentWatermarks_successAndWatermarks() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum_long_different_watermarks.txt"
        val expected =
            listOf("Test", "Okay").map {
                Watermark.fromString(it)
            }

        // Act
        val result = watermarker.getWatermarks(source)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun getWatermarks_txtNoWatermark_successAndEmptyList() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"

        // Act
        val result = watermarker.getWatermarks(source)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
    }

    @Test
    fun getWatermarks_wrongFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val fileType = "txt"
        val expectedMessage = TextFile.InvalidByteError().into().toString()

        // Act
        val result = watermarker.getWatermarks(source, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun getWatermarks_unsupportedFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val fileType = "unsupported"
        val expectedMessage = SupportedFileType.UnsupportedTypeError(fileType).into().toString()

        // Act
        val result = watermarker.getWatermarks(source, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun getWatermarks_noFileType_error() {
        // Arrange
        val source = ""
        val expectedMessage = SupportedFileType.NoFileTypeError(source).into().toString()

        // Act
        val result = watermarker.getWatermarks(source)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun getWatermarks_invalidSource_error() {
        // Arrange
        val source = ""
        val fileType = "zip"
        val expectedMessage =
            WatermarkableFile.ReadError(source, " (No such file or directory)")
                .into().toString()

        // Act
        val result = watermarker.getWatermarks(source, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun removeWatermarks_zipWatermark_successAndWatermark() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val expected = "src/jvmTest/resources/multiple_files.zip"
        val expectedWatermarks =
            listOf(
                Watermark("Lorem ipsum dolor sit amet".encodeToByteArray().asList()),
            )

        // Act
        val result = watermarker.removeWatermarks(source, target)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedWatermarks, result.value)
        assertTrue(areFilesEqual(expected, target))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun removeWatermarks_zipNoWatermark_successAndEmptyList() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val target = "src/jvmTest/resources/multiple_files_test.zip"

        // Act
        val result = watermarker.removeWatermarks(source, target)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
        assertTrue(areFilesEqual(source, target))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun removeWatermarks_txtWatermark_successAndWatermarks() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum_watermarked.txt"
        val target = "src/jvmTest/resources/lorem_ipsum_test.txt"
        val expected = "src/jvmTest/resources/lorem_ipsum.txt"
        val expectedWatermarks =
            listOf(
                Watermark.fromString("Hello World"),
                Watermark.fromString("Hello World"),
            )

        // Act
        val result = watermarker.removeWatermarks(source, target)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedWatermarks, result.value)
        assertTrue(areFilesEqual(expected, target))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun removeWatermarks_txtNoWatermark_successAndEmptyList() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val target = "src/jvmTest/resources/lorem_ipsum_test.txt"

        // Act
        val result = watermarker.removeWatermarks(source, target)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.value?.isEmpty() == true)
        assertTrue(areFilesEqual(source, target))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun removeWatermarks_wrongFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val target = "src/jvmTest/resources/multiple_files_watermarked_test.zip"
        val fileType = "txt"
        val expectedMessage = TextFile.InvalidByteError().into().toString()

        // Act
        val result = watermarker.removeWatermarks(source, target, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun removeWatermarks_unsupportedFileType_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val fileType = "unsupported"
        val expectedMessage = SupportedFileType.UnsupportedTypeError(fileType).into().toString()

        // Act
        val result = watermarker.removeWatermarks(source, target, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun removeWatermarks_noFileType_error() {
        // Arrange
        val source = ""
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val expectedMessage = SupportedFileType.NoFileTypeError(source).into().toString()

        // Act
        val result = watermarker.removeWatermarks(source, target)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun removeWatermarks_invalidSource_error() {
        // Arrange
        val source = ""
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val fileType = "zip"
        val expectedMessage =
            WatermarkableFile.ReadError(source, " (No such file or directory)")
                .into().toString()

        // Act
        val result = watermarker.removeWatermarks(source, target, fileType)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }
}
