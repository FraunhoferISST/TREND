/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.watermarks

import de.fraunhofer.isst.trend.watermarker.helper.CRC32
import de.fraunhofer.isst.trend.watermarker.helper.fromBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.helper.toHexString
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import org.kotlincrypto.hash.sha3.SHA3_256
import kotlin.collections.ArrayList

/**
 * Trendmark defines a list of Watermarks with specific format
 *
 * The constructor expects bytes that represent the given type.
 * To create a new watermark with arbitrary content the companion function `new` of that type must
 * be used.
 *
 */
sealed class Trendmark(val tag: UByte, content: List<Byte>) : Watermark(content) {
    abstract fun getContent(): List<Byte>

    abstract fun validate(): Status

    companion object {
        /** Number of bytes used as tag representing the type of the watermark */
        const val TAG_SIZE: Int = 1
        const val SOURCE = "Trendmark"

        fun parse(bytes: List<Byte>): Result<Trendmark> {
            if (bytes.size < TAG_SIZE) {
                return NotEnoughDataError.into<_>()
            }

            val watermark =
                when (val tag = extractTag(bytes)) {
                    PlainWatermark.TAG -> PlainWatermark(bytes)
                    SizedWatermark.TAG -> SizedWatermark(bytes)
                    CRC32Watermark.TAG -> CRC32Watermark(bytes)
                    SizedCRC32Watermark.TAG -> SizedCRC32Watermark(bytes)
                    SHA3256Watermark.TAG -> SHA3256Watermark(bytes)
                    SizedSHA3256Watermark.TAG -> SizedSHA3256Watermark(bytes)
                    else -> return UnknownTagError(tag).into<_>()
                }
            val status = watermark.validate()
            return status.into(watermark)
        }

        private fun extractTag(content: List<Byte>): UByte {
            check(TAG_SIZE == 1)
            require(content.isNotEmpty()) { "Cannot extract tag from empty watermark." }
            return content[0].toUByte()
        }

        @OptIn(ExperimentalStdlibApi::class)
        internal val hexFormat =
            HexFormat {
                number {
                    prefix = "0x"
                }
            }
    }

    fun extractTag(): UByte = Companion.extractTag(rawContent)

    object NotEnoughDataError : Event.Error(SOURCE) {
        override fun getMessage(): String = "More bytes are required for a valid Trendmark."
    }

    object EmptyError : Event.Error(SOURCE) {
        override fun getMessage(): String = "Cannot validate an empty watermark."
    }

    class UnknownTagError(val tag: UByte) : Event.Error(SOURCE) {
        override fun getMessage(): String = "Unknown watermark type: $tag."
    }

    class InvalidTagError(source: String, val expectedTag: UByte, val actualTag: UByte) :
        Event.Error(source) {
        override fun getMessage(): String = "Expected tag :$expectedTag, but was: $actualTag."
    }

    class MismatchedSizeWarning(source: String, val expectedSize: Int, val actualSize: Int) :
        Event.Warning(source) {
        override fun getMessage(): String =
            "Expected $expectedSize bytes, but extracted $actualSize bytes."
    }

    class InvalidChecksumWarning(
        source: String,
        val expectedChecksum: UInt,
        val actualChecksum: UInt,
    ) : Event.Warning(source) {
        @OptIn(ExperimentalStdlibApi::class)
        override fun getMessage(): String {
            val expectedHex = expectedChecksum.toHexString(hexFormat)
            val actualHex = actualChecksum.toHexString(hexFormat)
            return "Expected checksum: $expectedHex, but was: $actualHex."
        }
    }

    class InvalidHashWarning(
        source: String,
        val expectedHash: List<Byte>,
        val actualHash: List<Byte>,
    ) : Event.Warning(source) {
        override fun getMessage(): String {
            val expectedHex = expectedHash.toHexString()
            val actualHex = actualHash.toHexString()
            return "Expected hash: $expectedHex, but was: $actualHex."
        }
    }
}

class PlainWatermark(content: List<Byte>) : Trendmark(TAG, content) {
    companion object {
        const val SOURCE = "Trendmark.PlainWatermark"
        const val TAG: UByte = 0u

        fun new(content: List<Byte>): PlainWatermark {
            val watermark = content.toMutableList()
            watermark.add(0, TAG.toByte())

            return PlainWatermark(watermark)
        }
    }

    override fun getContent(): List<Byte> = rawContent.drop(TAG_SIZE)

    override fun validate(): Status {
        return if (rawContent.isEmpty()) {
            EmptyError.into()
        } else if (extractTag() != TAG) {
            InvalidTagError(SOURCE, TAG, extractTag()).into()
        } else {
            Status.success()
        }
    }
}

