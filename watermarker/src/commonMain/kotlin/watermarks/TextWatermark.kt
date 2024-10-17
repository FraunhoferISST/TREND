/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.watermarks

import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark.FailedTextWatermarkExtractionsWarning
import kotlin.js.JsExport
import kotlin.jvm.JvmName

/**
 * The TextWatermark class provides convenient functions to create and read Trendmarks with UTF-8
 * text as content.
 *
 * TextWatermark can be instantiated using the companion functions. The functions `new`, `raw`,
 * `compressed`, `sized`, `checked`, `hashed`, `compressedAndSized`, `compressedAndChecked`,
 * `compressedAndHashed`, `sizedAndChecked`, `sizedAndHashed`, `compressedSizedChecked` and
 * `compressedSizedHashed` allows creation of a new TextWatermark from a String and specifying
 * the format of the produced Trendmark.
 *
 * The function `fromTrendmark` allows parsing a supported Trendmark into a TextWatermark, giving
 * direct access to the contained text without having to consider the format of the Trendmark.
 *
 * Sized TextWatermarks will create Trendmarks that encode the size of the Trendmark into the
 * watermark.
 *
 * Checked TextWatermarks will create Trendmarks that encode a CRC32 checksum into the watermark.
 *
 * Hashed TextWatermarks will create Trendmarks that encode a SHA3256 hash into the watermark.
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
    private var checked: Boolean = false,
    private var hashed: Boolean = false,
) : TrendmarkBuilder {
    companion object {
        /**
         * Creates a TextWatermark in default configuration.
         *
         * The default configuration is: no compression, no size information, no checksum, no hash.
         */
        @JvmName("create")
        fun new(text: String): TextWatermark = TextWatermark(text)

        /** Creates a TextWatermark from [text] without additional information */
        fun raw(text: String): TextWatermark = TextWatermark(text)

        /** Creates a TextWatermark from [text] with compression */
        fun compressed(text: String): TextWatermark = TextWatermark(text, compressed = true)

        /** Creates a TextWatermark from [text] with compression only if compression decreases the size */
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
        fun sized(text: String): TextWatermark = TextWatermark(text, sized = true)

        /** Creates a TextWatermark from [text] with CRC32 checksum */
        fun checked(text: String): TextWatermark = TextWatermark(text, checked = true)

        /** Creates a TextWatermark from [text] with SHA3256 hash */
        fun hashed(text: String): TextWatermark = TextWatermark(text, hashed = true)

        /** Creates a TextWatermark from [text] with size information and compression */
        fun compressedAndSized(text: String): TextWatermark =
            TextWatermark(text, compressed = true, sized = true)

        /** Creates a TextWatermark from [text] with compression and checksum */
        fun compressedAndChecked(text: String): TextWatermark =
            TextWatermark(text, compressed = true, checked = true)

        /** Creates a TextWatermark from [text] with compression and hash */
        fun compressedAndHashed(text: String): TextWatermark =
            TextWatermark(text, compressed = true, hashed = true)

        /** Creates a TextWatermark from [text] with size information and checksum */
        fun sizedAndChecked(text: String): TextWatermark =
            TextWatermark(text, sized = true, checked = true)

        /** Creates a TextWatermark from [text] with size information and hash */
        fun sizedAndHashed(text: String): TextWatermark =
            TextWatermark(text, sized = true, hashed = true)

        /** Creates a TextWatermark from [text] with compression, size information and checksum */
        fun compressedSizedChecked(text: String): TextWatermark =
            TextWatermark(text, compressed = true, sized = true, checked = true)

        /** Creates a TextWatermark from [text] with compression, size information and hash */
        fun compressedSizedHashed(text: String): TextWatermark =
            TextWatermark(text, compressed = true, sized = true, hashed = true)

        /**
         * Creates a TextWatermark from [trendmark].
         * Sets sized, compressed, checked and hashed depending on the variant of [trendmark].
         *
         * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error.
         *                           is false: invalid bytes sequences are replace with the char �.
         * Returns an error when [trendmark] contains an unsupported variant.
         */
        fun fromTrendmark(
            trendmark: Trendmark,
            errorOnInvalidUTF8: Boolean = false,
        ): Result<TextWatermark> {
            val (content, status) =
                with(trendmark.getContent()) {
                    if (!hasValue) return status.into<_>()
                    value!! to status
                }
            val text =
                try {
                    content
                        .toByteArray()
                        .decodeToString(throwOnInvalidSequence = errorOnInvalidUTF8)
                } catch (e: Exception) {
                    status.addEvent(DecodeToStringError(e.message ?: e.stackTraceToString()))
                    return status.into()
                }

            val textWatermark =
                when (trendmark) {
                    is RawTrendmark -> TextWatermark(text)
                    is SizedTrendmark -> TextWatermark(text, sized = true)
                    is CompressedRawTrendmark -> TextWatermark(text, compressed = true)
                    is CompressedSizedTrendmark ->
                        TextWatermark(text, compressed = true, sized = true)

                    is CRC32Trendmark -> TextWatermark(text, checked = true)
                    is SizedCRC32Trendmark -> TextWatermark(text, sized = true, checked = true)
                    is CompressedCRC32Trendmark ->
                        TextWatermark(text, compressed = true, checked = true)
                    is CompressedSizedCRC32Trendmark ->
                        TextWatermark(text, compressed = true, sized = true, checked = true)
                    is SHA3256Trendmark -> TextWatermark(text, hashed = true)
                    is SizedSHA3256Trendmark -> TextWatermark(text, sized = true, hashed = true)
                    is CompressedSHA3256Trendmark ->
                        TextWatermark(text, compressed = true, hashed = true)
                    is CompressedSizedSHA3256Trendmark ->
                        TextWatermark(text, compressed = true, sized = true, hashed = true)
                    else -> {
                        status.addEvent(UnsupportedTrendmarkError(trendmark.getSource()))
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

    /** true if size information is added to the Trendmark */
    fun isSized(): Boolean = sized

    /** sets checked to [active] */
    fun checked(active: Boolean = true) {
        checked = active
    }

    /** true if checksum information is added to the Trendmark */
    fun isChecked(): Boolean = checked

    /** sets hashed to [active] */
    fun hashed(active: Boolean = true) {
        hashed = active
    }

    /** true if hash information is added to the Trendmark */
    fun isHashed(): Boolean = hashed

    /**
     * Generates a Trendmark with [text] as content.
     *
     * The used variant of Trendmark depends on:
     *  - [compressed]
     *  - [sized]
     *  - [checked]
     *  - [hashed].
     */
    override fun finish(): Trendmark {
        val content = text.encodeToByteArray().asList()

        return if (compressed && sized && hashed) {
            CompressedSizedSHA3256Trendmark.new(content)
        } else if (compressed && hashed) {
            CompressedSHA3256Trendmark.new(content)
        } else if (sized && hashed) {
            SizedSHA3256Trendmark.new(content)
        } else if (hashed) {
            SHA3256Trendmark.new(content)
        } else if (compressed && sized && checked) {
            CompressedSizedCRC32Trendmark.new(content)
        } else if (compressed && checked) {
            CompressedCRC32Trendmark.new(content)
        } else if (sized && checked) {
            SizedCRC32Trendmark.new(content)
        } else if (checked) {
            CRC32Trendmark.new(content)
        } else if (compressed && sized) {
            CompressedSizedTrendmark.new(content)
        } else if (compressed) {
            CompressedRawTrendmark.new(content)
        } else if (sized) {
            SizedTrendmark.new(content)
        } else {
            RawTrendmark.new(content)
        }
    }

    /** Contains the used Trendmark variant followed by [text] */
    override fun toString(): String {
        return if (compressed && sized && hashed) {
            "CompressedSizedHashedTextWatermark: '$text'"
        } else if (compressed && hashed) {
            "CompressedAndHashedTextWatermark: '$text'"
        } else if (sized && hashed) {
            "SizedAndHashedTextWatermark: '$text'"
        } else if (hashed) {
            "HashedTextWatermark: '$text'"
        } else if (compressed && sized && checked) {
            "CompressedSizedCheckedTextWatermark: '$text'"
        } else if (compressed && checked) {
            "CompressedAndCheckedTextWatermark: '$text'"
        } else if (sized && checked) {
            "SizedAndCheckedTextWatermark: '$text'"
        } else if (checked) {
            "CheckedTextWatermark: '$text'"
        } else if (compressed && sized) {
            "CompressedAndSizedTextWatermark: '$text'"
        } else if (compressed) {
            "CompressedTextWatermark: '$text'"
        } else if (sized) {
            "SizedTextWatermark: '$text'"
        } else {
            "TextWatermark: '$text'"
        }
    }

    /** Returns true if [this].finish() and [other].finish() produce an equal Trendmark */
    override fun equals(other: Any?): Boolean {
        if (other !is TextWatermark) return false
        return text == other.text && compressed == other.compressed && sized == other.sized &&
            checked == other.checked && hashed == other.hashed
    }

    class DecodeToStringError(val reason: String) : Event.Error("TextWatermark.fromTrendmark") {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Failed to decode bytes to string: $reason."
    }

    class UnsupportedTrendmarkError(val trendmark: String) :
        Event.Error("TextWatermark.fromTrendmark") {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "The Trendmark type $trendmark is not supported by TextWatermark."
    }

    class FailedTextWatermarkExtractionsWarning(source: String) : Event.Warning(source) {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "Could not extract and convert all watermarks to TextWatermarks"
    }
}

@JvmName("intoTextWatermarks")
fun Result<List<Trendmark>>.toTextWatermarks(
    errorOnInvalidUTF8: Boolean = false,
    source: String = "toTextWatermarks",
): Result<List<TextWatermark>> {
    val (trendmarks, status) =
        with(this) {
            if (value == null) {
                return status.into()
            }
            value to status
        }

    val textWatermarks =
        trendmarks.mapNotNull { trendmark ->
            val textWatermark = TextWatermark.fromTrendmark(trendmark, errorOnInvalidUTF8)
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
    return this.toTrendmarks(source).toTextWatermarks(errorOnInvalidUTF8, source)
}
