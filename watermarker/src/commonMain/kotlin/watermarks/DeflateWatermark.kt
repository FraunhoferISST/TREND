/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.watermarks

import de.fraunhofer.isst.trend.watermarker.helper.Compression
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result

open class DeflateWatermark(content: List<Byte>) : Watermark(content) {
    /** Returns the inflated bytes from the Watermark */
    fun getBytes(): Result<List<Byte>> {
        return Compression.inflate(content)
    }

    companion object {
        /** Converts a watermark to a DeflateWatermark */
        fun fromWatermark(watermark: Watermark): DeflateWatermark =
            DeflateWatermark(watermark.content)

        /** Creates a DeflateWatermark from a [text] using deflate compression */
        fun fromText(text: String): DeflateWatermark {
            val bytes = text.encodeToByteArray().asList()
            return fromBytes(bytes)
        }

        /** Creates a DeflateWatermark from [bytes] using deflate compression */
        fun fromBytes(bytes: List<Byte>): DeflateWatermark {
            val deflated = Compression.deflate(bytes)
            return DeflateWatermark(deflated)
        }
    }
}

class DecodeToStringError(val reason: String) : Event.Error("DeflateWatermark.decodeToString") {
    /** Returns a String explaining the event */
    override fun getMessage(): String = "Failed to decode bytes to string: $reason."
}