class SizedWatermark(content: List<Byte>) : Trendmark(TAG, content) {
    companion object {
        const val SOURCE = "Trendmark.SizedWatermark"
        const val TAG: UByte = 1u

        /** Number of bytes used to specify the length of the watermark */
        const val SIZE_SIZE: Int = 4
        const val SIZE_START_INDEX = TAG_SIZE
        const val SIZE_END_INDEX = SIZE_START_INDEX + SIZE_SIZE

        fun new(content: List<Byte>): SizedWatermark {
            val size = TAG_SIZE + SIZE_SIZE + content.size
            val watermark = ArrayList<Byte>(size)
            val encodedSize = size.toUInt().toBytesLittleEndian()

            watermark.add(TAG.toByte())
            watermark.addAll(encodedSize)
            watermark.addAll(content)

            return SizedWatermark(watermark)
        }
    }

    override fun getContent(): List<Byte> = rawContent.drop(TAG_SIZE + SIZE_SIZE)

    override fun validate(): Status {
        return if (rawContent.isEmpty()) {
            EmptyError.into()
        } else if (extractTag() != TAG) {
            InvalidTagError(SOURCE, TAG, extractTag()).into()
        } else {
            val parsedSize = extractSize()

            if (parsedSize != rawContent.size) {
                MismatchedSizeWarning(SOURCE, parsedSize, rawContent.size).into()
            } else {
                Status.success()
            }
        }
    }

    fun extractSize(): Int {
        return UInt.fromBytesLittleEndian(rawContent.subList(SIZE_START_INDEX, SIZE_END_INDEX))
            .toInt()
    }
}

class CRC32Watermark(content: List<Byte>) : Trendmark(TAG, content) {
    companion object {
        const val SOURCE = "Trendmark.CRC32Watermark"
        const val TAG: UByte = 2u
        const val CHECKSUM_SIZE = 4
        const val CHECKSUM_START_INDEX = TAG_SIZE
        const val CHECKSUM_END_INDEX = CHECKSUM_START_INDEX + CHECKSUM_SIZE

        fun new(content: List<Byte>): CRC32Watermark {
            val checksum = calculateChecksum(content).toBytesLittleEndian()
            val watermark = ArrayList<Byte>(TAG_SIZE + CHECKSUM_SIZE + content.size)

            watermark.add(TAG.toByte())
            watermark.addAll(checksum)
            watermark.addAll(content)

            return CRC32Watermark(watermark)
        }

        fun calculateChecksum(bytes: List<Byte>): UInt = CRC32.checksum(bytes)
    }

    override fun getContent(): List<Byte> = rawContent.drop(TAG_SIZE + CHECKSUM_SIZE)

    override fun validate(): Status {
        if (rawContent.isEmpty()) {
            return EmptyError.into()
        } else if (extractTag() != TAG) {
            return InvalidTagError(SOURCE, TAG, extractTag()).into()
        }

        val parsedChecksum = extractChecksum()
        val calculatedChecksum = calculateChecksum(getContent())

        return if (parsedChecksum != calculatedChecksum) {
            InvalidChecksumWarning(SOURCE, parsedChecksum, calculatedChecksum).into()
        } else {
            Status.success()
        }
    }

    fun extractChecksum(): UInt {
        val checksumBytes = rawContent.subList(CHECKSUM_START_INDEX, CHECKSUM_END_INDEX)
        return UInt.fromBytesLittleEndian(checksumBytes)
    }
}

class SizedCRC32Watermark(content: List<Byte>) : Trendmark(TAG, content) {
    companion object {
        const val SOURCE = "Trendmark.SizedCRC32Watermark"
        const val TAG: UByte = 3u
        const val SIZE_SIZE: Int = 4
        const val SIZE_START_INDEX = TAG_SIZE
        const val SIZE_END_INDEX = SIZE_START_INDEX + SIZE_SIZE
        const val CHECKSUM_SIZE = 4
        const val CHECKSUM_START_INDEX = SIZE_END_INDEX
        const val CHECKSUM_END_INDEX = CHECKSUM_START_INDEX + CHECKSUM_SIZE

        fun new(content: List<Byte>): SizedCRC32Watermark {
            val size = TAG_SIZE + SIZE_SIZE + CHECKSUM_SIZE + content.size
            val encodedSize = size.toUInt().toBytesLittleEndian()
            val checksum = calculateChecksum(content).toBytesLittleEndian()
            val watermark = ArrayList<Byte>(size)

            watermark.add(TAG.toByte())
            watermark.addAll(encodedSize)
            watermark.addAll(checksum)
            watermark.addAll(content)

            return SizedCRC32Watermark(watermark)
        }

        fun calculateChecksum(bytes: List<Byte>): UInt = CRC32.checksum(bytes)
    }

    override fun getContent(): List<Byte> = rawContent.drop(TAG_SIZE + SIZE_SIZE + CHECKSUM_SIZE)

    override fun validate(): Status {
        if (rawContent.isEmpty()) {
            return EmptyError.into()
        } else if (extractTag() != TAG) {
            return InvalidTagError(SOURCE, TAG, extractTag()).into()
        }

        val parsedSize = extractSize()
        val parsedChecksum = extractChecksum()
        val calculatedChecksum = calculateChecksum(getContent())

        val status = Status.success()
        if (parsedSize != rawContent.size) {
            status.addEvent(MismatchedSizeWarning(SOURCE, parsedSize, rawContent.size))
        }
        if (parsedChecksum != calculatedChecksum) {
            status.addEvent(InvalidChecksumWarning(SOURCE, parsedChecksum, calculatedChecksum))
        }

        return status
    }

