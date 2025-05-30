/*
 * Copyright (c) 2024-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.watermarks

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Event
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark.FailedTextWatermarkExtractionsWarning
import kotlin.js.JsExport
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * The TextWatermark class provides convenient functions to create and read InnamarkTags with UTF-8
 * text as content.
 *
 * TextWatermark can be instantiated using the companion functions. The functions `new`, `raw`,
 * `compressed`, `sized`, `CRC32`, `SHA3256`, `compressedSized`, `compressedCRC32`,
 * `compressedSHA3256`, `sizedCRC32`, `sizedSHA3256`, `compressedSizedCRC32` and
 * `compressedSizedSHA3256` allows creation of a new TextWatermark from a String and specifying
 * the format of the produced InnamarkTag.
 *
 * The function `fromInnamarkTag` allows parsing a supported InnamarkTag into a TextWatermark,
 * giving direct access to the contained text without having to consider the format of the
 * InnamarkTag.
 *
 * Sized TextWatermarks will create InnamarkTags that encode the size of the InnamarkTag into the
 * watermark.
 *
 * CRC32 TextWatermarks will create InnamarkTags that encode a CRC32 checksum into the watermark.
 *
 * SHA3256 TextWatermarks will create InnamarkTags that encode a SHA3256 hash into the watermark.
 *
 * Compressed TextWatermarks will compress the Text using a compression algorithm. This can be
 * useful when the watermark text is very long, but it might reduce the watermark robustness.
 *
 */
