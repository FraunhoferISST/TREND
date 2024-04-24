/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.watermarks

import de.fraunhofer.isst.trend.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.CRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.RawWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedCRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedSHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.TrendmarkInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrendmarkTest {
    @Test
    fun rawWatermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expected = listOf(RawWatermark.TYPE_TAG.toByte()) + content

        // Act
        val watermark = RawWatermark.new(content)
        val extractedContent = watermark.getContent()

        // Assert
        assertTrue(extractedContent.isSuccess)
        assertEquals(content, extractedContent.value)
        assertEquals(expected, watermark.watermarkContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun rawWatermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val watermarkContent = listOf((-1).toByte()) + content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.RawWatermark",
                0u,
                255u,
            ).into().toString()

        // Act
        val watermark = RawWatermark(watermarkContent)
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
    fun sizedWatermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            (TrendmarkInterface.TAG_SIZE + SizedWatermark.SIZE_SIZE + content.size).toUInt()
        val expected =
            listOf(SizedWatermark.TYPE_TAG.toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedWatermark.new(content)
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
    fun sizedWatermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            (TrendmarkInterface.TAG_SIZE + SizedWatermark.SIZE_SIZE + content.size).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedWatermark",
                1u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedWatermark(watermarkContent)
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
    fun sizedWatermark_mismatchedSize_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val invalidSize =
            (TrendmarkInterface.TAG_SIZE + SizedWatermark.SIZE_SIZE + content.size + 1).toUInt()
        val watermarkContent =
            listOf(SizedWatermark.TYPE_TAG.toByte()) +
                invalidSize.toUInt().toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedWatermark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ).into().toString()

        // Act
        val watermark = SizedWatermark(watermarkContent)
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
    fun crc32Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x35160B87u
        val expected =
            listOf(CRC32Watermark.TYPE_TAG.toByte()) + expectedCrc32.toBytesLittleEndian() + content

        // Act
        val watermark = CRC32Watermark.new(content)
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
    fun crc32Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0xBFC71733u
        val watermarkContent =
            listOf((-1).toByte()) + expectedCrc32.toBytesLittleEndian() + content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.CRC32Watermark",
                2u,
                255u,
            ).into().toString()

        // Act
        val watermark = CRC32Watermark(watermarkContent)
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
    fun crc32Watermark_invalidChecksum_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x35160B87u
        val invalidCrc32 = 0xFFFFFFFFu
        val watermarkContent =
            listOf(CRC32Watermark.TYPE_TAG.toByte()) +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidChecksumWarning(
                "Trendmark.CRC32Watermark",
                invalidCrc32,
                expectedCrc32,
            ).into()

        // Act
        val watermark = CRC32Watermark(watermarkContent)
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
    fun sizedCrc32Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Watermark.SIZE_SIZE +
                    SizedCRC32Watermark.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val expectedCrc32 = 0x045B851Eu
        val expected =
            listOf(SizedCRC32Watermark.TYPE_TAG.toByte()) +
                expectedSize.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedCRC32Watermark.new(content)
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
    fun sizedCrc32Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x735EA3E1u
        val size =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Watermark.SIZE_SIZE +
                    SizedCRC32Watermark.CHECKSUM_SIZE +
                    content.size
            ).toUInt()
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedCRC32Watermark",
                3u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedCRC32Watermark(watermarkContent)
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
    fun sizedCrc32Watermark_mismatchedSizeInvalidChecksum_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0xD3B90546u
        val invalidCrc32 = 0xFFFFFFFFu
        val invalidSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedCRC32Watermark.SIZE_SIZE +
                    SizedCRC32Watermark.CHECKSUM_SIZE +
                    content.size +
                    1
            ).toUInt()
        val watermarkContent =
            listOf(SizedCRC32Watermark.TYPE_TAG.toByte()) +
                invalidSize.toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedCRC32Watermark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Trendmark.InvalidChecksumWarning(
                "Trendmark.SizedCRC32Watermark",
                invalidCrc32,
                expectedCrc32,
            ),
        )

        // Act
        val watermark = SizedCRC32Watermark(watermarkContent)
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
    fun sha3256Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedHash =
            listOf<Byte>(
                -34, 2, 101, -35, 107, 22, -96, -76, -85, 5, -92, 57, 54, -64, 115, 18, 79, 102,
                -94, -86, 85, -77, -100, 43, 48, -74, 25, -34, 28, 17, -55, 80,
            )
        val expectedContent =
            listOf(SHA3256Watermark.TYPE_TAG.toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Watermark.new(content)
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
    fun sha3256Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedHash =
            listOf<Byte>(
                94, -5, 11, -28, 39, 90, -127, 48, 78, -60, -46, 88, 37, 14, -58, -35, 9, -124,
                -49, -76, -26, 59, -96, -35, -99, 98, 110, -6, -49, 47, -52, 25,
            )
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SHA3256Watermark",
                4u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Watermark(watermarkContent)
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
    fun sha3256Watermark_invalidHash_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                -34, 2, 101, -35, 107, 22, -96, -76, -85, 5, -92, 57, 54, -64, 115, 18, 79, 102,
                -94, -86, 85, -77, -100, 43, 48, -74, 25, -34, 28, 17, -55, 80,
            )
        val expectedStatus =
            Trendmark.InvalidHashWarning(
                "Trendmark.SHA3256Watermark",
                invalidHash,
                expectedHash,
            ).into()
        val watermarkContent = listOf(SHA3256Watermark.TYPE_TAG.toByte()) + invalidHash + content

        // Act
        val watermark = SHA3256Watermark(watermarkContent)
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
    fun sizedSha3256Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Watermark.SIZE_SIZE +
                    SizedSHA3256Watermark.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -14, 23, -91, -82, 67, -59, 112, -94, 51, 43, -75, -112, 96, 35, 69, -38, 109,
                53, -45, 52, -107, 92, 23, -125, -20, -20, 46, 73, 102, 69, -55, 26,
            )
        val expectedContent =
            listOf(SizedSHA3256Watermark.TYPE_TAG.toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Watermark.new(content)
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
    fun sizedSha3256Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Watermark.SIZE_SIZE +
                    SizedSHA3256Watermark.HASH_SIZE +
                    content.size
            ).toUInt()
        val expectedHash =
            listOf<Byte>(
                -68, 110, 96, 52, -108, 67, 119, -3, 58, 85, 89, 2, 25, 81, 60, 105, 63, -54, 8,
                -50, -44, 69, 39, 37, -21, -72, -75, -56, -90, 97, 36, 63,
            )
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedSHA3256Watermark",
                5u,
                255u,
            ).into().toString()

        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Watermark(watermarkContent)
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
    fun sizedSha3256Watermark_mismatchedSizeInvalidHash_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val invalidSize =
            (
                TrendmarkInterface.TAG_SIZE +
                    SizedSHA3256Watermark.SIZE_SIZE +
                    SizedSHA3256Watermark.HASH_SIZE +
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
                "Trendmark.SizedSHA3256Watermark",
                invalidSize.toInt(),
                invalidSize.toInt() - 1,
            ),
        )
        expectedStatus.addEvent(
            Trendmark.InvalidHashWarning(
                "Trendmark.SizedSHA3256Watermark",
                invalidHash,
                expectedHash,
            ),
        )

        val watermarkContent =
            listOf(SizedSHA3256Watermark.TYPE_TAG.toByte()) +
                invalidSize.toUInt().toBytesLittleEndian() +
                invalidHash +
                content

        // Act
        val watermark = SizedSHA3256Watermark(watermarkContent)
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
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val rawWatermark = RawWatermark.new(content)
        val sizedWatermark = SizedWatermark.new(content)
        val crc32Watermark = CRC32Watermark.new(content)
        val sizedCRC32Watermark = SizedCRC32Watermark.new(content)
        val sha3256Watermark = SHA3256Watermark.new(content)
        val sizedSHA3256Watermark = SizedSHA3256Watermark.new(content)

        // Act
        val parsedPlainWatermark = Trendmark.parse(rawWatermark.watermarkContent)
        val parsedSizedWatermark = Trendmark.parse(sizedWatermark.watermarkContent)
        val parsedCRC32Watermark = Trendmark.parse(crc32Watermark.watermarkContent)
        val parsedSizedCRC32Watermark = Trendmark.parse(sizedCRC32Watermark.watermarkContent)
        val parsedSHA3256Watermark = Trendmark.parse(sha3256Watermark.watermarkContent)
        val parsedSizedSHA3256Watermark = Trendmark.parse(sizedSHA3256Watermark.watermarkContent)

        // Assert
        assertTrue(parsedPlainWatermark.isSuccess)
        var parsedWatermark = parsedPlainWatermark.value!!
        assertTrue(parsedWatermark is RawWatermark)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(rawWatermark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSizedWatermark.isSuccess)
        parsedWatermark = parsedSizedWatermark.value!!
        assertTrue(parsedWatermark is SizedWatermark)
        assertEquals(sizedWatermark.extractSize().value!!, parsedWatermark.extractSize().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedWatermark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedCRC32Watermark.isSuccess)
        parsedWatermark = parsedCRC32Watermark.value!!
        assertTrue(parsedWatermark is CRC32Watermark)
        assertEquals(
            crc32Watermark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(crc32Watermark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSizedCRC32Watermark.isSuccess)
        parsedWatermark = parsedSizedCRC32Watermark.value!!
        assertTrue(parsedWatermark is SizedCRC32Watermark)
        assertEquals(
            sizedCRC32Watermark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedCRC32Watermark.extractChecksum().value!!,
            parsedWatermark.extractChecksum().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedCRC32Watermark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSHA3256Watermark.isSuccess)
        parsedWatermark = parsedSHA3256Watermark.value!!
        assertTrue(parsedWatermark is SHA3256Watermark)
        assertEquals(sha3256Watermark.extractHash().value!!, parsedWatermark.extractHash().value!!)
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sha3256Watermark.watermarkContent, parsedWatermark.watermarkContent)

        assertTrue(parsedSizedSHA3256Watermark.isSuccess)
        parsedWatermark = parsedSizedSHA3256Watermark.value!!
        assertTrue(parsedWatermark is SizedSHA3256Watermark)
        assertEquals(
            sizedSHA3256Watermark.extractSize().value!!,
            parsedWatermark.extractSize().value!!,
        )
        assertEquals(
            sizedSHA3256Watermark.extractHash().value!!,
            parsedWatermark.extractHash().value!!,
        )
        assertEquals(content, parsedWatermark.getContent().value)
        assertEquals(sizedSHA3256Watermark.watermarkContent, parsedWatermark.watermarkContent)
    }
}
