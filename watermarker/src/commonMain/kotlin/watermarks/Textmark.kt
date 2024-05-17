/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.watermarks

import de.fraunhofer.isst.trend.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status

class Textmark private constructor(
    var text: String,
    private var compressed: Boolean = false,
    private var sized: Boolean = false,
    private var textWatermarker: TextWatermarker? = null,
) : TrendmarkBuilder {
    companion object {
        fun new(text: String): Textmark = Textmark(text)

        fun compressed(text: String): Textmark = Textmark(text, compressed = true)

        fun sized(text: String): Textmark = Textmark(text, sized = true)

        fun compressedAndSized(text: String): Textmark =
            Textmark(text, compressed = true, sized = true)

        fun fromTrendmark(
            trendmark: Trendmark,
            errorOnInvalidUTF8: Boolean = false,
        ): Result<Textmark> {
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

            val textmark =
                when (trendmark) {
                    is RawWatermark -> Textmark(text)
                    is SizedWatermark -> Textmark(text, sized = true)
                    is CompressedRawWatermark -> Textmark(text, compressed = true)
                    is CompressedSizedWatermark -> Textmark(text, compressed = true, sized = true)

                    is CRC32Watermark,
                    is SizedCRC32Watermark,
                    is CompressedCRC32Watermark,
                    is CompressedSizedCRC32Watermark,
                    is SHA3256Watermark,
                    is SizedSHA3256Watermark,
                    is CompressedSHA3256Watermark,
                    is CompressedSizedSHA3256Watermark,
                    -> {
                        status.addEvent(UnsupportedTrendmarkError(trendmark.getSource()))
                        return status.into<_>()
                    }
                }

            return status.into(textmark)
        }
    }

    fun compressed(active: Boolean = true) {
        compressed = active
    }

    fun isCompressed(): Boolean = compressed

    fun sized(active: Boolean = true) {
        sized = active
    }

    fun isSized(): Boolean = sized

    override fun finish(): Trendmark {
        val content = text.encodeToByteArray().asList()

        return if (sized && compressed) {
            CompressedSizedWatermark.new(content)
        } else if (sized) {
            SizedWatermark.new(content)
        } else if (compressed) {
            CompressedRawWatermark.new(content)
        } else {
            RawWatermark.new(content)
        }
    }

    override fun toString(): String {
        return if (compressed && sized) {
            "CompressedAndSizedTextmark: '$text'"
        } else if (compressed) {
            "CompressedTextmark: '$text'"
        } else if (sized) {
            "SizedTextmark: '$text'"
        } else {
            "Textmark: '$text'"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Textmark) return false
        return text == other.text && compressed == other.compressed && sized == other.sized &&
            textWatermarker == other.textWatermarker
    }

    class DecodeToStringError(val reason: String) : Event.Error("Textmark.fromTrendmark") {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Failed to decode bytes to string: $reason."
    }

    class UnsupportedTrendmarkError(val trendmark: String) : Event.Error("Textmark.fromTrendmark") {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "The Trendmark type $trendmark is not supported by Textmark."
    }
}
