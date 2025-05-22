/*
 * Copyright (c) 2024-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.watermarks

import de.fraunhofer.isst.innamark.watermarker.helper.CRC32
import de.fraunhofer.isst.innamark.watermarker.helper.Compression
import de.fraunhofer.isst.innamark.watermarker.helper.fromBytesLittleEndian
import de.fraunhofer.isst.innamark.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.innamark.watermarker.helper.toHexString
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Event
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTag.FailedInnamarkExtractionsWarning
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTag.IncompleteTagError
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTag.InvalidTagError
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTagInterface.Companion.TAG_SIZE
import org.kotlincrypto.hash.sha3.SHA3_256
import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

@JsExport
sealed interface InnamarkTagInterface {
    companion object {
        /** Number of bytes used as tag representing the type of the watermark */
        const val TAG_SIZE: Int = 1
    }

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    fun getTag(): UByte

    /** Constant function that returns the name of the specific InnamarkTag */
    fun getSource(): String

    /** Extracts the encoded tag from the bytes of the InnamarkTag */
    fun extractTag(): UByte {
        check(TAG_SIZE == 1)
        val content = getRawContent()
        require(content.size >= TAG_SIZE)
        return content.first().toUByte()
    }

    /** Returns the decoded information stored in the InnamarkTag */
    fun getContent(): Result<ByteArray>

    /** Returns the raw bytes of the watermark */
    fun getRawContent(): ByteArray

    /** Updates the raw bytes of the watermark */
    fun setRawContent(content: ByteArray)

    /** Checks if the bytes represent a valid InnamarkTag of the given class */
    fun validate(): Status {
        val content = getRawContent()
        if (content.size < TAG_SIZE) {
            return IncompleteTagError.into()
        }
        val status = Status.success()
        val extractedTag = extractTag()
        val expectedTag = getTag()

        if (extractedTag != expectedTag) {
            status.addEvent(InvalidTagError(getSource(), expectedTag, extractedTag))
        }

        if (this is InnamarkTag.Sized) {
            status.appendStatus(validateSize())
        }
        if (this is InnamarkTag.Checksum) {
            status.appendStatus(validateChecksum())
        }
        if (this is InnamarkTag.Hash) {
            status.appendStatus(validateHash())
        }
        if (this is InnamarkTag.Compressed) {
            status.appendStatus(validateCompression())
        }

        return status
    }
}

/**
 * Innamark defines a list of Watermarks with specific formats. The format is encoded in the first
 * byte of the watermark, which allows parsing unknown types of InnamarkTags. The implemented
 * variants of InnamarkTag allow encoding additional information like the size or a hash into the
 * watermark. For detailed information about the different formats, see
 * [Innamark.md](https://github.com/FraunhoferISST/TREND/blob/main/docs/Innamark.md)
 *
 * The constructor expects bytes that represent the given type.
 * To create a new watermark with arbitrary content, the companion function `new` of that type must
 * be used.
 *
 * @param content: expects bytes that represent a InnamarkTag.
 */
