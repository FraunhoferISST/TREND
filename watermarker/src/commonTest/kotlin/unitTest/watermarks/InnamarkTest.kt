/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.innamark.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.CRC32Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedCRC32Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedRawInnamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSHA3256Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedCRC32Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedInnamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.CompressedSizedSHA3256Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkInterface
import de.fraunhofer.isst.innamark.watermarker.watermarks.RawInnamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.SHA3256Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedCRC32Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedInnamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.SizedSHA3256Innamark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InnamarkTest {
    private val content = "Lorem Ipsum".encodeToByteArray().asList()
    private val compressedContent =
        listOf<Byte>(-13, -55, 47, 74, -51, 85, -16, 44, 40, 46, -51, 5, 0)

    @Test
    fun rawInnamark_creation_success() {
        // Arrange
        val expected = listOf(RawInnamark.TYPE_TAG.toByte()) + content

        // Act
        val watermark = RawInnamark.new(content)
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
            Innamark.InvalidTagError(
                "Innamark.RawInnamark",
                0u,
                255u,
            ).into().toString()

        // Act
        val watermark = RawInnamark(watermarkContent)
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
        val expected = listOf(CompressedRawInnamark.TYPE_TAG.toByte()) + compressedContent

        // Act
        val watermark = CompressedRawInnamark.new(content)
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
            Innamark.InvalidTagError(
                "Innamark.CompressedRawInnamark",
                64u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedRawInnamark(watermarkContent)
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
            (InnamarkInterface.TAG_SIZE + SizedInnamark.SIZE_SIZE + content.size).toUInt()
        val expected =
            listOf(SizedInnamark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedInnamark.new(content)
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
            (InnamarkInterface.TAG_SIZE + SizedInnamark.SIZE_SIZE + content.size).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                content
        val expectedStatus =
            Innamark.InvalidTagError(
                "Innamark.SizedInnamark",
                32u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedInnamark(watermarkContent)
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
            (InnamarkInterface.TAG_SIZE + SizedInnamark.SIZE_SIZE + content.size + 1).toUInt()
        val watermarkContent =
            listOf(SizedInnamark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                content
        val expectedStatus =
            Innamark.MismatchedSizeWarning(
                "Innamark.SizedInnamark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = SizedInnamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedInnamark.SIZE_SIZE +
                    compressedContent.size
            ).toUInt()
        val expected =
            listOf(CompressedSizedInnamark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedSizedInnamark.new(content)
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
                InnamarkInterface.TAG_SIZE +
                    SizedInnamark.SIZE_SIZE +
                    compressedContent.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Innamark.InvalidTagError(
                "Innamark.CompressedSizedInnamark",
                96u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedSizedInnamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedInnamark.SIZE_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(CompressedSizedInnamark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Innamark.MismatchedSizeWarning(
                "Innamark.CompressedSizedInnamark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = CompressedSizedInnamark(watermarkContent)
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
            listOf(CRC32Innamark.TYPE_TAG.toByte()) + expectedCrc32.toBytesLittleEndian() + content

        // Act
        val watermark = CRC32Innamark.new(content)
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
            Innamark.InvalidTagError(
                "Innamark.CRC32Innamark",
                16u,
                255u,
            ).into().toString()

        // Act
        val watermark = CRC32Innamark(watermarkContent)
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
            listOf(CRC32Innamark.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            Innamark.InvalidChecksumWarning(
                "Innamark.CRC32Innamark",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CRC32Innamark(watermarkContent)
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
            listOf(CompressedCRC32Innamark.TYPE_TAG.toByte()) +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedCRC32Innamark.new(content)
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
            Innamark.InvalidTagError(
                "Innamark.CompressedCRC32Innamark",
                80u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedCRC32Innamark(watermarkContent)
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
            listOf(CompressedCRC32Innamark.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Innamark.InvalidChecksumWarning(
                "Innamark.CompressedCRC32Innamark",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CompressedCRC32Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedCRC32Innamark.SIZE_SIZE +
                    SizedCRC32Innamark.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val expectedCrc32 = 0x51C833FAu
        val expected =
            listOf(SizedCRC32Innamark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedCRC32Innamark.new(content)
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
                InnamarkInterface.TAG_SIZE +
                    SizedCRC32Innamark.SIZE_SIZE +
                    SizedCRC32Innamark.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            Innamark.InvalidTagError(
                "Innamark.SizedCRC32Innamark",
                48u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedCRC32Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedCRC32Innamark.SIZE_SIZE +
                    SizedCRC32Innamark.CHECKSUM_SIZE +
                    content.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(SizedCRC32Innamark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Innamark.MismatchedSizeWarning(
                "Innamark.SizedCRC32Innamark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Innamark.InvalidChecksumWarning(
                "Innamark.SizedCRC32Innamark",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = SizedCRC32Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedCRC32Innamark.SIZE_SIZE +
                    SizedCRC32Innamark.CHECKSUM_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedCrc32 = 0x1D71CD9Cu
        val expected =
            listOf(CompressedSizedCRC32Innamark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedSizedCRC32Innamark.new(content)
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
                InnamarkInterface.TAG_SIZE +
                    SizedCRC32Innamark.SIZE_SIZE +
                    SizedCRC32Innamark.CHECKSUM_SIZE +
                    compressedContent.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Innamark.InvalidTagError(
                "Innamark.CompressedSizedCRC32Innamark",
                112u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedSizedCRC32Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedCRC32Innamark.SIZE_SIZE +
                    SizedCRC32Innamark.CHECKSUM_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(CompressedSizedCRC32Innamark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Innamark.MismatchedSizeWarning(
                "Innamark.CompressedSizedCRC32Innamark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Innamark.InvalidChecksumWarning(
                "Innamark.CompressedSizedCRC32Innamark",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = CompressedSizedCRC32Innamark(watermarkContent)
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
            listOf(SHA3256Innamark.TYPE_TAG.toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Innamark.new(content)
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
            Innamark.InvalidTagError(
                "Innamark.SHA3256Innamark",
                8u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Innamark(watermarkContent)
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
            Innamark.InvalidHashWarning(
                "Innamark.SHA3256Innamark",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent = listOf(SHA3256Innamark.TYPE_TAG.toByte()) + invalidHash + content

        // Act
        val watermark = SHA3256Innamark(watermarkContent)
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
            listOf(CompressedSHA3256Innamark.TYPE_TAG.toByte()) + expectedHash + compressedContent

        // Act
        val watermark = CompressedSHA3256Innamark.new(content)
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
            Innamark.InvalidTagError(
                "Innamark.CompressedSHA3256Innamark",
                72u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + compressedContent

        // Act
        val watermark = CompressedSHA3256Innamark(watermarkContent)
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
            Innamark.InvalidHashWarning(
                "Innamark.CompressedSHA3256Innamark",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent =
            listOf(CompressedSHA3256Innamark.TYPE_TAG.toByte()) +
                invalidHash +
                compressedContent

        // Act
        val watermark = CompressedSHA3256Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedSHA3256Innamark.SIZE_SIZE +
                    SizedSHA3256Innamark.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                16, 40, 37, 39, 11, -23, 67, 16, -53, -112, -19, 39, -109, 30, 9, -53, -26, 19,
                -70, -68, -11, 59, 8, -6, -105, 42, 27, 107, 30, -31, 59, -115,
            )
        val expectedContent =
            listOf(SizedSHA3256Innamark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Innamark.new(content)
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
                InnamarkInterface.TAG_SIZE +
                    SizedSHA3256Innamark.SIZE_SIZE +
                    SizedSHA3256Innamark.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -68, 110, 96, 52, -108, 67, 119, -3, 58, 85, 89, 2, 25, 81, 60, 105, 63, -54, 8,
                -50, -44, 69, 39, 37, -21, -72, -75, -56, -90, 97, 36, 63,
            )
        val expectedStatus =
            Innamark.InvalidTagError(
                "Innamark.SizedSHA3256Innamark",
                40u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedSHA3256Innamark.SIZE_SIZE +
                    SizedSHA3256Innamark.HASH_SIZE +
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
            Innamark.MismatchedSizeWarning(
                "Innamark.SizedSHA3256Innamark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Innamark.InvalidHashWarning(
                "Innamark.SizedSHA3256Innamark",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(SizedSHA3256Innamark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidHash +
                content

        // Act
        val watermark = SizedSHA3256Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedSHA3256Innamark.SIZE_SIZE +
                    SizedSHA3256Innamark.HASH_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -33, -68, 41, 33, 109, -108, 61, 115, 107, 115, 25, -122, 76, -88, -73, 88, 0,
                35, 122, 46, 83, -70, 108, -22, 73, 79, -48, -114, -7, 71, -99, 27,
            )
        val expectedContent =
            listOf(CompressedSizedSHA3256Innamark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256Innamark.new(content)
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
                InnamarkInterface.TAG_SIZE +
                    SizedSHA3256Innamark.SIZE_SIZE +
                    SizedSHA3256Innamark.HASH_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                22, 122, -87, -67, 54, -68, -18, -114, 70, 43, -46, -77, 75, -59, 107, 121, 32, -63,
                72, 54, 39, -70, 100, 80, -51, 116, 126, 105, 94, 14, -36, -102,
            )
        val expectedStatus =
            Innamark.InvalidTagError(
                "Innamark.CompressedSizedSHA3256Innamark",
                104u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256Innamark(watermarkContent)
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
                InnamarkInterface.TAG_SIZE +
                    SizedSHA3256Innamark.SIZE_SIZE +
                    SizedSHA3256Innamark.HASH_SIZE +
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
            Innamark.MismatchedSizeWarning(
                "Innamark.CompressedSizedSHA3256Innamark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Innamark.InvalidHashWarning(
                "Innamark.CompressedSizedSHA3256Innamark",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(CompressedSizedSHA3256Innamark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256Innamark(watermarkContent)
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
        val rawInnamark = RawInnamark.new(content)
        val sizedInnamark = SizedInnamark.new(content)
        val crc32Innamark = CRC32Innamark.new(content)
        val sizedCRC32Innamark = SizedCRC32Innamark.new(content)
        val sha3256Innamark = SHA3256Innamark.new(content)
        val sizedSHA3256Innamark = SizedSHA3256Innamark.new(content)
        val compressedRawInnamark = CompressedRawInnamark.new(content)
        val compressedSizedInnamark = CompressedSizedInnamark.new(content)
        val compressedCRC32Innamark = CompressedCRC32Innamark.new(content)
        val compressedSizedCRC32Innamark = CompressedSizedCRC32Innamark.new(content)
        val compressedSHA3256Innamark = CompressedSHA3256Innamark.new(content)
        val compressedSizedSHA3256Innamark = CompressedSizedSHA3256Innamark.new(content)

        // Act
        val parsedPlainWatermark = Innamark.parse(rawInnamark.watermarkContent)
        val parsedSizedInnamark = Innamark.parse(sizedInnamark.watermarkContent)
        val parsedCRC32Innamark = Innamark.parse(crc32Innamark.watermarkContent)
        val parsedSizedCRC32Innamark = Innamark.parse(sizedCRC32Innamark.watermarkContent)
        val parsedSHA3256Innamark = Innamark.parse(sha3256Innamark.watermarkContent)
        val parsedSizedSHA3256Innamark = Innamark.parse(sizedSHA3256Innamark.watermarkContent)
        val parsedCompressedRawInnamark = Innamark.parse(compressedRawInnamark.watermarkContent)
        val parsedCompressedSizedInnamark =
            Innamark.parse(compressedSizedInnamark.watermarkContent)
        val parsedCompressedCRC32Innamark =
            Innamark.parse(compressedCRC32Innamark.watermarkContent)
        val parsedCompressedSizedCRC32Innamark =
            Innamark.parse(compressedSizedCRC32Innamark.watermarkContent)
        val parsedCompressedSHA3256Innamark =
            Innamark.parse(compressedSHA3256Innamark.watermarkContent)
        val parsedCompressedSizedSHA3256Innamark =
            Innamark.parse(compressedSizedSHA3256Innamark.watermarkContent)

        // Assert
        assertTrue(parsedPlainWatermark.isSuccess)
        var parsedWatermark = parsedPlainWatermark.value!!
        assertTrue(parsedWatermark is RawInnamark)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(rawInnamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSizedInnamark.isSuccess)
        parsedWatermark = parsedSizedInnamark.value!!
        assertTrue(parsedWatermark is SizedInnamark)
        assertEquals(sizedInnamark.extractSize().value!!, parsedWatermark.extractSize().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedInnamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCRC32Innamark.isSuccess)
        parsedWatermark = parsedCRC32Innamark.value!!
        assertTrue(parsedWatermark is CRC32Innamark)
        assertEquals(
            crc32Innamark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(crc32Innamark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSizedCRC32Innamark.isSuccess)
        parsedWatermark = parsedSizedCRC32Innamark.value!!
        assertTrue(parsedWatermark is SizedCRC32Innamark)
        assertEquals(
            sizedCRC32Innamark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedCRC32Innamark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedCRC32Innamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSHA3256Innamark.isSuccess)
        parsedWatermark = parsedSHA3256Innamark.value!!
        assertTrue(parsedWatermark is SHA3256Innamark)
        assertEquals(sha3256Innamark.extractHash().value!!, parsedWatermark.extractHash().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sha3256Innamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSizedSHA3256Innamark.isSuccess)
        parsedWatermark = parsedSizedSHA3256Innamark.value!!
        assertTrue(parsedWatermark is SizedSHA3256Innamark)
        assertEquals(
            sizedSHA3256Innamark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedSHA3256Innamark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedSHA3256Innamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedRawInnamark.isSuccess)
        parsedWatermark = parsedCompressedRawInnamark.value!!
        assertTrue(parsedWatermark is CompressedRawInnamark)
        var decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(compressedRawInnamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedInnamark.isSuccess)
        parsedWatermark = parsedCompressedSizedInnamark.value!!
        assertTrue(parsedWatermark is CompressedSizedInnamark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedInnamark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(compressedSizedInnamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedCRC32Innamark.isSuccess)
        parsedWatermark = parsedCompressedCRC32Innamark.value!!
        assertTrue(parsedWatermark is CompressedCRC32Innamark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedCRC32Innamark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(compressedCRC32Innamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedCRC32Innamark.isSuccess)
        parsedWatermark = parsedCompressedSizedCRC32Innamark.value!!
        assertTrue(parsedWatermark is CompressedSizedCRC32Innamark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedCRC32Innamark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            compressedSizedCRC32Innamark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(
            compressedSizedCRC32Innamark.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSHA3256Innamark.isSuccess)
        parsedWatermark = parsedCompressedSHA3256Innamark.value!!
        assertTrue(parsedWatermark is CompressedSHA3256Innamark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSHA3256Innamark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(compressedSHA3256Innamark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedSHA3256Innamark.isSuccess)
        parsedWatermark = parsedCompressedSizedSHA3256Innamark.value!!
        assertTrue(parsedWatermark is CompressedSizedSHA3256Innamark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedSHA3256Innamark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            compressedSizedSHA3256Innamark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(
            compressedSizedSHA3256Innamark.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)
    }

    @Test
    fun parse_completeness_success() {
        // Arrange
        check(InnamarkInterface.TAG_SIZE == 1)
        val existingTags = listOf(0, 32, 16, 48, 8, 40, 64, 96, 80, 112, 72, 104)

        // Act & Assert
        for (tag in 0..255) {
            val watermark = listOf(tag.toByte())
            val parsedWatermark = Innamark.parse(watermark)
            val events = parsedWatermark.status.getEvents()

            if (tag !in existingTags) {
                assertTrue(events.size == 1, "Tag $tag should not exist!")
                assertTrue(
                    events.first() is Innamark.UnknownTagError,
                    "Tag $tag should not exist!",
                )
            } else {
                for (event in events) {
                    assertFalse(
                        event is Innamark.UnknownTagError,
                        "Tag $tag should exist!",
                    )
                }
            }
        }
    }

    @Test
    fun emptyError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Innamark.IncompleteTagError
        val expected =
            "Error (Innamark): Cannot validate a watermark without a complete tag (1 byte(s))."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun notEnoughDataError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Innamark.NotEnoughDataError("Unittest", 42)
        val expected = "Error (Unittest): At least 42 bytes are required."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun unknownTagError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Innamark.UnknownTagError(42u)
        val expected = "Error (Innamark): Unknown watermark tag: 42."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidTagError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Innamark.InvalidTagError("Unittest", 42u, 43u)
        val expected = "Error (Unittest): Expected tag: 42, but was: 43."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun mismatchedSizeWarning_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Innamark.MismatchedSizeWarning("Unittest", 42, 43)
        val expected = "Warning (Unittest): Expected 42 bytes, but extracted 43 bytes."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidChecksumWarning_defaultInput_correctErrorMessage() {
        // Arrange
        val error = Innamark.InvalidChecksumWarning("Unittest", 0xdeadbeefu, 0xcafebabeu)
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
            Innamark.InvalidHashWarning(
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
