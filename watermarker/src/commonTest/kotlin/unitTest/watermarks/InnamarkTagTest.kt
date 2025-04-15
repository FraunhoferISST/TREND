/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.innamark.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.CRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedCRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedRawInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedCRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedSHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTagInterface
import de.fraunhofer.isst.innamark.watermarker.watermarks.RawInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SHA3256InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedCRC32InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedInnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedSHA3256InnamarkTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InnamarkTagTest {
    private val content = "Lorem Ipsum".encodeToByteArray().asList()
    private val compressedContent =
        listOf<Byte>(-13, -55, 47, 74, -51, 85, -16, 44, 40, 46, -51, 5, 0)

    @Test
    fun rawInnamark_creation_success() {
        // Arrange
        val expected = listOf(RawInnamarkTag.TYPE_TAG.toByte()) + content

        // Act
        val watermark = RawInnamarkTag.new(content)
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun rawInnamark_invalidTag_error() {
        // Arrange
        val watermarkContent = listOf((-1).toByte()) + content
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.RawInnamarkTag",
                0u,
                255u,
            ).into().toString()

        // Act
        val watermark = RawInnamarkTag(watermarkContent)
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedRawInnamark_creation_success() {
        // Arrange
        val expected = listOf(CompressedRawInnamarkTag.TYPE_TAG.toByte()) + compressedContent

        // Act
        val watermark = CompressedRawInnamarkTag.new(content)
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedRawInnamark_invalidTag_error() {
        // Arrange
        val watermarkContent = listOf((-1).toByte()) + compressedContent
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CompressedRawInnamarkTag",
                64u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedRawInnamarkTag(watermarkContent)
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedInnamark_creation_success() {
        // Arrange
        val expectedSize =
            (InnamarkTagInterface.TAG_SIZE + SizedInnamarkTag.SIZE_SIZE + content.size).toUInt()
        val expected =
            listOf(SizedInnamarkTag.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedInnamarkTag.new(content)
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sizedInnamark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (InnamarkTagInterface.TAG_SIZE + SizedInnamarkTag.SIZE_SIZE + content.size).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                content
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.SizedInnamarkTag",
                32u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedInnamarkTag(watermarkContent)
        val extractedSize = watermark.extractSize()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedInnamark_mismatchedSize_warning() {
        // Arrange
        val invalidSize =
            (InnamarkTagInterface.TAG_SIZE + SizedInnamarkTag.SIZE_SIZE + content.size + 1).toUInt()
        val watermarkContent =
            listOf(SizedInnamarkTag.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                content
        val expectedStatus =
            InnamarkTag.MismatchedSizeWarning(
                "InnamarkTag.SizedInnamarkTag",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = SizedInnamarkTag(watermarkContent)
        val extractedSize = watermark.extractSize()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(invalidSize, extractedSize.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedSizedInnamark_creation_success() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedInnamarkTag.SIZE_SIZE +
                    compressedContent.size
            ).toUInt()
        val expected =
            listOf(CompressedSizedInnamarkTag.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedSizedInnamarkTag.new(content)
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedSizedInnamark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedInnamarkTag.SIZE_SIZE +
                    compressedContent.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CompressedSizedInnamarkTag",
                96u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedSizedInnamarkTag(watermarkContent)
        val extractedSize = watermark.extractSize()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedSizedInnamark_mismatchedSize_warning() {
        // Arrange
        val invalidSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedInnamarkTag.SIZE_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(CompressedSizedInnamarkTag.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            InnamarkTag.MismatchedSizeWarning(
                "InnamarkTag.CompressedSizedInnamarkTag",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = CompressedSizedInnamarkTag(watermarkContent)
        val extractedSize = watermark.extractSize()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(invalidSize, extractedSize.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun crc32Innamark_creation_success() {
        // Arrange
        val expectedCrc32 = 0x5491107Au
        val expected =
            listOf(CRC32InnamarkTag.TYPE_TAG.toByte()) + expectedCrc32.toBytesLittleEndian() +
                content

        // Act
        val watermark = CRC32InnamarkTag.new(content)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun crc32Innamark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0xBFC71733u
        val watermarkContent =
            listOf((-1).toByte()) + expectedCrc32.toBytesLittleEndian() + content
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CRC32InnamarkTag",
                16u,
                255u,
            ).into().toString()

        // Act
        val watermark = CRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun crc32Innamark_invalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0x5491107Au
        val invalidCrc32 = 0xFFFFFFFFu
        val watermarkContent =
            listOf(CRC32InnamarkTag.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            InnamarkTag.InvalidChecksumWarning(
                "InnamarkTag.CRC32InnamarkTag",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(invalidCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun compressedCRC32Innamark_creation_success() {
        // Arrange
        val expectedCrc32 = 0x10927326u
        val expected =
            listOf(CompressedCRC32InnamarkTag.TYPE_TAG.toByte()) +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedCRC32InnamarkTag.new(content)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedCRC32Innamark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0x15C089FFu
        val watermarkContent =
            listOf((-1).toByte()) + expectedCrc32.toBytesLittleEndian() + compressedContent
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CompressedCRC32InnamarkTag",
                80u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedCRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedCRC32Innamark_invalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0x10927326u
        val invalidCrc32 = 0xFFFFFFFFu
        val watermarkContent =
            listOf(CompressedCRC32InnamarkTag.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            InnamarkTag.InvalidChecksumWarning(
                "InnamarkTag.CompressedCRC32InnamarkTag",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CompressedCRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(invalidCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun sizedCRC32Innamark_creation_success() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedCRC32InnamarkTag.SIZE_SIZE +
                    SizedCRC32InnamarkTag.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val expectedCrc32 = 0x51C833FAu
        val expected =
            listOf(SizedCRC32InnamarkTag.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedCRC32InnamarkTag.new(content)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sizedCRC32Innamark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0x735EA3E1u
        val size =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedCRC32InnamarkTag.SIZE_SIZE +
                    SizedCRC32InnamarkTag.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.SizedCRC32InnamarkTag",
                48u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedCRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedCRC32Trendmark_mismatchedSizeInvalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0x862AB3A2u
        val invalidCrc32 = 0xFFFFFFFFu
        val invalidSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedCRC32InnamarkTag.SIZE_SIZE +
                    SizedCRC32InnamarkTag.CHECKSUM_SIZE +
                    content.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(SizedCRC32InnamarkTag.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            InnamarkTag.MismatchedSizeWarning(
                "InnamarkTag.SizedCRC32InnamarkTag",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            InnamarkTag.InvalidChecksumWarning(
                "InnamarkTag.SizedCRC32InnamarkTag",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = SizedCRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(invalidCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun compressedSizedCRC32Innamark_creation_success() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedCRC32InnamarkTag.SIZE_SIZE +
                    SizedCRC32InnamarkTag.CHECKSUM_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedCrc32 = 0x1D71CD9Cu
        val expected =
            listOf(CompressedSizedCRC32InnamarkTag.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedSizedCRC32InnamarkTag.new(content)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedSizedCRC32Innamark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0x8E069413u
        val size =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedCRC32InnamarkTag.SIZE_SIZE +
                    SizedCRC32InnamarkTag.CHECKSUM_SIZE +
                    compressedContent.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CompressedSizedCRC32InnamarkTag",
                112u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedSizedCRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(expectedCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedSizedCRC32Innamark_mismatchedSizeInvalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0x82AB4E02u
        val invalidCrc32 = 0xFFFFFFFFu
        val invalidSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedCRC32InnamarkTag.SIZE_SIZE +
                    SizedCRC32InnamarkTag.CHECKSUM_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(CompressedSizedCRC32InnamarkTag.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            InnamarkTag.MismatchedSizeWarning(
                "InnamarkTag.CompressedSizedCRC32InnamarkTag",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            InnamarkTag.InvalidChecksumWarning(
                "InnamarkTag.CompressedSizedCRC32InnamarkTag",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = CompressedSizedCRC32InnamarkTag(watermarkContent)
        val extractedChecksum = watermark.extractChecksum()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedChecksum.isSuccess)
        assertEquals(invalidCrc32, extractedChecksum.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun sha3256Innamark_creation_success() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                100, 2, -123, -78, 123, 0, 73, 0, -117, 5, -12, -11, -83, 82, -2, -34, 24, 19,
                -76, 61, 47, 113, 121, -36, -76, 56, -100, -96, 28, 21, -66, -80,
            )
        val expectedContent =
            listOf(SHA3256InnamarkTag.TYPE_TAG.toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256InnamarkTag.new(content)
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(expectedContent, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sha3256Innamark_invalidTag_error() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                94, -5, 11, -28, 39, 90, -127, 48, 78, -60, -46, 88, 37, 14, -58, -35, 9, -124,
                -49, -76, -26, 59, -96, -35, -99, 98, 110, -6, -49, 47, -52, 25,
            )
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.SHA3256InnamarkTag",
                8u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256InnamarkTag(watermarkContent)
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sha3256Trendmark_invalidHash_warning() {
        // Arrange
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                100, 2, -123, -78, 123, 0, 73, 0, -117, 5, -12, -11, -83, 82, -2, -34, 24, 19,
                -76, 61, 47, 113, 121, -36, -76, 56, -100, -96, 28, 21, -66, -80,
            )
        val expectedStatus =
            InnamarkTag.InvalidHashWarning(
                "InnamarkTag.SHA3256InnamarkTag",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent = listOf(SHA3256InnamarkTag.TYPE_TAG.toByte()) + invalidHash + content

        // Act
        val watermark = SHA3256InnamarkTag(watermarkContent)
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(invalidHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun compressedSHA3256Innamark_creation_success() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                83, -74, -63, -41, 15, 9, 90, -55, -72, 74, 82, -63, -76, -123, 72, -16, -42, 9,
                -120, -86, 127, 120, -35, -3, 84, -62, 33, -33, -113, -97, -79, 41,
            )
        val expectedContent =
            listOf(CompressedSHA3256InnamarkTag.TYPE_TAG.toByte()) + expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSHA3256InnamarkTag.new(content)
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(expectedContent, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedSHA3256Innamark_invalidTag_error() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                55, 41, 25, 53, -104, 106, 100, -32, 42, 123, -87, 37, -47, 105, -71, 24, -48, -43,
                54, 84, 69, -92, -70, -57, -61, -120, 113, 124, 119, -44, -38, 92,
            )
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CompressedSHA3256InnamarkTag",
                72u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + compressedContent

        // Act
        val watermark = CompressedSHA3256InnamarkTag(watermarkContent)
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedSHA3256Innamark_invalidHash_warning() {
        // Arrange
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                83, -74, -63, -41, 15, 9, 90, -55, -72, 74, 82, -63, -76, -123, 72, -16, -42, 9,
                -120, -86, 127, 120, -35, -3, 84, -62, 33, -33, -113, -97, -79, 41,
            )
        val expectedStatus =
            InnamarkTag.InvalidHashWarning(
                "InnamarkTag.CompressedSHA3256InnamarkTag",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent =
            listOf(CompressedSHA3256InnamarkTag.TYPE_TAG.toByte()) +
                invalidHash +
                compressedContent

        // Act
        val watermark = CompressedSHA3256InnamarkTag(watermarkContent)
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(invalidHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun sizedSHA3256Innamark_creation_success() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedSHA3256InnamarkTag.SIZE_SIZE +
                    SizedSHA3256InnamarkTag.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                16, 40, 37, 39, 11, -23, 67, 16, -53, -112, -19, 39, -109, 30, 9, -53, -26, 19,
                -70, -68, -11, 59, 8, -6, -105, 42, 27, 107, 30, -31, 59, -115,
            )
        val expectedContent =
            listOf(SizedSHA3256InnamarkTag.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256InnamarkTag.new(content)
        val extractedSize = watermark.extractSize()
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(expectedContent, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sizedSHA3256Innamark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedSHA3256InnamarkTag.SIZE_SIZE +
                    SizedSHA3256InnamarkTag.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -68, 110, 96, 52, -108, 67, 119, -3, 58, 85, 89, 2, 25, 81, 60, 105, 63, -54, 8,
                -50, -44, 69, 39, 37, -21, -72, -75, -56, -90, 97, 36, 63,
            )
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.SizedSHA3256InnamarkTag",
                40u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256InnamarkTag(watermarkContent)
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()
        val extractedHash = watermark.extractHash()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedSHA3256Trendmark_mismatchedSizeInvalidHash_warning() {
        // Arrange
        val invalidSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedSHA3256InnamarkTag.SIZE_SIZE +
                    SizedSHA3256InnamarkTag.HASH_SIZE +
                    content.size +
                    1
            ).toUInt()
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                39, -19, 18, 28, -13, 83, -11, 91, 79, 63, -53, 86, -126, -122, -70, -60, -81,
                17, -64, 117, 19, 119, 30, 118, 40, 56, -22, -10, -94, 54, -45, 20,
            )
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            InnamarkTag.MismatchedSizeWarning(
                "InnamarkTag.SizedSHA3256InnamarkTag",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            InnamarkTag.InvalidHashWarning(
                "InnamarkTag.SizedSHA3256InnamarkTag",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(SizedSHA3256InnamarkTag.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidHash +
                content

        // Act
        val watermark = SizedSHA3256InnamarkTag(watermarkContent)
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()
        val extractedHash = watermark.extractHash()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(invalidSize, extractedSize.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(invalidHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun compressedSizedSHA3256Innamark_creation_success() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedSHA3256InnamarkTag.SIZE_SIZE +
                    SizedSHA3256InnamarkTag.HASH_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -33, -68, 41, 33, 109, -108, 61, 115, 107, 115, 25, -122, 76, -88, -73, 88, 0,
                35, 122, 46, 83, -70, 108, -22, 73, 79, -48, -114, -7, 71, -99, 27,
            )
        val expectedContent =
            listOf(CompressedSizedSHA3256InnamarkTag.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256InnamarkTag.new(content)
        val extractedSize = watermark.extractSize()
        val extractedHash = watermark.extractHash()
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(expectedContent, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedSizedSHA3256Innamark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedSHA3256InnamarkTag.SIZE_SIZE +
                    SizedSHA3256InnamarkTag.HASH_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                22, 122, -87, -67, 54, -68, -18, -114, 70, 43, -46, -77, 75, -59, 107, 121, 32, -63,
                72, 54, 39, -70, 100, 80, -51, 116, 126, 105, 94, 14, -36, -102,
            )
        val expectedStatus =
            InnamarkTag.InvalidTagError(
                "InnamarkTag.CompressedSizedSHA3256InnamarkTag",
                104u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256InnamarkTag(watermarkContent)
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()
        val extractedHash = watermark.extractHash()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(expectedSize, extractedSize.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(expectedHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun compressedSizedSHA3256Innamark_mismatchedSizeInvalidHash_warning() {
        // Arrange
        val invalidSize =
            (
                InnamarkTagInterface.TAG_SIZE +
                    SizedSHA3256InnamarkTag.SIZE_SIZE +
                    SizedSHA3256InnamarkTag.HASH_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                56, -83, 26, -125, 21, 20, 92, 91, 122, 82, -125, -58, 17, -34, -53, 117, 112,
                -88, -128, -3, 72, 62, -85, 112, -103, 46, 26, -112, -67, 30, -34, 31,
            )
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            InnamarkTag.MismatchedSizeWarning(
                "InnamarkTag.CompressedSizedSHA3256InnamarkTag",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            InnamarkTag.InvalidHashWarning(
                "InnamarkTag.CompressedSizedSHA3256InnamarkTag",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(CompressedSizedSHA3256InnamarkTag.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256InnamarkTag(watermarkContent)
        val extractedContent = watermark.getContent()
        val extractedSize = watermark.extractSize()
        val extractedHash = watermark.extractHash()
        val status = watermark.validate()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertTrue(extractedSize.isSuccess)
        assertEquals(invalidSize, extractedSize.value)
        assertTrue(extractedHash.isSuccess)
        assertEquals(invalidHash, extractedHash.value)
        assertEquals(watermarkContent, watermark.watermarkContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun parse_valid_success() {
        // Arrange
        val rawInnamarkTag = RawInnamarkTag.new(content)
        val sizedInnamarkTag = SizedInnamarkTag.new(content)
        val crc32InnamarkTag = CRC32InnamarkTag.new(content)
        val sizedCRC32InnamarkTag = SizedCRC32InnamarkTag.new(content)
        val sha3256InnamarkTag = SHA3256InnamarkTag.new(content)
        val sizedSHA3256InnamarkTag = SizedSHA3256InnamarkTag.new(content)
        val compressedRawInnamarkTag = CompressedRawInnamarkTag.new(content)
        val compressedSizedInnamarkTag = CompressedSizedInnamarkTag.new(content)
        val compressedCRC32InnamarkTag = CompressedCRC32InnamarkTag.new(content)
        val compressedSizedCRC32InnamarkTag = CompressedSizedCRC32InnamarkTag.new(content)
        val compressedSHA3256InnamarkTag = CompressedSHA3256InnamarkTag.new(content)
        val compressedSizedSHA3256InnamarkTag = CompressedSizedSHA3256InnamarkTag.new(content)

        // Act
        val parsedPlainWatermark = InnamarkTag.parse(rawInnamarkTag.watermarkContent)
        val parsedSizedInnamarkTag = InnamarkTag.parse(sizedInnamarkTag.watermarkContent)
        val parsedCRC32InnamarkTag = InnamarkTag.parse(crc32InnamarkTag.watermarkContent)
        val parsedSizedCRC32InnamarkTag = InnamarkTag.parse(sizedCRC32InnamarkTag.watermarkContent)
        val parsedSHA3256InnamarkTag = InnamarkTag.parse(sha3256InnamarkTag.watermarkContent)
        val parsedSizedSHA3256InnamarkTag =
            InnamarkTag.parse(sizedSHA3256InnamarkTag.watermarkContent)
        val parsedCompressedRawInnamarkTag =
            InnamarkTag.parse(compressedRawInnamarkTag.watermarkContent)
        val parsedCompressedSizedInnamarkTag =
            InnamarkTag.parse(compressedSizedInnamarkTag.watermarkContent)
        val parsedCompressedCRC32InnamarkTag =
            InnamarkTag.parse(compressedCRC32InnamarkTag.watermarkContent)
        val parsedCompressedSizedCRC32InnamarkTag =
            InnamarkTag.parse(compressedSizedCRC32InnamarkTag.watermarkContent)
        val parsedCompressedSHA3256InnamarkTag =
            InnamarkTag.parse(compressedSHA3256InnamarkTag.watermarkContent)
        val parsedCompressedSizedSHA3256InnamarkTag =
            InnamarkTag.parse(compressedSizedSHA3256InnamarkTag.watermarkContent)

        // Assert
        assertTrue(parsedPlainWatermark.isSuccess)
        var parsedWatermark = parsedPlainWatermark.value!!
        assertTrue(parsedWatermark is RawInnamarkTag)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(rawInnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSizedInnamarkTag.isSuccess)
        parsedWatermark = parsedSizedInnamarkTag.value!!
        assertTrue(parsedWatermark is SizedInnamarkTag)
        assertEquals(sizedInnamarkTag.extractSize().value!!, parsedWatermark.extractSize().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedInnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCRC32InnamarkTag.isSuccess)
        parsedWatermark = parsedCRC32InnamarkTag.value!!
        assertTrue(parsedWatermark is CRC32InnamarkTag)
        assertEquals(
            crc32InnamarkTag.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(crc32InnamarkTag.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSizedCRC32InnamarkTag.isSuccess)
        parsedWatermark = parsedSizedCRC32InnamarkTag.value!!
        assertTrue(parsedWatermark is SizedCRC32InnamarkTag)
        assertEquals(
            sizedCRC32InnamarkTag.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedCRC32InnamarkTag.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedCRC32InnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSHA3256InnamarkTag.isSuccess)
        parsedWatermark = parsedSHA3256InnamarkTag.value!!
        assertTrue(parsedWatermark is SHA3256InnamarkTag)
        assertEquals(
            sha3256InnamarkTag.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sha3256InnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSizedSHA3256InnamarkTag.isSuccess)
        parsedWatermark = parsedSizedSHA3256InnamarkTag.value!!
        assertTrue(parsedWatermark is SizedSHA3256InnamarkTag)
        assertEquals(
            sizedSHA3256InnamarkTag.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedSHA3256InnamarkTag.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedSHA3256InnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedRawInnamarkTag.isSuccess)
        parsedWatermark = parsedCompressedRawInnamarkTag.value!!
        assertTrue(parsedWatermark is CompressedRawInnamarkTag)
        var decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(compressedRawInnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedInnamarkTag.isSuccess)
        parsedWatermark = parsedCompressedSizedInnamarkTag.value!!
        assertTrue(parsedWatermark is CompressedSizedInnamarkTag)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedInnamarkTag.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(compressedSizedInnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedCRC32InnamarkTag.isSuccess)
        parsedWatermark = parsedCompressedCRC32InnamarkTag.value!!
        assertTrue(parsedWatermark is CompressedCRC32InnamarkTag)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedCRC32InnamarkTag.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(compressedCRC32InnamarkTag.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedCRC32InnamarkTag.isSuccess)
        parsedWatermark = parsedCompressedSizedCRC32InnamarkTag.value!!
        assertTrue(parsedWatermark is CompressedSizedCRC32InnamarkTag)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedCRC32InnamarkTag.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            compressedSizedCRC32InnamarkTag.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(
            compressedSizedCRC32InnamarkTag.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSHA3256InnamarkTag.isSuccess)
        parsedWatermark = parsedCompressedSHA3256InnamarkTag.value!!
        assertTrue(parsedWatermark is CompressedSHA3256InnamarkTag)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSHA3256InnamarkTag.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(
            compressedSHA3256InnamarkTag.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedSHA3256InnamarkTag.isSuccess)
        parsedWatermark = parsedCompressedSizedSHA3256InnamarkTag.value!!
        assertTrue(parsedWatermark is CompressedSizedSHA3256InnamarkTag)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedSHA3256InnamarkTag.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            compressedSizedSHA3256InnamarkTag.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(
            compressedSizedSHA3256InnamarkTag.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)
    }

    @Test
    fun parse_completeness_success() {
        // Arrange
        check(InnamarkTagInterface.TAG_SIZE == 1)
        val existingTags = listOf(0, 32, 16, 48, 8, 40, 64, 96, 80, 112, 72, 104)

        // Act & Assert
        for (tag in 0..255) {
            val watermark = listOf(tag.toByte())
            val parsedWatermark = InnamarkTag.parse(watermark)
            val events = parsedWatermark.status.getEvents()

            if (tag !in existingTags) {
                assertTrue(events.size == 1, "Tag $tag should not exist!")
                assertTrue(
                    events.first() is InnamarkTag.UnknownTagError,
                    "Tag $tag should not exist!",
                )
            } else {
                for (event in events) {
                    assertFalse(
                        event is InnamarkTag.UnknownTagError,
                        "Tag $tag should exist!",
                    )
                }
            }
        }
    }

    @Test
    fun emptyError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = InnamarkTag.IncompleteTagError
        val expected =
            "Error (InnamarkTag): Cannot validate a watermark without a complete tag (1 byte(s))."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun notEnoughDataError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = InnamarkTag.NotEnoughDataError("Unittest", 42)
        val expected = "Error (Unittest): At least 42 bytes are required."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun unknownTagError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = InnamarkTag.UnknownTagError(42u)
        val expected = "Error (InnamarkTag): Unknown watermark tag: 42."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidTagError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = InnamarkTag.InvalidTagError("Unittest", 42u, 43u)
        val expected = "Error (Unittest): Expected tag: 42, but was: 43."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun mismatchedSizeWarning_defaultInput_correctWarningMessage() {
        // Arrange
        val error = InnamarkTag.MismatchedSizeWarning("Unittest", 42, 43)
        val expected = "Warning (Unittest): Expected 42 bytes, but extracted 43 bytes."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidChecksumWarning_defaultInput_correctErrorMessage() {
        // Arrange
        val error = InnamarkTag.InvalidChecksumWarning("Unittest", 0xdeadbeefu, 0xcafebabeu)
        val expected = "Warning (Unittest): Expected checksum: 0xdeadbeef, but was: 0xcafebabe."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidHashWarning_defaultInput_correctWarningMessage() {
        // Arrange
        val error =
            InnamarkTag.InvalidHashWarning(
                "Unittest",
                listOf<Byte>(0xde.toByte(), 0xad.toByte(), 0xbe.toByte(), 0xef.toByte()),
                listOf<Byte>(0xca.toByte(), 0xfe.toByte(), 0xba.toByte(), 0xbe.toByte()),
            )
        val expected =
            "Warning (Unittest): Expected hash: [DE, AD, BE, EF], but was: [CA, FE, BA, BE]."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }
}