@JsExport
sealed class InnamarkTag(
    content: ByteArray,
) : Watermark(content), InnamarkTagInterface {
    companion object {
        const val SOURCE = "InnamarkTag"

        /**
         * Parses [input] as InnamarkTag.
         *
         * Returns an error if:
         *  - The first byte is not a valid Innamark tag
         *  - The `validate()` function of the created InnamarkTag returns an error.
         */
        @JvmStatic
        fun parse(input: ByteArray): Result<InnamarkTag> {
            if (input.size < TAG_SIZE) {
                return NotEnoughDataError(SOURCE, TAG_SIZE).into<_>()
            }

            val watermark =
                when (val tag = extractTag(input)) {
                    RawInnamarkTag.TYPE_TAG -> RawInnamarkTag(input)
                    SizedInnamarkTag.TYPE_TAG -> SizedInnamarkTag(input)
                    CRC32InnamarkTag.TYPE_TAG -> CRC32InnamarkTag(input)
                    SizedCRC32InnamarkTag.TYPE_TAG -> SizedCRC32InnamarkTag(input)
                    SHA3256InnamarkTag.TYPE_TAG -> SHA3256InnamarkTag(input)
                    SizedSHA3256InnamarkTag.TYPE_TAG -> SizedSHA3256InnamarkTag(input)
                    CompressedRawInnamarkTag.TYPE_TAG -> CompressedRawInnamarkTag(input)
                    CompressedSizedInnamarkTag.TYPE_TAG -> CompressedSizedInnamarkTag(input)
                    CompressedCRC32InnamarkTag.TYPE_TAG -> CompressedCRC32InnamarkTag(input)
                    CompressedSizedCRC32InnamarkTag.TYPE_TAG ->
                        CompressedSizedCRC32InnamarkTag(input)

                    CompressedSHA3256InnamarkTag.TYPE_TAG -> CompressedSHA3256InnamarkTag(input)
                    CompressedSizedSHA3256InnamarkTag.TYPE_TAG ->
                        CompressedSizedSHA3256InnamarkTag(input)

                    else -> return UnknownTagError(tag).into<_>()
                }
            val status = watermark.validate()
            return status.into(watermark)
        }

        /**
         * Parses [watermark] as InnamarkTag.
         *
         * Returns an error if:
         *  - The first byte is not a valid Innamark tag
         *  - The `validate()` function of the created InnamarkTag returns an error.
         */
        @JvmStatic
        fun fromWatermark(watermark: Watermark): Result<InnamarkTag> =
            parse(watermark.watermarkContent)

        @JvmStatic
        private fun extractTag(content: ByteArray): UByte {
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

    /** Returns the tag byte from the watermark content */
    override fun extractTag(): UByte = Companion.extractTag(watermarkContent)

    /** Returns the all bytes of the watermark */
    override fun getRawContent(): ByteArray = watermarkContent

    /** Sets all bytes of the watermark to [content] */
    override fun setRawContent(content: ByteArray) {
        watermarkContent = content
    }

    /** Checks if [this] and [other] are the same InnamarkTag instance and have the same content */
    override fun equals(other: Any?): Boolean {
        val equalClass =
            when (this) {
                is RawInnamarkTag -> other is RawInnamarkTag
                is SizedInnamarkTag -> other is SizedInnamarkTag
                is CRC32InnamarkTag -> other is CRC32InnamarkTag
                is SizedCRC32InnamarkTag -> other is SizedCRC32InnamarkTag
                is SHA3256InnamarkTag -> other is SHA3256InnamarkTag
                is SizedSHA3256InnamarkTag -> other is SizedSHA3256InnamarkTag
                is CompressedRawInnamarkTag -> other is CompressedRawInnamarkTag
                is CompressedSizedInnamarkTag -> other is CompressedSizedInnamarkTag
                is CompressedCRC32InnamarkTag -> other is CompressedCRC32InnamarkTag
                is CompressedSizedCRC32InnamarkTag -> other is CompressedSizedCRC32InnamarkTag
                is CompressedSHA3256InnamarkTag -> other is CompressedSHA3256InnamarkTag
                is CompressedSizedSHA3256InnamarkTag -> other is CompressedSizedSHA3256InnamarkTag
            }
        if (!equalClass) return false

        return super.equals(other)
    }

    /** Represents the InnamarkTag in a human-readable form */
    override fun toString(): String {
        return "${getSource()}(${super.getContentAsText()})"
    }

    sealed interface Sized : InnamarkTagInterface {
        /**
         * Returns the range of bytes (inclusive) within the watermark bytes that represent the
         * size.
         *
         * Example:
         * If the size is stored in the first four bytes after the tag byte of the watermark, this
         * function returns `1..4`.
         */
        fun getSizeRange(): IntRange

        /**
         * Extracts the size bytes from the watermark content and parses them as UInt.
         *
         * Returns an error if the watermark content does not contain enough bytes (according to
         * the `getSizeRange()` function).
         */
        fun extractSize(): Result<UInt> {
            val content = getRawContent()
            val sizeRange = getSizeRange()
            if (content.size <= sizeRange.last) {
                return NotEnoughDataError(getSource(), sizeRange.last + 1).into<_>()
            }

            return Result.success(
                UInt.fromBytesLittleEndian(
                    content.slice(sizeRange.first until sizeRange.last + 1),
                ),
            )
        }

        /**
         * Validates that the size of the watermark content and the encoded size in the watermark
         * content match.
         *
         * Returns a warning if the size does not match.
         * Returns an error if the watermark content does not contain enough bytes (according to
         * the `getSizeRange()` function).
         */
        fun validateSize(): Status {
            val extractedSize =
                with(extractSize()) {
                    if (!isSuccess) return status
                    value!!
                }.toInt()
            val actualSize = getRawContent().size

            return if (extractedSize != actualSize) {
                MismatchedSizeWarning(getSource(), extractedSize, actualSize).into()
            } else {
                Status.success()
            }
        }
    }

    object ChecksumConstants {
        const val CHECKSUM_PLACEHOLDER: Byte = 0
    }

    sealed interface Checksum : InnamarkTagInterface {
        /**
         * Returns the range of bytes (inclusive) within the watermark bytes that represent the
         * checksum.
         *
         * Example:
         * If the checksum is stored in the first four bytes after the tag byte of the watermark,
         * this function returns `1..4`.
         */
        fun getChecksumRange(): IntRange

        /**
         * Extracts the checksum bytes from the watermark content and parses them as UInt.
         *
         * Returns an error if the watermark content does not contain enough bytes (according to
         * the `getChecksumRange()` function).
         */
        fun extractChecksum(): Result<UInt> {
            val checksumRange = getChecksumRange()
            val checksumSize = checksumRange.last - checksumRange.first + 1
            // Ensure checksum fits into an Int, otherwise impl must be changed
            check(checksumSize <= 4)

            val content = getRawContent()
            if (content.size <= checksumRange.last) {
                return NotEnoughDataError(getSource(), checksumRange.last + 1).into<_>()
            }

            val checksumBytes =
                content.slice(
                    checksumRange.first until
                        checksumRange.last + 1,
                )
            val checksum = UInt.fromBytesLittleEndian(checksumBytes)

            return Result.success(checksum)
        }

        /**
         * Returns checksum placeholder bytes that are used as checksum during the calculation of
         * the checksum.
         */
        fun getChecksumPlaceholder(): ByteArray {
            val checksumRange = getChecksumRange()
            val size = checksumRange.last - checksumRange.first + 1
            return ByteArray(size) { ChecksumConstants.CHECKSUM_PLACEHOLDER }
        }

        /**
         * Extracts the bytes that are used as input to calculate the checksum.
         *
         * This function must be overridden if more than the checksum itself is excluded from the
         * checksum input. E.g., when a placeholder for a hash is required.
         */
        fun getChecksumInput(): Result<ByteArray> {
            val rawContent = getRawContent()
            val checksumRange = getChecksumRange()
            if (rawContent.size <= checksumRange.last) {
                return NotEnoughDataError(getSource(), checksumRange.last + 1).into<_>()
            }

            val checksumInput = ArrayList<Byte>(rawContent.size)
            checksumInput.addAll(rawContent.slice(0 until checksumRange.first))
            checksumInput.addAll(getChecksumPlaceholder().asList())
            checksumInput.addAll(
                rawContent.slice(checksumRange.last + 1 until rawContent.size),
            )

            return Result.success(checksumInput.toByteArray())
        }

        /**
         * Calculates the checksum of the watermark content.
         *
         * The bytes of `getChecksumPlaceholder()` replace the actual checksum in the watermark
         * content.
         */
        fun calculateChecksum(): Result<UInt>

        /**
         * Uses the `calculateChecksum()` function to update the checksum store in the watermark
         * content.
         *
         * Returns and error if `calculateChecksum()` returns an error.
         * Returns an error if the content does not contain enough bytes (according to
         * `getChecksumRange()`).
         */
        fun updateChecksum(): Status {
            val checksum =
                with(calculateChecksum()) {
                    if (!isSuccess) return status
                    value!!
                }.toBytesLittleEndian().iterator()
            val checksumRange = getChecksumRange()

            val content = getRawContent().toMutableList()
            if (content.size <= checksumRange.last) {
                return NotEnoughDataError(SOURCE, checksumRange.last + 1).into()
            }

            for (i in checksumRange) {
                content[i] = checksum.next()
            }
            check(!checksum.hasNext())
            setRawContent(content.toByteArray())

            return Status.success()
        }

        /**
         * Validates that the checksum of the watermark content and the encoded checksum in the
         * watermark content match.
         *
         * Returns a warning if the checksum does not match.
         * Returns an error if the watermark content does not contain enough bytes (according to
         * the `getChecksumRange()` function).
         */
        fun validateChecksum(): Status {
            val extractedChecksum =
                with(extractChecksum()) {
                    if (!isSuccess) return status
                    value!!
                }
            val calculatedChecksum =
                with(calculateChecksum()) {
                    if (!isSuccess) return status
                    value!!
                }

            return if (extractedChecksum != calculatedChecksum) {
                InvalidChecksumWarning(getSource(), extractedChecksum, calculatedChecksum).into()
            } else {
                Status.success()
            }
        }
    }

    object HashConstants {
        const val HASH_PLACEHOLDER: Byte = 0
    }

    sealed interface Hash : InnamarkTagInterface {
        /**
         * Returns the range of bytes (inclusive) within the watermark bytes that represent the
         * hash.
         *
         * Example:
         * If the hash is stored in the first 32 bytes after the tag byte of the watermark, this
         * function returns `1..32`.
         */
        fun getHashRange(): IntRange

        /**
         * Extracts the hash bytes from the watermark content.
         *
         * Returns an error if the watermark content does not contain enough bytes (according to
         * the `getHashRange()` function).
         */
        fun extractHash(): Result<ByteArray> {
            val content = getRawContent()
            val hashRange = getHashRange()

            if (content.size <= hashRange.last) {
                return NotEnoughDataError(getSource(), hashRange.last + 1).into<_>()
            }

            return Result.success(
                content.copyOfRange(hashRange.first, hashRange.last + 1),
            )
        }

        /**
         * Returns hash placeholder bytes that are used as checksum during the calculation of the
         * checksum.
         */
        fun getHashPlaceholder(): ByteArray {
            val hashRange = getHashRange()
            val size = hashRange.last - hashRange.first + 1
            return ByteArray(size) { HashConstants.HASH_PLACEHOLDER }
        }

        /**
         * Extracts the bytes that are used as input to calculate the hash.
         *
         * This function must be overridden if more than the hash itself is excluded from the
         * hash input. E.g., when a placeholder for a checksum is required.
         */
        fun getHashInput(): Result<ByteArray> {
            val rawContent = getRawContent()
            val hashRange = getHashRange()
            if (rawContent.size <= hashRange.last) {
                return NotEnoughDataError(getSource(), hashRange.last + 1).into<_>()
            }

            val hashInput = ArrayList<Byte>(rawContent.size)
            hashInput.addAll(rawContent.slice(0 until hashRange.first))
            hashInput.addAll(getHashPlaceholder().asList())
            hashInput.addAll(rawContent.slice(hashRange.last + 1 until rawContent.size))

            return Result.success(hashInput.toByteArray())
        }

        /**
         * Calculates the hash of the watermark content.
         *
         * The bytes of `getHashPlaceholder()` replace the actual checksum in the watermark
         * content.
         */
        fun calculateHash(): Result<ByteArray>

        /**
         * Uses the `calculateHash()` function to update the hash store in the watermark content.
         *
         * Returns and error if `calculateHash()` returns an error.
         * Returns an error if the content does not contain enough bytes (according to
         * `getHashRange()`).
         */
        fun updateHash(): Status {
            val hash =
                with(calculateHash()) {
                    if (!isSuccess) return status
                    value!!
                }.iterator()
            val content = getRawContent().toMutableList()
            for (i in getHashRange()) {
                content[i] = hash.next()
            }
            check(!hash.hasNext())
            setRawContent(content.toByteArray())

            return Status.success()
        }

        /**
         * Validates that the hash of the watermark content and the encoded hash in the watermark
         * content match.
         *
         * Returns a warning if the hash does not match.
         * Returns an error if the watermark content does not contain enough bytes (according to
         * the `getHashRange()` function).
         */
        fun validateHash(): Status {
            val extractedHash =
                with(extractHash()) {
                    if (!isSuccess) return status
                    value!!
                }
            val calculatedHash =
                with(calculateHash()) {
                    if (!isSuccess) return status
                    value!!
                }

            return if (!extractedHash.contentEquals(calculatedHash)) {
                InvalidHashWarning(getSource(), extractedHash, calculatedHash).into()
            } else {
                Status.success()
            }
        }
    }

    sealed interface Compressed : InnamarkTagInterface {
        /** Returns if the decompression algorithm fails */
        fun validateCompression(): Status = getContent().status
    }

    object IncompleteTagError : Event.Error(SOURCE) {
        override fun getMessage(): String =
            "Cannot validate a watermark without a complete tag ($TAG_SIZE byte(s))."
    }

    class NotEnoughDataError(source: String, val minimumBytesRequired: Int) : Event.Error(source) {
        override fun getMessage(): String = "At least $minimumBytesRequired bytes are required."
    }

    class UnknownTagError(val tag: UByte) : Event.Error(SOURCE) {
        override fun getMessage(): String = "Unknown watermark tag: $tag."
    }

    class InvalidTagError(source: String, val expectedTag: UByte, val actualTag: UByte) :
        Event.Error(source) {
        override fun getMessage(): String = "Expected tag: $expectedTag, but was: $actualTag."
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
        val expectedHash: ByteArray,
        val actualHash: ByteArray,
    ) : Event.Warning(source) {
        override fun getMessage(): String {
            val expectedHex = expectedHash.toHexString()
            val actualHex = actualHash.toHexString()
            return "Expected hash: $expectedHex, but was: $actualHex."
        }
    }

    class FailedInnamarkExtractionsWarning(source: String) : Event.Warning(source) {
        override fun getMessage(): String =
            "Could not extract and convert all watermarks to InnamarkTags"
    }
}

fun Result<List<Watermark>>.toInnamarkTags(
    source: String = "InnamarkTag",
): Result<List<InnamarkTag>> {
    val (watermarks, status) =
        with(this) {
            if (value == null) {
                return status.into()
            }
            value to status
        }

    val innamarkTags =
        watermarks.mapNotNull { watermark ->
            val innamarkTag = InnamarkTag.fromWatermark(watermark)
            status.appendStatus(innamarkTag.status)
            innamarkTag.value
        }

    if (status.isError && innamarkTags.isNotEmpty()) {
        status.addEvent(
            FailedInnamarkExtractionsWarning(source),
            overrideSeverity = true,
        )
    }

    return if (status.isError) {
        status.into()
    } else {
        status.into(innamarkTags)
    }
}

@JsExport
class RawInnamarkTag(content: ByteArray) : InnamarkTag(content) {
    companion object {
        const val SOURCE = "InnamarkTag.RawInnamarkTag"
        const val TYPE_TAG: UByte = 0u // "00000000"

        /** Creates a new `RawInnamarkTag` with containing [content] */
        @JvmStatic
        fun new(content: ByteArray): RawInnamarkTag = RawInnamarkTag(createRaw(TYPE_TAG, content))

        /** Creates a new `RawInnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())

        internal fun createRaw(
            tag: UByte,
            content: ByteArray,
        ): ByteArray = byteArrayOf(tag.toByte()) + content
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent() = Result.success(watermarkContent.drop(TAG_SIZE).toByteArray())
}

@JsExport
class SizedInnamarkTag(content: ByteArray) : InnamarkTag(content), InnamarkTag.Sized {
    companion object {
        const val SOURCE = "InnamarkTag.SizedInnamarkTag"
        const val TYPE_TAG: UByte = 32u // "00100000"

        /**
         *  Number of bytes used to specify the length of the watermark. The decision to use 4 bytes
         * (i.e. 32-bit unsigned integer) was made to find a balance between maximum text size and
         * minimal additional watermark size. This decision might be reevaluated in the future.
         */
        const val SIZE_SIZE: Int = 4
        const val SIZE_START_INDEX = TAG_SIZE
        const val SIZE_END_INDEX = SIZE_START_INDEX + SIZE_SIZE - 1

        /** Creates a new `SizedInnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): SizedInnamarkTag {
            return SizedInnamarkTag(createRaw(TYPE_TAG, content))
        }

        /** Creates a new `SizedInnamark` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())

        internal fun createRaw(
            tag: UByte,
            content: ByteArray,
        ): ByteArray {
            val size = TAG_SIZE + SIZE_SIZE + content.size
            val watermark = ArrayList<Byte>(size)
            val encodedSize = size.toUInt().toBytesLittleEndian()

            watermark.add(tag.toByte())
            watermark.addAll(encodedSize)
            watermark.addAll(content.asList())

            return watermark.toByteArray()
        }
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent() =
        Result.success(
            watermarkContent.drop(TAG_SIZE + SIZE_SIZE).toByteArray(),
        )

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the size */
    override fun getSizeRange(): IntRange = SIZE_START_INDEX..SIZE_END_INDEX
}

@JsExport
class CRC32InnamarkTag(
    content: ByteArray,
) : InnamarkTag(content), InnamarkTag.Checksum {
    companion object {
        const val SOURCE = "InnamarkTag.CRC32InnamarkTag"
        const val TYPE_TAG: UByte = 16u // "00010000"
        const val CHECKSUM_SIZE = 4
        const val CHECKSUM_START_INDEX = TAG_SIZE
        const val CHECKSUM_END_INDEX = CHECKSUM_START_INDEX + CHECKSUM_SIZE - 1

        /** Creates a new `CRC32InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CRC32InnamarkTag {
            val watermark = CRC32InnamarkTag(createRaw(TYPE_TAG, content))
            watermark.updateChecksum()
            return watermark
        }

        /** Creates a new `CRC32InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())

        internal fun createRaw(
            tag: UByte,
            content: ByteArray,
        ): ByteArray {
            val watermark = ArrayList<Byte>(TAG_SIZE + CHECKSUM_SIZE + content.size)

            watermark.add(tag.toByte())
            repeat(CHECKSUM_SIZE) {
                watermark.add(ChecksumConstants.CHECKSUM_PLACEHOLDER)
            }
            watermark.addAll(content.toList())

            return watermark.toByteArray()
        }

        /** Calculates the CRC32 checksum of [input] */
        @JvmStatic
        fun calculateChecksum(input: ByteArray): UInt = CRC32.checksum(input)
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent() =
        Result.success(
            watermarkContent.drop(TAG_SIZE + CHECKSUM_SIZE).toByteArray(),
        )

    /**
     * Returns the range of bytes (inclusive) within the watermark bytes that represent the checksum
     */
    override fun getChecksumRange(): IntRange = CHECKSUM_START_INDEX..CHECKSUM_END_INDEX

    /**
     * Calculates the CRC32 checksum of the watermark content.
     *
     * The bytes of `getChecksumPlaceholder()` replace the actual checksum in the watermark content.
     */
    override fun calculateChecksum(): Result<UInt> {
        val checksumContent =
            with(getChecksumInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }

        return Result.success(Companion.calculateChecksum(checksumContent))
    }
}

@JsExport
class SizedCRC32InnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Sized, InnamarkTag.Checksum {
    companion object {
        const val SOURCE = "InnamarkTag.SizedCRC32InnamarkTag"
        const val TYPE_TAG: UByte = 48u // "00110000"
        const val SIZE_SIZE: Int = 4
        const val SIZE_START_INDEX = TAG_SIZE
        const val SIZE_END_INDEX = SIZE_START_INDEX + SIZE_SIZE - 1
        const val CHECKSUM_SIZE = 4
        const val CHECKSUM_START_INDEX = SIZE_END_INDEX + 1
        const val CHECKSUM_END_INDEX = CHECKSUM_START_INDEX + CHECKSUM_SIZE - 1

        /** Creates a new `SizedCRC32InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): SizedCRC32InnamarkTag {
            val watermark = SizedCRC32InnamarkTag(createRaw(TYPE_TAG, content))
            watermark.updateChecksum()
            return watermark
        }

        /** Creates a new `SizedCRC32InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())

        internal fun createRaw(
            tag: UByte,
            content: ByteArray,
        ): ByteArray {
            val size = TAG_SIZE + SIZE_SIZE + CHECKSUM_SIZE + content.size
            val encodedSize = size.toUInt().toBytesLittleEndian()
            val watermark = ArrayList<Byte>(size)

            watermark.add(tag.toByte())
            watermark.addAll(encodedSize.toList())
            repeat(CHECKSUM_SIZE) {
                watermark.add(ChecksumConstants.CHECKSUM_PLACEHOLDER)
            }
            watermark.addAll(content.toList())

            return watermark.toByteArray()
        }
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent() =
        Result.success(watermarkContent.drop(TAG_SIZE + SIZE_SIZE + CHECKSUM_SIZE).toByteArray())

    override fun getSizeRange(): IntRange = SIZE_START_INDEX..SIZE_END_INDEX

    /**
     * Returns the range of bytes (inclusive) within the watermark bytes that represent the checksum
     */
    override fun getChecksumRange(): IntRange = CHECKSUM_START_INDEX..CHECKSUM_END_INDEX

    /**
     * Calculates the CRC32 checksum of the watermark content.
     *
     * The bytes of `getChecksumPlaceholder()` replace the actual checksum in the watermark content.
     */
    override fun calculateChecksum(): Result<UInt> {
        val checksumContent =
            with(getChecksumInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }

        return Result.success(CRC32InnamarkTag.calculateChecksum(checksumContent))
    }
}

@JsExport
class SHA3256InnamarkTag(
    content: ByteArray,
) : InnamarkTag(content), InnamarkTag.Hash {
    companion object {
        const val SOURCE = "InnamarkTag.SHA3256InnamarkTag"
        const val TYPE_TAG: UByte = 8u // "00001000"
        const val HASH_SIZE = 32
        const val HASH_START_INDEX = TAG_SIZE
        const val HASH_END_INDEX = HASH_START_INDEX + HASH_SIZE - 1

        /** Creates a new `SHA3256InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): SHA3256InnamarkTag {
            val watermark = SHA3256InnamarkTag(createRaw(TYPE_TAG, content))
            watermark.updateHash()
            return watermark
        }

        /** Creates a new `SHA3256InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())

        internal fun createRaw(
            tag: UByte,
            content: ByteArray,
        ): ByteArray {
            val size = TAG_SIZE + HASH_SIZE + content.size

            val watermark = ArrayList<Byte>(size)
            watermark.add(tag.toByte())
            repeat(HASH_SIZE) {
                watermark.add(HashConstants.HASH_PLACEHOLDER)
            }
            watermark.addAll(content.toList())

            return watermark.toByteArray()
        }

        /** Calculates the SHA3-256 hash of [input] */
        @JvmStatic
        fun calculateHash(input: ByteArray): ByteArray {
            val hashAlgorithm = SHA3_256()
            hashAlgorithm.update(input)
            return hashAlgorithm.digest()
        }
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent() =
        Result.success(
            watermarkContent.drop(TAG_SIZE + HASH_SIZE).toByteArray(),
        )

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the hash */
    override fun getHashRange(): IntRange = HASH_START_INDEX..HASH_END_INDEX

    /**
     * Calculates the SHA3-256 hash of the watermark content.
     *
     * The bytes of `getHashPlaceholder()` replace the actual checksum in the watermark
     * content.
     */
    override fun calculateHash(): Result<ByteArray> {
        val hashInput =
            with(getHashInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }
        return Result.success(Companion.calculateHash(hashInput))
    }
}

@JsExport
class SizedSHA3256InnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Sized, InnamarkTag.Hash {
    companion object {
        const val SOURCE = "InnamarkTag.SizedSHA3256InnamarkTag"
        const val TYPE_TAG: UByte = 40u // "00101000"
        const val SIZE_SIZE = 4
        const val SIZE_START_INDEX = TAG_SIZE
        const val SIZE_END_INDEX = SIZE_START_INDEX + SIZE_SIZE - 1
        const val HASH_SIZE = 32
        const val HASH_START_INDEX = SIZE_END_INDEX + 1
        const val HASH_END_INDEX = HASH_START_INDEX + HASH_SIZE - 1

        /** Creates a new `SizedSHA3256InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): SizedSHA3256InnamarkTag {
            val watermark = SizedSHA3256InnamarkTag(createRaw(TYPE_TAG, content))
            watermark.updateHash()
            return watermark
        }

        /** Creates a new `SizedSHA3256InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())

        internal fun createRaw(
            tag: UByte,
            content: ByteArray,
        ): ByteArray {
            val size = TAG_SIZE + SIZE_SIZE + HASH_SIZE + content.size
            val encodedSize = size.toUInt().toBytesLittleEndian()

            val watermark = ArrayList<Byte>(size)
            watermark.add(tag.toByte())
            watermark.addAll(encodedSize.toList())
            repeat(HASH_SIZE) {
                watermark.add(HashConstants.HASH_PLACEHOLDER)
            }
            watermark.addAll(content.toList())

            return watermark.toByteArray()
        }
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent() =
        Result.success(
            watermarkContent.drop(TAG_SIZE + SIZE_SIZE + HASH_SIZE).toByteArray(),
        )

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the size */
    override fun getSizeRange(): IntRange = SIZE_START_INDEX..SIZE_END_INDEX

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the hash */
    override fun getHashRange(): IntRange = HASH_START_INDEX..HASH_END_INDEX

    /**
     * Calculates the SHA3-256 hash of the watermark content.
     *
     * The bytes of `getHashPlaceholder()` replace the actual checksum in the watermark
     * content.
     */
    override fun calculateHash(): Result<ByteArray> {
        val hashInput =
            with(getHashInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }

        return Result.success(SHA3256InnamarkTag.calculateHash(hashInput))
    }
}

@JsExport
class CompressedRawInnamarkTag(
    content: ByteArray,
) : InnamarkTag(content), InnamarkTag.Compressed {
    companion object {
        const val SOURCE = "InnamarkTag.CompressedRawInnamarkTag"
        const val TYPE_TAG: UByte = 64u // "01000000"

        /** Creates a new `CompressedRawInnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CompressedRawInnamarkTag {
            val compressedContent = Compression.deflate(content)
            return CompressedRawInnamarkTag(RawInnamarkTag.createRaw(TYPE_TAG, compressedContent))
        }

        /** Creates a new `CompressedRawInnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent(): Result<ByteArray> {
        val compressedContent = watermarkContent.drop(TAG_SIZE).toByteArray()
        return Compression.inflate(compressedContent)
    }
}

@JsExport
class CompressedSizedInnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Sized, InnamarkTag.Compressed {
    companion object {
        const val SOURCE = "InnamarkTag.CompressedSizedInnamarkTag"
        const val TYPE_TAG: UByte = 96u // 01100000

        /** Creates a new `CompressedSizedInnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CompressedSizedInnamarkTag {
            val compressedContent = Compression.deflate(content)
            return CompressedSizedInnamarkTag(
                SizedInnamarkTag.createRaw(
                    TYPE_TAG,
                    compressedContent,
                ),
            )
        }

        /** Creates a new `CompressedSizedInnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent(): Result<ByteArray> {
        val compressedContent =
            watermarkContent.drop(
                TAG_SIZE + SizedInnamarkTag.SIZE_SIZE,
            ).toByteArray()
        return Compression.inflate(compressedContent)
    }

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the size */
    override fun getSizeRange(): IntRange =
        SizedInnamarkTag.SIZE_START_INDEX..SizedInnamarkTag.SIZE_END_INDEX
}

@JsExport
class CompressedCRC32InnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Compressed, InnamarkTag.Checksum {
    companion object {
        const val SOURCE = "InnamarkTag.CompressedCRC32InnamarkTag"
        const val TYPE_TAG: UByte = 80u // "01010000"

        /** Creates a new `CompressedCRC32InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CompressedCRC32InnamarkTag {
            val compressedContent = Compression.deflate(content)
            val watermark =
                CompressedCRC32InnamarkTag(CRC32InnamarkTag.createRaw(TYPE_TAG, compressedContent))
            watermark.updateChecksum()
            return watermark
        }

        /** Creates a new `CompressedCRC32InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /**
     * Returns the range of bytes (inclusive) within the watermark bytes that represent the checksum
     */
    override fun getChecksumRange(): IntRange =
        CRC32InnamarkTag.CHECKSUM_START_INDEX..CRC32InnamarkTag.CHECKSUM_END_INDEX

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent(): Result<ByteArray> {
        val compressedContent =
            watermarkContent.drop(
                TAG_SIZE + CRC32InnamarkTag.CHECKSUM_SIZE,
            ).toByteArray()
        return Compression.inflate(compressedContent)
    }

    /**
     * Returns the range of bytes (inclusive) within the watermark bytes that represent the checksum
     */
    override fun calculateChecksum(): Result<UInt> {
        val checksumContent =
            with(getChecksumInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }

        return Result.success(CRC32InnamarkTag.calculateChecksum(checksumContent))
    }
}

@JsExport
class CompressedSizedCRC32InnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Sized, InnamarkTag.Checksum, InnamarkTag.Compressed {
    companion object {
        const val SOURCE = "InnamarkTag.CompressedSizedCRC32InnamarkTag"
        const val TYPE_TAG: UByte = 112u // "01110000"

        /** Creates a new `CompressedSizedCRC32InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CompressedSizedCRC32InnamarkTag {
            val compressedContent = Compression.deflate(content)
            val watermark =
                CompressedSizedCRC32InnamarkTag(
                    SizedCRC32InnamarkTag.createRaw(TYPE_TAG, compressedContent),
                )
            watermark.updateChecksum()
            return watermark
        }

        /** Creates a new `CompressedSizedCRC32InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())
    }

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the size */
    override fun getSizeRange(): IntRange =
        SizedCRC32InnamarkTag.SIZE_START_INDEX..SizedCRC32InnamarkTag.SIZE_END_INDEX

    /**
     * Returns the range of bytes (inclusive) within the watermark bytes that represent the checksum
     */
    override fun getChecksumRange(): IntRange =
        SizedCRC32InnamarkTag.CHECKSUM_START_INDEX..SizedCRC32InnamarkTag.CHECKSUM_END_INDEX

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent(): Result<ByteArray> {
        val contentOffset =
            TAG_SIZE + SizedCRC32InnamarkTag.SIZE_SIZE + SizedCRC32InnamarkTag.CHECKSUM_SIZE
        val compressedContent = watermarkContent.drop(contentOffset).toByteArray()
        return Compression.inflate(compressedContent)
    }

    /**
     * Calculates the CRC32 checksum of the watermark content.
     *
     * The bytes of `getChecksumPlaceholder()` replace the actual checksum in the watermark content.
     */
    override fun calculateChecksum(): Result<UInt> {
        val checksumContent =
            with(getChecksumInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }

        return Result.success(CRC32InnamarkTag.calculateChecksum(checksumContent))
    }
}

@JsExport
class CompressedSHA3256InnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Compressed, InnamarkTag.Hash {
    companion object {
        const val SOURCE = "InnamarkTag.CompressedSHA3256InnamarkTag"
        const val TYPE_TAG: UByte = 72u // "01001000"

        /** Creates a new `CompressedSHA3256InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CompressedSHA3256InnamarkTag {
            val compressedContent = Compression.deflate(content)
            val watermark =
                CompressedSHA3256InnamarkTag(
                    SHA3256InnamarkTag.createRaw(TYPE_TAG, compressedContent),
                )
            watermark.updateHash()
            return watermark
        }

        /** Creates a new `CompressedSHA3256InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())
    }

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Constant function that returns the name of the specific InnamarkTag */
    override fun getSource(): String = SOURCE

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the hash */
    override fun getHashRange(): IntRange =
        SHA3256InnamarkTag.HASH_START_INDEX..SHA3256InnamarkTag.HASH_END_INDEX

    /**
     * Calculates the SHA3-256 hash of the watermark content.
     *
     * The bytes of `getHashPlaceholder()` replace the actual checksum in the watermark
     * content.
     */
    override fun calculateHash(): Result<ByteArray> {
        val hashInput =
            with(getHashInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }
        return Result.success(SHA3256InnamarkTag.calculateHash(hashInput))
    }

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent(): Result<ByteArray> {
        val contentOffset = TAG_SIZE + SHA3256InnamarkTag.HASH_SIZE
        val compressedContent = watermarkContent.drop(contentOffset).toByteArray()
        return Compression.inflate(compressedContent)
    }
}

@JsExport
class CompressedSizedSHA3256InnamarkTag(content: ByteArray) :
    InnamarkTag(content), InnamarkTag.Compressed, InnamarkTag.Sized, InnamarkTag.Hash {
    companion object {
        const val SOURCE = "InnamarkTag.CompressedSizedSHA3256InnamarkTag"
        const val TYPE_TAG: UByte = 104u // "01101000"

        /** Creates a new `CompressedSizedSHA3256InnamarkTag` containing [content] */
        @JvmStatic
        fun new(content: ByteArray): CompressedSizedSHA3256InnamarkTag {
            val compressedContent = Compression.deflate(content)
            val watermark =
                CompressedSizedSHA3256InnamarkTag(
                    SizedSHA3256InnamarkTag.createRaw(TYPE_TAG, compressedContent),
                )
            watermark.updateHash()
            return watermark
        }

        /** Creates a new `CompressedSizedSHA3256InnamarkTag` with [text] as content */
        @JvmStatic
        fun fromString(text: String) = new(text.encodeToByteArray())
    }

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getTag(): UByte = TYPE_TAG

    /** Constant function that returns the tag used to encode this InnamarkTag class */
    override fun getSource(): String = SOURCE

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the size */
    override fun getSizeRange(): IntRange =
        SizedSHA3256InnamarkTag.SIZE_START_INDEX..SizedSHA3256InnamarkTag.SIZE_END_INDEX

    /** Returns the range of bytes (inclusive) within the watermark bytes that represent the hash */
    override fun getHashRange(): IntRange =
        SizedSHA3256InnamarkTag.HASH_START_INDEX..SizedSHA3256InnamarkTag.HASH_END_INDEX

    /**
     * Calculates the SHA3-256 hash of the watermark content.
     *
     * The bytes of `getHashPlaceholder()` replace the actual checksum in the watermark
     * content.
     */
    override fun calculateHash(): Result<ByteArray> {
        val hashInput =
            with(getHashInput()) {
                if (!isSuccess) return status.into<_>()
                value!!
            }
        return Result.success(SHA3256InnamarkTag.calculateHash(hashInput))
    }

    /** Returns the decoded information stored in the InnamarkTag */
    override fun getContent(): Result<ByteArray> {
        val contentOffset =
            TAG_SIZE + SizedSHA3256InnamarkTag.SIZE_SIZE + SizedSHA3256InnamarkTag.HASH_SIZE
        val compressedContent = watermarkContent.drop(contentOffset).toByteArray()
        return Compression.inflate(compressedContent)
    }
}