    fun extractSize(): Int {
        return UInt.fromBytesLittleEndian(rawContent.subList(SIZE_START_INDEX, SIZE_END_INDEX))
            .toInt()
    }

    fun extractChecksum(): UInt {
        val checksumBytes = rawContent.subList(CHECKSUM_START_INDEX, CHECKSUM_END_INDEX)
        return UInt.fromBytesLittleEndian(checksumBytes)
    }
}

class SHA3256Watermark(content: List<Byte>) : Trendmark(TAG, content) {
    companion object {
        const val SOURCE = "Trendmark.SHA3256Watermark"
        const val TAG: UByte = 4u
        const val HASH_SIZE = 32
        const val HASH_START_INDEX = TAG_SIZE
        const val HASH_END_INDEX = HASH_START_INDEX + HASH_SIZE

        fun new(content: List<Byte>): SHA3256Watermark {
            val size = TAG_SIZE + HASH_SIZE + content.size
            val hash = calculateHash(content)

            val watermark = ArrayList<Byte>(size)
            watermark.add(TAG.toByte())
            watermark.addAll(hash)
            watermark.addAll(content)

            return SHA3256Watermark(watermark)
        }

        fun calculateHash(bytes: List<Byte>): List<Byte> {
            val hashAlgorithm = SHA3_256()
            hashAlgorithm.update(TAG.toByte())
            hashAlgorithm.update(bytes.toByteArray())
            return hashAlgorithm.digest().asList()
        }
    }

    fun extractHash(): List<Byte> {
        return rawContent.subList(HASH_START_INDEX, HASH_END_INDEX)
    }

    override fun getContent(): List<Byte> = rawContent.drop(TAG_SIZE + HASH_SIZE)

    override fun validate(): Status {
        if (rawContent.isEmpty()) {
            return EmptyError.into()
        } else if (extractTag() != TAG) {
            return InvalidTagError(SOURCE, TAG, extractTag()).into()
        }

        val extractedHash = extractHash()
        val calculatedHash = calculateHash(getContent())

        return if (extractedHash != calculatedHash) {
            InvalidHashWarning(SOURCE, extractedHash, calculatedHash).into()
        } else {
            Status.success()
        }
    }
}

class SizedSHA3256Watermark(content: List<Byte>) : Trendmark(TAG, content) {
    companion object {
        const val SOURCE = "Trendmark.SizedSHA3256Watermark"
        const val TAG: UByte = 5u
        const val SIZE_SIZE = 4
        const val SIZE_START_INDEX = TAG_SIZE
        const val SIZE_END_INDEX = SIZE_START_INDEX + SIZE_SIZE
        const val HASH_SIZE = 32
        const val HASH_START_INDEX = SIZE_END_INDEX
        const val HASH_END_INDEX = HASH_START_INDEX + HASH_SIZE

        fun new(content: List<Byte>): SizedSHA3256Watermark {
            val size = TAG_SIZE + SIZE_SIZE + HASH_SIZE + content.size
            val encodedSize = size.toUInt().toBytesLittleEndian()
            val hash = calculateHash(content)

            val watermark = ArrayList<Byte>(size)
            watermark.add(TAG.toByte())
            watermark.addAll(encodedSize)
            watermark.addAll(hash)
            watermark.addAll(content)

            return SizedSHA3256Watermark(watermark)
        }

        fun calculateHash(bytes: List<Byte>): List<Byte> {
            val hashAlgorithm = SHA3_256()
            hashAlgorithm.update(TAG.toByte())
            hashAlgorithm.update(bytes.toByteArray())
            return hashAlgorithm.digest().asList()
        }
    }

    override fun getContent(): List<Byte> = rawContent.drop(TAG_SIZE + SIZE_SIZE + HASH_SIZE)

    override fun validate(): Status {
        if (rawContent.isEmpty()) {
            return EmptyError.into()
        } else if (extractTag() != TAG) {
            return InvalidTagError(SOURCE, TAG, extractTag()).into()
        }

        val parsedSize = extractSize()
        val extractedHash = extractHash()
        val calculatedHash = calculateHash(getContent())

        val status = Status.success()
        if (parsedSize != rawContent.size) {
            status.addEvent(MismatchedSizeWarning(SOURCE, parsedSize, rawContent.size))
        }
        if (extractedHash != calculatedHash) {
            status.addEvent(InvalidHashWarning(SOURCE, extractedHash, calculatedHash))
        }

        return status
    }

    fun extractSize(): Int {
        return UInt.fromBytesLittleEndian(rawContent.subList(SIZE_START_INDEX, SIZE_END_INDEX))
            .toInt()
    }

    fun extractHash(): List<Byte> {
        return rawContent.subList(HASH_START_INDEX, HASH_END_INDEX)
    }
}