@JsExport
class TextWatermark private constructor(
    var text: String,
    private var compressed: Boolean = false,
    private var sized: Boolean = false,
    private var CRC32: Boolean = false,
    private var SHA3256: Boolean = false,
) : InnamarkTagBuilder {
    companion object {
        /**
         * Creates a TextWatermark in default configuration.
         *
         * The default configuration is: no compression, no size information, no checksum, no hash.
         */
        @JvmName("create")
        @JvmStatic
        fun new(text: String): TextWatermark = TextWatermark(text)

        /** Creates a TextWatermark from [text] without additional information */
        @JvmStatic
        fun raw(text: String): TextWatermark = TextWatermark(text)

        /** Creates a TextWatermark from [text] with compression */
        @JvmStatic
        fun compressed(text: String): TextWatermark = TextWatermark(text, compressed = true)

        /** Creates a TextWatermark from [text] with compression only if compression decreases the size */
        @JvmStatic
        fun small(text: String): TextWatermark {
            val raw = TextWatermark.raw(text)
            val rawSize = raw.finish().watermarkContent.size
            val compressed = TextWatermark.compressed(text)
            val compressedSize = compressed.finish().watermarkContent.size

            return if (rawSize < compressedSize) {
                raw
            } else {
                compressed
            }
        }

        /** Creates a TextWatermark from [text] with size information */
        @JvmStatic
        fun sized(text: String): TextWatermark = TextWatermark(text, sized = true)

        /** Creates a TextWatermark from [text] with CRC32 checksum */
        @Suppress("ktlint:standard:function-naming")
        @JvmStatic
        fun CRC32(text: String): TextWatermark = TextWatermark(text, CRC32 = true)

        /** Creates a TextWatermark from [text] with SHA3256 hash */
        @Suppress("ktlint:standard:function-naming")
        @JvmStatic
        fun SHA3256(text: String): TextWatermark = TextWatermark(text, SHA3256 = true)

        /** Creates a TextWatermark from [text] with size information and compression */
        @JvmStatic
        fun compressedSized(text: String): TextWatermark =
            TextWatermark(text, compressed = true, sized = true)

        /** Creates a TextWatermark from [text] with compression and CRC32 checksum */
        @JvmStatic
        fun compressedCRC32(text: String): TextWatermark =
            TextWatermark(text, compressed = true, CRC32 = true)

        /** Creates a TextWatermark from [text] with compression and SHA3256 hash */
        @JvmStatic
        fun compressedSHA3256(text: String): TextWatermark =
            TextWatermark(text, compressed = true, SHA3256 = true)

        /** Creates a TextWatermark from [text] with size information and CRC32 checksum */
        @JvmStatic
        fun sizedCRC32(text: String): TextWatermark =
            TextWatermark(text, sized = true, CRC32 = true)

        /** Creates a TextWatermark from [text] with size information and SHA3256 hash */
        @JvmStatic
        fun sizedSHA3256(text: String): TextWatermark =
            TextWatermark(text, sized = true, SHA3256 = true)

        /** Creates a TextWatermark from [text] with compression, size information and CRC32 checksum */
        @JvmStatic
        fun compressedSizedCRC32(text: String): TextWatermark =
            TextWatermark(text, compressed = true, sized = true, CRC32 = true)

        /** Creates a TextWatermark from [text] with compression, size information and SHA3256 hash */
        @JvmStatic
        fun compressedSizedSHA3256(text: String): TextWatermark =
            TextWatermark(text, compressed = true, sized = true, SHA3256 = true)

        /**
         * Creates a TextWatermark from [innamarkTag].
         * Sets sized, compressed, CRC32 and SHA3256 depending on the variant of [innamarkTag].
         *
         * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error.
         *                           is false: invalid bytes sequences are replace with the char �.
         * Returns an error when [innamarkTag] contains an unsupported variant.
         */
        @JvmStatic
        fun fromInnamarkTag(
            innamarkTag: InnamarkTag,
            errorOnInvalidUTF8: Boolean = false,
        ): Result<TextWatermark> {
            val (content, status) =
                with(innamarkTag.getContent()) {
                    if (!hasValue) return status.into<_>()
                    value!! to status
                }
            val text =
                try {
                    content
                        .decodeToString(throwOnInvalidSequence = errorOnInvalidUTF8)
                } catch (e: Exception) {
                    status.addEvent(DecodeToStringError(e.message ?: e.stackTraceToString()))
                    return status.into()
                }

            val textWatermark =
                when (innamarkTag) {
                    is RawInnamarkTag -> TextWatermark(text)
                    is SizedInnamarkTag -> TextWatermark(text, sized = true)
                    is CompressedRawInnamarkTag -> TextWatermark(text, compressed = true)
                    is CompressedSizedInnamarkTag ->
                        TextWatermark(text, compressed = true, sized = true)

                    is CRC32InnamarkTag -> TextWatermark(text, CRC32 = true)
                    is SizedCRC32InnamarkTag -> TextWatermark(text, sized = true, CRC32 = true)
                    is CompressedCRC32InnamarkTag ->
                        TextWatermark(text, compressed = true, CRC32 = true)
                    is CompressedSizedCRC32InnamarkTag ->
                        TextWatermark(text, compressed = true, sized = true, CRC32 = true)
                    is SHA3256InnamarkTag -> TextWatermark(text, SHA3256 = true)
                    is SizedSHA3256InnamarkTag -> TextWatermark(text, sized = true, SHA3256 = true)
                    is CompressedSHA3256InnamarkTag ->
                        TextWatermark(text, compressed = true, SHA3256 = true)
                    is CompressedSizedSHA3256InnamarkTag ->
                        TextWatermark(text, compressed = true, sized = true, SHA3256 = true)
                    else -> {
                        status.addEvent(UnsupportedInnamarkError(innamarkTag.getSource()))
                        return status.into<_>()
                    }
                }

            return status.into(textWatermark)
        }
    }

    /** sets compressed to [active] */
    fun compressed(active: Boolean = true) {
        compressed = active
    }

    /** true if compression is activated */
    fun isCompressed(): Boolean = compressed

    /** sets sized to [active] */
    fun sized(active: Boolean = true) {
        sized = active
    }

    /** true if size information is added to the InnamarkTag */
    fun isSized(): Boolean = sized

    /** sets CRC32 to [active] */
    @Suppress("ktlint:standard:function-naming")
    fun CRC32(active: Boolean = true) {
        CRC32 = active
    }

    /** true if checksum information is added to the InnamarkTag */
    fun isCRC32(): Boolean = CRC32

    /** sets SHA3256 to [active] */
    @Suppress("ktlint:standard:function-naming")
    fun SHA3256(active: Boolean = true) {
        SHA3256 = active
    }

    /** true if hash information is added to the InnamarkTag */
    fun isSHA3256(): Boolean = SHA3256

    /**
     * Generates a InnamarkTag with [text] as content.
     *
     * The used variant of InnamarkTag depends on:
     *  - [compressed]
     *  - [sized]
     *  - [CRC32]
     *  - [SHA3256].
     */
    override fun finish(): InnamarkTag {
        val content = text.encodeToByteArray()

        return if (compressed && sized && SHA3256) {
            CompressedSizedSHA3256InnamarkTag.new(content)
        } else if (compressed && SHA3256) {
            CompressedSHA3256InnamarkTag.new(content)
        } else if (sized && SHA3256) {
            SizedSHA3256InnamarkTag.new(content)
        } else if (SHA3256) {
            SHA3256InnamarkTag.new(content)
        } else if (compressed && sized && CRC32) {
            CompressedSizedCRC32InnamarkTag.new(content)
        } else if (compressed && CRC32) {
            CompressedCRC32InnamarkTag.new(content)
        } else if (sized && CRC32) {
            SizedCRC32InnamarkTag.new(content)
        } else if (CRC32) {
            CRC32InnamarkTag.new(content)
        } else if (compressed && sized) {
            CompressedSizedInnamarkTag.new(content)
        } else if (compressed) {
            CompressedRawInnamarkTag.new(content)
        } else if (sized) {
            SizedInnamarkTag.new(content)
        } else {
            RawInnamarkTag.new(content)
        }
    }

    /** Contains the used InnamarkTag variant followed by [text] */
    override fun toString(): String {
        return if (compressed && sized && SHA3256) {
            "CompressedSizedSHA3256TextWatermark: '$text'"
        } else if (compressed && SHA3256) {
            "CompressedSHA3256TextWatermark: '$text'"
        } else if (sized && SHA3256) {
            "SizedSHA3256TextWatermark: '$text'"
        } else if (SHA3256) {
            "SHA3256TextWatermark: '$text'"
        } else if (compressed && sized && CRC32) {
            "CompressedSizedCRC32TextWatermark: '$text'"
        } else if (compressed && CRC32) {
            "CompressedCRC32TextWatermark: '$text'"
        } else if (sized && CRC32) {
            "SizedCRC32TextWatermark: '$text'"
        } else if (CRC32) {
            "CRC32TextWatermark: '$text'"
        } else if (compressed && sized) {
            "CompressedSizedTextWatermark: '$text'"
        } else if (compressed) {
            "CompressedTextWatermark: '$text'"
        } else if (sized) {
            "SizedTextWatermark: '$text'"
        } else {
            "TextWatermark: '$text'"
        }
    }

    /** Returns true if [this].finish() and [other].finish() produce an equal InnamarkTag */
    override fun equals(other: Any?): Boolean {
        if (other !is TextWatermark) return false
        return text == other.text && compressed == other.compressed && sized == other.sized &&
            CRC32 == other.CRC32 && SHA3256 == other.SHA3256
    }

    class DecodeToStringError(val reason: String) : Event.Error("TextWatermark.fromInnamarkTag") {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Failed to decode bytes to string: $reason."
    }

    class UnsupportedInnamarkError(val innamarkTag: String) :
        Event.Error("TextWatermark.fromInnamarkTag") {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "The InnamarkTag type $innamarkTag is not supported by TextWatermark."
    }

    class FailedTextWatermarkExtractionsWarning(source: String) : Event.Warning(source) {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "Could not extract and convert all watermarks to TextWatermarks"
    }
}

@JvmName("intoTextWatermarks")
fun Result<List<InnamarkTag>>.toTextWatermarks(
    errorOnInvalidUTF8: Boolean = false,
    source: String = "toTextWatermarks",
): Result<List<TextWatermark>> {
    val (Innamarks, status) =
        with(this) {
            if (value == null) {
                return status.into()
            }
            value to status
        }

    val textWatermarks =
        Innamarks.mapNotNull { innamarkTag ->
            val textWatermark = TextWatermark.fromInnamarkTag(innamarkTag, errorOnInvalidUTF8)
            status.appendStatus(textWatermark.status)
            textWatermark.value
        }

    if (status.isError && textWatermarks.isNotEmpty()) {
        status.addEvent(
            FailedTextWatermarkExtractionsWarning(source),
            overrideSeverity = true,
        )
    }

    return if (status.isError) {
        status.into()
    } else {
        status.into(textWatermarks)
    }
}

fun Result<List<Watermark>>.toTextWatermarks(
    errorOnInvalidUTF8: Boolean = false,
    source: String = "toTextWatermarks",
): Result<List<TextWatermark>> {
    return this.toInnamarkTags(source).toTextWatermarks(errorOnInvalidUTF8, source)
}
