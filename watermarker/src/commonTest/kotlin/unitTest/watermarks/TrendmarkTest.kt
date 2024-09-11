/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.trend.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.CRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedCRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedRawTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedCRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedSHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.CompressedSizedTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.RawTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedCRC32Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedSHA3256Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedTrendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.TrendmarkInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrendmarkTest {
    private val content = "Lorem Ipsum".encodeToByteArray().asList()
    private val compressedContent =
        listOf<Byte>(-13, -55, 47, 74, -51, 85, -16, 44, 40, 46, -51, 5, 0)

    @Test
    fun rawTrendmark_creation_success() {
        // Arrange
        val expected = listOf(RawTrendmark.TYPE_TAG.toByte()) + content

        // Act
        val watermark = RawTrendmark.new(content)
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun rawTrendmark_invalidTag_error() {
        // Arrange
        val watermarkContent = listOf((-1).toByte()) + content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.RawTrendmark",
                0u,
                255u,
            ).into().toString()

        // Act
        val watermark = RawTrendmark(watermarkContent)
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
    fun compressedRawTrendmark_creation_success() {
        // Arrange
        val expected = listOf(CompressedRawTrendmark.TYPE_TAG.toByte()) + compressedContent

        // Act
        val watermark = CompressedRawTrendmark.new(content)
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun compressedRawTrendmark_invalidTag_error() {
        // Arrange
        val watermarkContent = listOf((-1).toByte()) + compressedContent
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CompressedRawTrendmark",
                254u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedRawTrendmark(watermarkContent)
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
    fun sizedTrendmark_creation_success() {
        // Arrange
        val expectedSize =
            (TrendmarkInterface.TAG_SIZE + SizedTrendmark.SIZE_SIZE + content.size).toUInt()
        val expected =
            listOf(SizedTrendmark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedTrendmark.new(content)
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
    fun sizedTrendmark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (TrendmarkInterface.TAG_SIZE + SizedTrendmark.SIZE_SIZE + content.size).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedTrendmark",
                1u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedTrendmark(watermarkContent)
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
    fun sizedTrendmark_mismatchedSize_warning() {
        // Arrange
        val invalidSize =
            (TrendmarkInterface.TAG_SIZE + SizedTrendmark.SIZE_SIZE + content.size + 1).toUInt()
        val watermarkContent =
            listOf(SizedTrendmark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedTrendmark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = SizedTrendmark(watermarkContent)
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
    fun compressedSizedTrendmark_creation_success() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedTrendmark.SIZE_SIZE +
                    compressedContent.size
            ).toUInt()
        val expected =
            listOf(CompressedSizedTrendmark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedSizedTrendmark.new(content)
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
    fun compressedSizedTrendmark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedTrendmark.SIZE_SIZE +
                    compressedContent.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CompressedSizedTrendmark",
                253u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedSizedTrendmark(watermarkContent)
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
    fun compressedSizedTrendmark_mismatchedSize_warning() {
        // Arrange
        val invalidSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedTrendmark.SIZE_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(CompressedSizedTrendmark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Trendmark.MismatchedSizeWarning(
                "Trendmark.CompressedSizedTrendmark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = CompressedSizedTrendmark(watermarkContent)
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
    fun crc32Trendmark_creation_success() {
        // Arrange
        val expectedCrc32 = 0x35160B87u
        val expected =
            listOf(CRC32Trendmark.TYPE_TAG.toByte()) + expectedCrc32.toBytesLittleEndian() + content

        // Act
        val watermark = CRC32Trendmark.new(content)
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
    fun crc32Trendmark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0xBFC71733u
        val watermarkContent =
            listOf((-1).toByte()) + expectedCrc32.toBytesLittleEndian() + content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CRC32Trendmark",
                2u,
                255u,
            ).into().toString()

        // Act
        val watermark = CRC32Trendmark(watermarkContent)
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
    fun crc32Trendmark_invalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0x35160B87u
        val invalidCrc32 = 0xFFFFFFFFu
        val watermarkContent =
            listOf(CRC32Trendmark.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidChecksumWarning(
                "Trendmark.CRC32Trendmark",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CRC32Trendmark(watermarkContent)
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
    fun compressedCRC32Trendmark_creation_success() {
        // Arrange
        val expectedCrc32 = 0xFF46549Du
        val expected =
            listOf(CompressedCRC32Trendmark.TYPE_TAG.toByte()) +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedCRC32Trendmark.new(content)
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
    fun compressedCRC32Trendmark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0x15C089FFu
        val watermarkContent =
            listOf((-1).toByte()) + expectedCrc32.toBytesLittleEndian() + compressedContent
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CompressedCRC32Trendmark",
                252u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedCRC32Trendmark(watermarkContent)
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
    fun compressedCRC32Trendmark_invalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0xFF46549Du
        val invalidCrc32 = 0xFFFFFFFFu
        val watermarkContent =
            listOf(CompressedCRC32Trendmark.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Trendmark.InvalidChecksumWarning(
                "Trendmark.CompressedCRC32Trendmark",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CompressedCRC32Trendmark(watermarkContent)
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
    fun sizedCRC32Trendmark_creation_success() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Trendmark.SIZE_SIZE +
                    SizedCRC32Trendmark.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val expectedCrc32 = 0x045B851Eu
        val expected =
            listOf(SizedCRC32Trendmark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedCRC32Trendmark.new(content)
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
    fun sizedCRC32Trendmark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0x735EA3E1u
        val size =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Trendmark.SIZE_SIZE +
                    SizedCRC32Trendmark.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedCRC32Trendmark",
                3u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedCRC32Trendmark(watermarkContent)
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
        val expectedCrc32 = 0xD3B90546u
        val invalidCrc32 = 0xFFFFFFFFu
        val invalidSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Trendmark.SIZE_SIZE +
                    SizedCRC32Trendmark.CHECKSUM_SIZE +
                    content.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(SizedCRC32Trendmark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedCRC32Trendmark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Trendmark.InvalidChecksumWarning(
                "Trendmark.SizedCRC32Trendmark",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = SizedCRC32Trendmark(watermarkContent)
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
    fun compressedSizedCRC32Trendmark_creation_success() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Trendmark.SIZE_SIZE +
                    SizedCRC32Trendmark.CHECKSUM_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedCrc32 = 0xD2A70713u
        val expected =
            listOf(CompressedSizedCRC32Trendmark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent

        // Act
        val watermark = CompressedSizedCRC32Trendmark.new(content)
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
    fun compressedSizedCRC32Trendmark_invalidTag_error() {
        // Arrange
        val expectedCrc32 = 0x8E069413u
        val size =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Trendmark.SIZE_SIZE +
                    SizedCRC32Trendmark.CHECKSUM_SIZE +
                    compressedContent.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CompressedSizedCRC32Trendmark",
                251u,
                255u,
            ).into().toString()

        // Act
        val watermark = CompressedSizedCRC32Trendmark(watermarkContent)
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
    fun compressedSizedCRC32Trendmark_mismatchedSizeInvalidChecksum_warning() {
        // Arrange
        val expectedCrc32 = 0x4D7D848Du
        val invalidCrc32 = 0xFFFFFFFFu
        val invalidSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Trendmark.SIZE_SIZE +
                    SizedCRC32Trendmark.CHECKSUM_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(CompressedSizedCRC32Trendmark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                compressedContent
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.CompressedSizedCRC32Trendmark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Trendmark.InvalidChecksumWarning(
                "Trendmark.CompressedSizedCRC32Trendmark",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = CompressedSizedCRC32Trendmark(watermarkContent)
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
    fun sha3256Trendmark_creation_success() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                -34, 2, 101, -35, 107, 22, -96, -76, -85, 5, -92, 57, 54, -64, 115, 18, 79, 102,
                -94, -86, 85, -77, -100, 43, 48, -74, 25, -34, 28, 17, -55, 80,
            )
        val expectedContent =
            listOf(SHA3256Trendmark.TYPE_TAG.toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Trendmark.new(content)
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
    fun sha3256Trendmark_invalidTag_error() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                94, -5, 11, -28, 39, 90, -127, 48, 78, -60, -46, 88, 37, 14, -58, -35, 9, -124,
                -49, -76, -26, 59, -96, -35, -99, 98, 110, -6, -49, 47, -52, 25,
            )
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SHA3256Trendmark",
                4u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Trendmark(watermarkContent)
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
                -34, 2, 101, -35, 107, 22, -96, -76, -85, 5, -92, 57, 54, -64, 115, 18, 79, 102,
                -94, -86, 85, -77, -100, 43, 48, -74, 25, -34, 28, 17, -55, 80,
            )
        val expectedStatus =
            Trendmark.InvalidHashWarning(
                "Trendmark.SHA3256Trendmark",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent = listOf(SHA3256Trendmark.TYPE_TAG.toByte()) + invalidHash + content

        // Act
        val watermark = SHA3256Trendmark(watermarkContent)
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
    fun compressedSHA3256Trendmark_creation_success() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                -33, 96, 25, 69, -62, 119, -104, 93, 14, 89, -52, -8, -101, 39, -19, -97, -100,
                -104, -123, -91, -77, 62, -57, 71, -6, -120, 104, 116, -88, -17, 119, 91,
            )
        val expectedContent =
            listOf(CompressedSHA3256Trendmark.TYPE_TAG.toByte()) + expectedHash + compressedContent

        // Act
        val watermark = CompressedSHA3256Trendmark.new(content)
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
    fun compressedSHA3256Trendmark_invalidTag_error() {
        // Arrange
        val expectedHash =
            listOf<Byte>(
                55, 41, 25, 53, -104, 106, 100, -32, 42, 123, -87, 37, -47, 105, -71, 24, -48, -43,
                54, 84, 69, -92, -70, -57, -61, -120, 113, 124, 119, -44, -38, 92,
            )
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CompressedSHA3256Trendmark",
                250u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + compressedContent

        // Act
        val watermark = CompressedSHA3256Trendmark(watermarkContent)
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
    fun compressedSHA3256Trendmark_invalidHash_warning() {
        // Arrange
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                -33, 96, 25, 69, -62, 119, -104, 93, 14, 89, -52, -8, -101, 39, -19, -97, -100,
                -104, -123, -91, -77, 62, -57, 71, -6, -120, 104, 116, -88, -17, 119, 91,
            )
        val expectedStatus =
            Trendmark.InvalidHashWarning(
                "Trendmark.CompressedSHA3256Trendmark",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent =
            listOf(CompressedSHA3256Trendmark.TYPE_TAG.toByte()) +
                invalidHash +
                compressedContent

        // Act
        val watermark = CompressedSHA3256Trendmark(watermarkContent)
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
    fun sizedSHA3256Trendmark_creation_success() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Trendmark.SIZE_SIZE +
                    SizedSHA3256Trendmark.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -14, 23, -91, -82, 67, -59, 112, -94, 51, 43, -75, -112, 96, 35, 69, -38, 109,
                53, -45, 52, -107, 92, 23, -125, -20, -20, 46, 73, 102, 69, -55, 26,
            )
        val expectedContent =
            listOf(SizedSHA3256Trendmark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Trendmark.new(content)
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
    fun sizedSHA3256Trendmark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Trendmark.SIZE_SIZE +
                    SizedSHA3256Trendmark.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -68, 110, 96, 52, -108, 67, 119, -3, 58, 85, 89, 2, 25, 81, 60, 105, 63, -54, 8,
                -50, -44, 69, 39, 37, -21, -72, -75, -56, -90, 97, 36, 63,
            )
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedSHA3256Trendmark",
                5u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Trendmark(watermarkContent)
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
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Trendmark.SIZE_SIZE +
                    SizedSHA3256Trendmark.HASH_SIZE +
                    content.size +
                    1
            ).toUInt()
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                42, 17, 9, 123, -5, 38, -78, -1, 60, 99, -65, -40, 54, 22, 125, -128, -113, -57,
                5, 82, -71, 97, 76, -23, -39, 92, -45, 62, -36, -19, 73, -115,
            )
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedSHA3256Trendmark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Trendmark.InvalidHashWarning(
                "Trendmark.SizedSHA3256Trendmark",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(SizedSHA3256Trendmark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidHash +
                content

        // Act
        val watermark = SizedSHA3256Trendmark(watermarkContent)
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
    fun compressedSizedSHA3256Trendmark_creation_success() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Trendmark.SIZE_SIZE +
                    SizedSHA3256Trendmark.HASH_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -52, -87, -76, -127, -75, 58, 51, -92, -79, -18, 122, -28, -128, 96, 69, -46, 102,
                -28, 68, -118, 65, -44, -115, 94, -63, -103, -120, -78, -17, -125, -56, 110,
            )
        val expectedContent =
            listOf(CompressedSizedSHA3256Trendmark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256Trendmark.new(content)
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
    fun compressedSizedSHA3256Trendmark_invalidTag_error() {
        // Arrange
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Trendmark.SIZE_SIZE +
                    SizedSHA3256Trendmark.HASH_SIZE +
                    compressedContent.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                22, 122, -87, -67, 54, -68, -18, -114, 70, 43, -46, -77, 75, -59, 107, 121, 32, -63,
                72, 54, 39, -70, 100, 80, -51, 116, 126, 105, 94, 14, -36, -102,
            )
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CompressedSizedSHA3256Trendmark",
                249u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256Trendmark(watermarkContent)
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
    fun compressedSizedSHA3256Trendmark_mismatchedSizeInvalidHash_warning() {
        // Arrange
        val invalidSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Trendmark.SIZE_SIZE +
                    SizedSHA3256Trendmark.HASH_SIZE +
                    compressedContent.size +
                    1
            ).toUInt()
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                47, 42, -73, -2, -51, -41, 54, -37, 84, -120, 62, 73, -100, 0, -1, -8, 57, 54, -48,
                21, 97, -110, -2, -9, 14, 59, -36, 117, 66, -83, -74, 47,
            )
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.CompressedSizedSHA3256Trendmark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Trendmark.InvalidHashWarning(
                "Trendmark.CompressedSizedSHA3256Trendmark",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(CompressedSizedSHA3256Trendmark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidHash +
                compressedContent

        // Act
        val watermark = CompressedSizedSHA3256Trendmark(watermarkContent)
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
        val rawTrendmark = RawTrendmark.new(content)
        val sizedTrendmark = SizedTrendmark.new(content)
        val crc32Trendmark = CRC32Trendmark.new(content)
        val sizedCRC32Trendmark = SizedCRC32Trendmark.new(content)
        val sha3256Trendmark = SHA3256Trendmark.new(content)
        val sizedSHA3256Trendmark = SizedSHA3256Trendmark.new(content)
        val compressedRawTrendmark = CompressedRawTrendmark.new(content)
        val compressedSizedTrendmark = CompressedSizedTrendmark.new(content)
        val compressedCRC32Trendmark = CompressedCRC32Trendmark.new(content)
        val compressedSizedCRC32Trendmark = CompressedSizedCRC32Trendmark.new(content)
        val compressedSHA3256Trendmark = CompressedSHA3256Trendmark.new(content)
        val compressedSizedSHA3256Trendmark = CompressedSizedSHA3256Trendmark.new(content)

        // Act
        val parsedPlainWatermark = Trendmark.parse(rawTrendmark.watermarkContent)
        val parsedSizedTrendmark = Trendmark.parse(sizedTrendmark.watermarkContent)
        val parsedCRC32Trendmark = Trendmark.parse(crc32Trendmark.watermarkContent)
        val parsedSizedCRC32Trendmark = Trendmark.parse(sizedCRC32Trendmark.watermarkContent)
        val parsedSHA3256Trendmark = Trendmark.parse(sha3256Trendmark.watermarkContent)
        val parsedSizedSHA3256Trendmark = Trendmark.parse(sizedSHA3256Trendmark.watermarkContent)
        val parsedCompressedRawTrendmark = Trendmark.parse(compressedRawTrendmark.watermarkContent)
        val parsedCompressedSizedTrendmark =
            Trendmark.parse(compressedSizedTrendmark.watermarkContent)
        val parsedCompressedCRC32Trendmark =
            Trendmark.parse(compressedCRC32Trendmark.watermarkContent)
        val parsedCompressedSizedCRC32Trendmark =
            Trendmark.parse(compressedSizedCRC32Trendmark.watermarkContent)
        val parsedCompressedSHA3256Trendmark =
            Trendmark.parse(compressedSHA3256Trendmark.watermarkContent)
        val parsedCompressedSizedSHA3256Trendmark =
            Trendmark.parse(compressedSizedSHA3256Trendmark.watermarkContent)

        // Assert
        assertTrue(parsedPlainWatermark.isSuccess)
        var parsedWatermark = parsedPlainWatermark.value!!
        assertTrue(parsedWatermark is RawTrendmark)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(rawTrendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSizedTrendmark.isSuccess)
        parsedWatermark = parsedSizedTrendmark.value!!
        assertTrue(parsedWatermark is SizedTrendmark)
        assertEquals(sizedTrendmark.extractSize().value!!, parsedWatermark.extractSize().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedTrendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCRC32Trendmark.isSuccess)
        parsedWatermark = parsedCRC32Trendmark.value!!
        assertTrue(parsedWatermark is CRC32Trendmark)
        assertEquals(
            crc32Trendmark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(crc32Trendmark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSizedCRC32Trendmark.isSuccess)
        parsedWatermark = parsedSizedCRC32Trendmark.value!!
        assertTrue(parsedWatermark is SizedCRC32Trendmark)
        assertEquals(
            sizedCRC32Trendmark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedCRC32Trendmark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedCRC32Trendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSHA3256Trendmark.isSuccess)
        parsedWatermark = parsedSHA3256Trendmark.value!!
        assertTrue(parsedWatermark is SHA3256Trendmark)
        assertEquals(sha3256Trendmark.extractHash().value!!, parsedWatermark.extractHash().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sha3256Trendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedSizedSHA3256Trendmark.isSuccess)
        parsedWatermark = parsedSizedSHA3256Trendmark.value!!
        assertTrue(parsedWatermark is SizedSHA3256Trendmark)
        assertEquals(
            sizedSHA3256Trendmark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedSHA3256Trendmark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedSHA3256Trendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedRawTrendmark.isSuccess)
        parsedWatermark = parsedCompressedRawTrendmark.value!!
        assertTrue(parsedWatermark is CompressedRawTrendmark)
        var decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(compressedRawTrendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedTrendmark.isSuccess)
        parsedWatermark = parsedCompressedSizedTrendmark.value!!
        assertTrue(parsedWatermark is CompressedSizedTrendmark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedTrendmark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(compressedSizedTrendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedCRC32Trendmark.isSuccess)
        parsedWatermark = parsedCompressedCRC32Trendmark.value!!
        assertTrue(parsedWatermark is CompressedCRC32Trendmark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedCRC32Trendmark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(compressedCRC32Trendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedCRC32Trendmark.isSuccess)
        parsedWatermark = parsedCompressedSizedCRC32Trendmark.value!!
        assertTrue(parsedWatermark is CompressedSizedCRC32Trendmark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedCRC32Trendmark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            compressedSizedCRC32Trendmark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(
            compressedSizedCRC32Trendmark.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSHA3256Trendmark.isSuccess)
        parsedWatermark = parsedCompressedSHA3256Trendmark.value!!
        assertTrue(parsedWatermark is CompressedSHA3256Trendmark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSHA3256Trendmark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(compressedSHA3256Trendmark.watermarkContent, parsedWatermark.watermarkContent)
        assertTrue(parsedWatermark.validate().isSuccess)

        assertTrue(parsedCompressedSizedSHA3256Trendmark.isSuccess)
        parsedWatermark = parsedCompressedSizedSHA3256Trendmark.value!!
        assertTrue(parsedWatermark is CompressedSizedSHA3256Trendmark)
        decompressedContent = parsedWatermark.getContent()
        assertTrue(decompressedContent.isSuccess)
        assertEquals(content, decompressedContent.value)
        assertEquals(
            compressedSizedSHA3256Trendmark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            compressedSizedSHA3256Trendmark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(
            compressedSizedSHA3256Trendmark.watermarkContent,
            parsedWatermark.watermarkContent,
        )
        assertTrue(parsedWatermark.validate().isSuccess)
    }

    @Test
    fun parse_completeness_success() {
        // Arrange
        check(TrendmarkInterface.TAG_SIZE == 1)
        val existingTags = listOf(0, 1, 2, 3, 4, 5, 254, 253, 252, 251, 250, 249)

        // Act & Assert
        for (tag in 0..255) {
            val watermark = listOf(tag.toByte())
            val parsedWatermark = Trendmark.parse(watermark)
            val events = parsedWatermark.status.getEvents()

            if (tag !in existingTags) {
                assertTrue(events.size == 1, "Tag $tag should not exist!")
                assertTrue(
                    events.first() is Trendmark.UnknownTagError,
                    "Tag $tag should not exist!",
                )
            } else {
                for (event in events) {
                    assertFalse(
                        event is Trendmark.UnknownTagError,
                        "Tag $tag should exist!",
                    )
                }
            }
        }
    }

    @Test
    fun emptyError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Trendmark.IncompleteTagError
        val expected =
            "Error (Trendmark): Cannot validate a watermark without a complete tag (1 byte(s))."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun notEnoughDataError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Trendmark.NotEnoughDataError("Unittest", 42)
        val expected = "Error (Unittest): At least 42 bytes are required."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun unknownTagError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Trendmark.UnknownTagError(42u)
        val expected = "Error (Trendmark): Unknown watermark tag: 42."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidTagError_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Trendmark.InvalidTagError("Unittest", 42u, 43u)
        val expected = "Error (Unittest): Expected tag: 42, but was: 43."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun mismatchedSizeWarning_defaultInput_correctWarningMessage() {
        // Arrange
        val error = Trendmark.MismatchedSizeWarning("Unittest", 42, 43)
        val expected = "Warning (Unittest): Expected 42 bytes, but extracted 43 bytes."

        // Act
        val result = error.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun invalidChecksumWarning_defaultInput_correctErrorMessage() {
        // Arrange
        val error = Trendmark.InvalidChecksumWarning("Unittest", 0xdeadbeefu, 0xcafebabeu)
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
            Trendmark.InvalidHashWarning(
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
        assertTrue(false)
    }

    @Test
    fun trendmark_parseableBytes_contentAsString() {
        // Arrange
        val expected = "Lorem Ipsum"
        val watermark = RawTrendmark.new(content)

        // Act
        val extractedContent = watermark.getContentAsString()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(expected, extractedContent.value)
    }

    @Test
    fun trendmark_unparseableBytes_contentAsString_gracefull() {
        // Arrange
        val expected = "\uFFFD"
        val watermark = RawTrendmark.new(listOf<Byte>(195.toByte()))

        // Act
        val extractedContent = watermark.getContentAsString(errorOnInvalidUTF8 = false)

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(expected, extractedContent.value)
    }

    @Test
    fun trendmark_unparseableBytes_contentAsString_throwError() {
        // Arrange
        val watermark = RawTrendmark.new(listOf<Byte>(195.toByte()))

        // Act
        val extractedContent = watermark.getContentAsString(errorOnInvalidUTF8 = true)

        // Assert
        assertTrue(extractedContent.isError)
    }
}
