/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.files

import areFilesEqual
import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.trend.watermarker.files.ZipFile
import de.fraunhofer.isst.trend.watermarker.files.ZipFileHeader
import de.fraunhofer.isst.trend.watermarker.files.fromFile
import de.fraunhofer.isst.trend.watermarker.files.writeToFile
import openZipFile
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ZipFileTestJvm {
    @Test
    fun fromFile_validFile_success() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"

        val extraFields =
            mutableListOf(
                ZipFileHeader.ExtraField(
                    0x5455u,
                    listOf<Byte>(
                        0x03, 0x67, 0x1B, 0xDD.toByte(), 0x63, 0x67, 0x1B, 0xDD.toByte(), 0x63,
                    ),
                ),
                ZipFileHeader.ExtraField(
                    0x7875u,
                    listOf<Byte>(
                        0x01, 0x04, 0xE8.toByte(), 0x03, 0x00, 0x00, 0x04, 0xE9.toByte(), 0x03,
                        0x00, 0x00,
                    ),
                ),
            )

        val expectedHeader =
            ZipFileHeader(
                0x04034b50u,
                0x000au,
                0x0000u,
                0x0000u,
                0x7c48u,
                0x5643u,
                0x486e85a5u,
                0x00000002u,
                0x00000002u,
                0x0005u,
                0x0023u,
                0x001cu,
                0x003fu,
                "a.txt",
                extraFields,
            )

        val expectedContent =
            listOf<Byte>(
                0x41, 0x0a, 0x50, 0x4b, 0x03, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x4b, 0x7c,
                0x43, 0x56, 0x66, 0xd6.toByte(), 0x43, 0x63, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00,
                0x00, 0x00, 0x05, 0x00, 0x1c, 0x00, 0x62, 0x2e, 0x74, 0x78, 0x74, 0x55, 0x54, 0x09,
                0x00, 0x03, 0x6d, 0x1b, 0xdd.toByte(), 0x63, 0x6d, 0x1b, 0xdd.toByte(), 0x63, 0x75,
                0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8.toByte(), 0x03, 0x00, 0x00, 0x04, 0xe9.toByte(),
                0x03, 0x00, 0x00, 0x42, 0x0a, 0x50, 0x4b, 0x01, 0x02, 0x1e, 0x03, 0x0a, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x48, 0x7c, 0x43, 0x56, 0xa5.toByte(), 0x85.toByte(), 0x6e, 0x48,
                0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x05, 0x00, 0x18, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0xa4.toByte(), 0x81.toByte(), 0x00, 0x00, 0x00,
                0x00, 0x61, 0x2e, 0x74, 0x78, 0x74, 0x55, 0x54, 0x05, 0x00, 0x03, 0x67, 0x1b,
                0xdd.toByte(), 0x63, 0x75, 0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8.toByte(), 0x03, 0x00,
                0x00, 0x04, 0xe9.toByte(), 0x03, 0x00, 0x00, 0x50, 0x4b, 0x01, 0x02, 0x1e, 0x03,
                0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x4b, 0x7c, 0x43, 0x56, 0x66, 0xd6.toByte(),
                0x43, 0x63, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x05, 0x00, 0x18, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0xa4.toByte(), 0x81.toByte(), 0x41,
                0x00, 0x00, 0x00, 0x62, 0x2e, 0x74, 0x78, 0x74, 0x55, 0x54, 0x05, 0x00, 0x03, 0x6d,
                0x1b, 0xdd.toByte(), 0x63, 0x75, 0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8.toByte(), 0x03,
                0x00, 0x00, 0x04, 0xe9.toByte(), 0x03, 0x00, 0x00, 0x50, 0x4b, 0x05, 0x06, 0x00,
                0x00, 0x00, 0x00, 0x02, 0x00, 0x02, 0x00, 0x96.toByte(), 0x00, 0x00, 0x00,
                0x82.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00,
            )

        val expected = ZipFile(source, expectedHeader, expectedContent)

        // Act
        val result = ZipFile.fromFile(source)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected.toBytes(), result.value?.toBytes())
    }

    @Test
    fun fromFile_emptyWatermark_success() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files_empty_watermark.zip"

        val extraFields =
            mutableListOf(
                ZipFileHeader.ExtraField(
                    0x5455u,
                    listOf<Byte>(
                        0x03, 0x67, 0x1B, 0xDD.toByte(), 0x63, 0x67, 0x1B, 0xDD.toByte(), 0x63,
                    ),
                ),
                ZipFileHeader.ExtraField(
                    0x7875u,
                    listOf<Byte>(
                        0x01, 0x04, 0xE8.toByte(), 0x03, 0x00, 0x00, 0x04, 0xE9.toByte(), 0x03,
                        0x00, 0x00,
                    ),
                ),
                ZipFileHeader.ExtraField(
                    0x8777u,
                    listOf<Byte>(),
                ),
            )

        val expectedHeader =
            ZipFileHeader(
                0x04034b50u,
                0x000au,
                0x0000u,
                0x0000u,
                0x7c48u,
                0x5643u,
                0x486e85a5u,
                0x00000002u,
                0x00000002u,
                0x0005u,
                0x0023u,
                0x0020u,
                0x003fu,
                "a.txt",
                extraFields,
            )

        val expectedContent =
            listOf<Byte>(
                0x41, 0x0a, 0x50, 0x4b, 0x03, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x4b, 0x7c,
                0x43, 0x56, 0x66, 0xd6.toByte(), 0x43, 0x63, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00,
                0x00, 0x00, 0x05, 0x00, 0x1c, 0x00, 0x62, 0x2e, 0x74, 0x78, 0x74, 0x55, 0x54, 0x09,
                0x00, 0x03, 0x6d, 0x1b, 0xdd.toByte(), 0x63, 0x6d, 0x1b, 0xdd.toByte(), 0x63, 0x75,
                0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8.toByte(), 0x03, 0x00, 0x00, 0x04, 0xe9.toByte(),
                0x03, 0x00, 0x00, 0x42, 0x0a, 0x50, 0x4b, 0x01, 0x02, 0x1e, 0x03, 0x0a, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x48, 0x7c, 0x43, 0x56, 0xa5.toByte(), 0x85.toByte(), 0x6e, 0x48,
                0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x05, 0x00, 0x18, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0xa4.toByte(), 0x81.toByte(), 0x00, 0x00, 0x00,
                0x00, 0x61, 0x2e, 0x74, 0x78, 0x74, 0x55, 0x54, 0x05, 0x00, 0x03, 0x67, 0x1b,
                0xdd.toByte(), 0x63, 0x75, 0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8.toByte(), 0x03, 0x00,
                0x00, 0x04, 0xe9.toByte(), 0x03, 0x00, 0x00, 0x50, 0x4b, 0x01, 0x02, 0x1e, 0x03,
                0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x4b, 0x7c, 0x43, 0x56, 0x66, 0xd6.toByte(),
                0x43, 0x63, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x05, 0x00, 0x18, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0xa4.toByte(), 0x81.toByte(), 0x41,
                0x00, 0x00, 0x00, 0x62, 0x2e, 0x74, 0x78, 0x74, 0x55, 0x54, 0x05, 0x00, 0x03, 0x6d,
                0x1b, 0xdd.toByte(), 0x63, 0x75, 0x78, 0x0b, 0x00, 0x01, 0x04, 0xe8.toByte(), 0x03,
                0x00, 0x00, 0x04, 0xe9.toByte(), 0x03, 0x00, 0x00, 0x50, 0x4b, 0x05, 0x06, 0x00,
                0x00, 0x00, 0x00, 0x02, 0x00, 0x02, 0x00, 0x96.toByte(), 0x00, 0x00, 0x00,
                0x82.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00,
            )

        val expected = ZipFile(source, expectedHeader, expectedContent)

        // Act
        val result = ZipFile.fromFile(source)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected.toBytes(), result.value?.toBytes())
    }

    @Test
    fun fromFile_textFile_error() {
        // Arrange
        val path = "src/jvmTest/resources/lorem_ipsum.txt"
        val expectedMessage = ZipFileHeader.InvalidMagicBytesError().into().toString()

        // Act
        val result = ZipFile.fromFile(path)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun fromFile_cutOff_error() {
        // Arrange
        val path = "src/jvmTest/resources/cut_off.zip"
        val expectedMessage = ZipFileHeader.NotEnoughBytesError().into().toString()

        // Act
        val result = ZipFile.fromFile(path)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun fromFile_extraFieldLengthTooLong_error() {
        // Arrange
        val path = "src/jvmTest/resources/extra_field_length_too_long.zip"
        val expectedMessage = ZipFileHeader.ExtraField.NotEnoughBytesError().into().toString()

        // Act
        val result = ZipFile.fromFile(path)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun fromFile_invalidPath_error() {
        // Arrange
        val source = ""
        val expectedMessage =
            WatermarkableFile.ReadError(source, " (No such file or directory)").into().toString()

        // Act
        val result = TextFile.fromFile(source)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun writeToFile_validPath_success() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val target = "src/jvmTest/resources/multiple_files_test.zip"
        val zipFile = openZipFile(source)

        // Act
        val result = zipFile.writeToFile(target)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(areFilesEqual(source, target))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun writeToFile_invalidPath_error() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val target = ""
        val expectedMessage =
            WatermarkableFile.WriteError(target, " (No such file or directory)").into().toString()
        val zipFile = openZipFile(source)

        // Act
        val result = zipFile.writeToFile(target)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun addExtraField_watermark_success() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")
        val content =
            listOf<Byte>(
                0x4c, 0x6f, 0x72, 0x65, 0x6d, 0x20, 0x69, 0x70, 0x73, 0x75, 0x6d, 0x20, 0x64, 0x6f,
                0x6c, 0x6f, 0x72, 0x20, 0x73, 0x69, 0x74, 0x20, 0x61, 0x6d, 0x65, 0x74,
            )
        val expected = openZipFile("src/jvmTest/resources/multiple_files_watermarked.zip")

        // Act
        val status = file.header.addExtraField(0x8777u, content)

        // Assert
        assertTrue(status.isSuccess)
        assertEquals(expected, file)
    }

    @Test
    fun addExtraField_oversizedContent_error() {
        // Arrange
        val file = openZipFile("src/jvmTest/resources/multiple_files.zip")
        val content = List<Byte>(UShort.MAX_VALUE.toInt()) { 0 }
        val expectedMessage = ZipFileHeader.ExtraField.OversizedHeaderError(65567).into().toString()

        // Act
        val status = file.header.addExtraField(0u, content)

        // Assert
        assertTrue(status.isError)
        assertEquals(expectedMessage, status.toString())
    }

    @Test
    fun equals_sameFile_true() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val file1 = openZipFile(source)
        val file2 = openZipFile(source)

        // Act
        val result = file1 == file2

        // Assert
        assertTrue(result)
    }

    @Test
    fun equals_differentZipFiles_false() {
        // Arrange
        val source1 = "src/jvmTest/resources/multiple_files.zip"
        val source2 = "src/jvmTest/resources/multiple_files_watermarked.zip"
        val file1 = openZipFile(source1)
        val file2 = openZipFile(source2)

        // Act
        val result = file1 == file2

        // Assert
        assertFalse(result)
    }

    @Test
    fun equals_differentObject_false() {
        // Arrange
        val source = "src/jvmTest/resources/multiple_files.zip"
        val file = openZipFile(source)
        val obj = 0

        // Act
        val result = file.equals(obj)

        // Assert
        assertFalse(result)
    }
}
