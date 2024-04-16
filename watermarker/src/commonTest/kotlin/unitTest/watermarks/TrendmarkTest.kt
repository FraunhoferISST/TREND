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
import de.fraunhofer.isst.trend.watermarker.watermarks.PlainWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedCRC32Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedSHA3256Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.SizedWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrendmarkTest {
    @Test
    fun plainWatermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expected = listOf(PlainWatermark.TYPE_TAG.toByte()) + content

        // Act
        val watermark = PlainWatermark.new(content)

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expected, watermark.rawContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun plainWatermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val watermarkContent = listOf((-1).toByte()) + content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.PlainWatermark",
                0u,
                255u,
            ).into().toString()

        // Act
        val watermark = PlainWatermark(watermarkContent)
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedWatermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize = Trendmark.TAG_SIZE + SizedWatermark.SIZE_SIZE + content.size
        val expected =
            listOf(SizedWatermark.TYPE_TAG.toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedWatermark.new(content)

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedSize, watermark.extractSize())
        assertEquals(expected, watermark.rawContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sizedWatermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val size = Trendmark.TAG_SIZE + SizedWatermark.SIZE_SIZE + content.size
        val watermarkContent =
            listOf((-1).toByte()) +
                size.toUInt().toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.InvalidTagError(
                "Trendmark.SizedWatermark",
                1u,
                255u,
            ).into().toString()

        // Act
        val watermark = SizedWatermark(watermarkContent)
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(size, watermark.extractSize())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedWatermark_mismatchedSize_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val invalidSize = Trendmark.TAG_SIZE + SizedWatermark.SIZE_SIZE + content.size + 1
        val watermarkContent =
            listOf(SizedWatermark.TYPE_TAG.toByte()) +
                invalidSize.toUInt().toBytesLittleEndian() +
                content
        val expectedStatus =
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedWatermark",
                invalidSize,
                invalidSize - 1,
            ).into().toString()

        // Act
        val watermark = SizedWatermark(watermarkContent)
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(invalidSize, watermark.extractSize())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun crc32Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x358AD45Du
        val expected =
            listOf(CRC32Watermark.TYPE_TAG.toByte()) + expectedCrc32.toBytesLittleEndian() + content

        // Act
        val watermark = CRC32Watermark.new(content)

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedCrc32, watermark.extractChecksum())
        assertEquals(expected, watermark.rawContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun crc32Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x358AD45Du
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedCrc32, watermark.extractChecksum())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun crc32Watermark_invalidChecksum_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x358AD45Du
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(invalidCrc32, watermark.extractChecksum())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun sizedCrc32Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            Trendmark.TAG_SIZE +
                SizedCRC32Watermark.SIZE_SIZE +
                SizedCRC32Watermark.CHECKSUM_SIZE +
                content.size
        val expectedCrc32 = 0x358AD45Du
        val expected =
            listOf(SizedCRC32Watermark.TYPE_TAG.toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                expectedCrc32.toBytesLittleEndian() +
                content

        // Act
        val watermark = SizedCRC32Watermark.new(content)

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedSize, watermark.extractSize())
        assertEquals(expectedCrc32, watermark.extractChecksum())
        assertEquals(expected, watermark.rawContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sizedCrc32Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x358AD45Du
        val expectedSize =
            Trendmark.TAG_SIZE +
                SizedCRC32Watermark.SIZE_SIZE +
                SizedCRC32Watermark.CHECKSUM_SIZE +
                content.size
        val watermarkContent =
            listOf((-1).toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedCrc32, watermark.extractChecksum())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedCrc32Watermark_mismatchedSizeInvalidChecksum_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedCrc32 = 0x358AD45Du
        val invalidCrc32 = 0xFFFFFFFFu
        val invalidSize =
            Trendmark.TAG_SIZE +
                SizedCRC32Watermark.SIZE_SIZE +
                SizedCRC32Watermark.CHECKSUM_SIZE +
                content.size +
                1
        val watermarkContent =
            listOf(SizedCRC32Watermark.TYPE_TAG.toByte()) +
                invalidSize.toUInt().toBytesLittleEndian() +
                invalidCrc32.toBytesLittleEndian() +
                content
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedCRC32Watermark",
                invalidSize,
                invalidSize - 1,
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(invalidCrc32, watermark.extractChecksum())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun sha3256Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedHash =
            listOf<Byte>(
                96, 68, -90, -92, -105, 81, 104, 86, -3, 70, -64, -26, -72, -39, -71, 115, 14, -73,
                62, 89, 112, -111, -83, -16, 75, 74, 70, 84, 22, -99, 101, 39,
            )
        val expectedContent =
            listOf(SHA3256Watermark.TYPE_TAG.toByte()) + expectedHash + content

        // Act
        val watermark = SHA3256Watermark.new(content)

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedHash, watermark.extractHash())
        assertEquals(expectedContent, watermark.rawContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sha3256Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedHash =
            listOf<Byte>(
                96, 68, -90, -92, -105, 81, 104, 86, -3, 70, -64, -26, -72, -39, -71, 115, 14, -73,
                62, 89, 112, -111, -83, -16, 75, 74, 70, 84, 22, -99, 101, 39,
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedHash, watermark.extractHash())
        assertEquals(watermarkContent, watermark.rawContent)
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
                96, 68, -90, -92, -105, 81, 104, 86, -3, 70, -64, -26, -72, -39, -71, 115, 14, -73,
                62, 89, 112, -111, -83, -16, 75, 74, 70, 84, 22, -99, 101, 39,
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(invalidHash, watermark.extractHash())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun sizedSha3256Watermark_creation_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            Trendmark.TAG_SIZE +
                SizedSHA3256Watermark.SIZE_SIZE +
                SizedSHA3256Watermark.HASH_SIZE +
                content.size
        val expectedHash =
            listOf<Byte>(
                32, -89, -126, -79, -44, -17, -77, -73, 0, 122, -59, -47, -95, -19, 55, -19, -90,
                -41, 6, -3, -118, 33, 105, -76, -46, 122, 12, 21, -97, -39, 18, 3,
            )
        val expectedContent =
            listOf(SizedSHA3256Watermark.TYPE_TAG.toByte()) +
                expectedSize.toUInt().toBytesLittleEndian() +
                expectedHash +
                content

        // Act
        val watermark = SizedSHA3256Watermark.new(content)

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedSize, watermark.extractSize())
        assertEquals(expectedHash, watermark.extractHash())
        assertEquals(expectedContent, watermark.rawContent)
        assertTrue(watermark.validate().isSuccess)
    }

    @Test
    fun sizedSha3256Watermark_invalidTag_error() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val expectedSize =
            Trendmark.TAG_SIZE +
                SizedSHA3256Watermark.SIZE_SIZE +
                SizedSHA3256Watermark.HASH_SIZE +
                content.size
        val expectedHash =
            listOf<Byte>(
                96, 68, -90, -92, -105, 81, 104, 86, -3, 70, -64, -26, -72, -39, -71, 115, 14, -73,
                62, 89, 112, -111, -83, -16, 75, 74, 70, 84, 22, -99, 101, 39,
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(expectedSize, watermark.extractSize())
        assertEquals(expectedHash, watermark.extractHash())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isError)
        assertEquals(expectedStatus, status.toString())
    }

    @Test
    fun sizedSha3256Watermark_mismatchedSizeInvalidHash_warning() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val invalidSize =
            Trendmark.TAG_SIZE +
                SizedSHA3256Watermark.SIZE_SIZE +
                SizedSHA3256Watermark.HASH_SIZE +
                content.size +
                1
        val invalidHash = (0 until 32).map { it.toByte() }.toList()
        val expectedHash =
            listOf<Byte>(
                32, -89, -126, -79, -44, -17, -77, -73, 0, 122, -59, -47, -95, -19, 55, -19, -90,
                -41, 6, -3, -118, 33, 105, -76, -46, 122, 12, 21, -97, -39, 18, 3,
            )
        val expectedStatus = Status.success()
        expectedStatus.addEvent(
            Trendmark.MismatchedSizeWarning(
                "Trendmark.SizedSHA3256Watermark",
                invalidSize,
                invalidSize - 1,
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
        val status = watermark.validate()

        // Assert
        assertEquals(content, watermark.getContent())
        assertEquals(invalidSize, watermark.extractSize())
        assertEquals(invalidHash, watermark.extractHash())
        assertEquals(watermarkContent, watermark.rawContent)
        assertTrue(status.isWarning)
        assertEquals(expectedStatus.toString(), status.toString())
    }

    @Test
    fun parse_valid_success() {
        // Arrange
        val content = "Lorem Ipsum".encodeToByteArray().asList()
        val plainWatermark = PlainWatermark.new(content)
        val sizedWatermark = SizedWatermark.new(content)
        val crc32Watermark = CRC32Watermark.new(content)
        val sizedCRC32Watermark = SizedCRC32Watermark.new(content)
        val sha3256Watermark = SHA3256Watermark.new(content)
        val sizedSHA3256Watermark = SizedSHA3256Watermark.new(content)

        // Act
        val parsedPlainWatermark = Trendmark.parse(plainWatermark.rawContent)
        val parsedSizedWatermark = Trendmark.parse(sizedWatermark.rawContent)
        val parsedCRC32Watermark = Trendmark.parse(crc32Watermark.rawContent)
        val parsedSizedCRC32Watermark = Trendmark.parse(sizedCRC32Watermark.rawContent)
        val parsedSHA3256Watermark = Trendmark.parse(sha3256Watermark.rawContent)
        val parsedSizedSHA3256Watermark = Trendmark.parse(sizedSHA3256Watermark.rawContent)

        // Assert
        assertTrue(parsedPlainWatermark.isSuccess)
        var parsedWatermark = parsedPlainWatermark.value!!
        assertTrue(parsedWatermark is PlainWatermark)
        assertEquals(content, parsedWatermark.getContent())
        assertEquals(plainWatermark.rawContent, parsedWatermark.rawContent)

        assertTrue(parsedSizedWatermark.isSuccess)
        parsedWatermark = parsedSizedWatermark.value!!
        assertTrue(parsedWatermark is SizedWatermark)
        assertEquals(sizedWatermark.extractSize(), parsedWatermark.extractSize())
        assertEquals(content, parsedWatermark.getContent())
        assertEquals(sizedWatermark.rawContent, parsedWatermark.rawContent)

        assertTrue(parsedCRC32Watermark.isSuccess)
        parsedWatermark = parsedCRC32Watermark.value!!
        assertTrue(parsedWatermark is CRC32Watermark)
        assertEquals(crc32Watermark.extractChecksum(), parsedWatermark.extractChecksum())
        assertEquals(content, parsedWatermark.getContent())
        assertEquals(crc32Watermark.rawContent, parsedWatermark.rawContent)

        assertTrue(parsedSizedCRC32Watermark.isSuccess)
        parsedWatermark = parsedSizedCRC32Watermark.value!!
        assertTrue(parsedWatermark is SizedCRC32Watermark)
        assertEquals(sizedCRC32Watermark.extractSize(), parsedWatermark.extractSize())
        assertEquals(sizedCRC32Watermark.extractChecksum(), parsedWatermark.extractChecksum())
        assertEquals(content, parsedWatermark.getContent())
        assertEquals(sizedCRC32Watermark.rawContent, parsedWatermark.rawContent)

        assertTrue(parsedSHA3256Watermark.isSuccess)
        parsedWatermark = parsedSHA3256Watermark.value!!
        assertTrue(parsedWatermark is SHA3256Watermark)
        assertEquals(sha3256Watermark.extractHash(), parsedWatermark.extractHash())
        assertEquals(content, parsedWatermark.getContent())
        assertEquals(sha3256Watermark.rawContent, parsedWatermark.rawContent)

        assertTrue(parsedSizedSHA3256Watermark.isSuccess)
        parsedWatermark = parsedSizedSHA3256Watermark.value!!
        assertTrue(parsedWatermark is SizedSHA3256Watermark)
        assertEquals(sizedSHA3256Watermark.extractSize(), parsedWatermark.extractSize())
        assertEquals(sizedSHA3256Watermark.extractHash(), parsedWatermark.extractHash())
        assertEquals(content, parsedWatermark.getContent())
        assertEquals(sizedSHA3256Watermark.rawContent, parsedWatermark.rawContent)
    }
}
