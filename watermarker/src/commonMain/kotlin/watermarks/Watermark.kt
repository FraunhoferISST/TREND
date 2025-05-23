/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.watermarks

import de.fraunhofer.isst.innamark.watermarker.helper.toHexString
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Event
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

@JsExport
open class Watermark(var watermarkContent: ByteArray) {
    companion object {
        const val SOURCE = "Watermark"

        /** Creates a Watermark from [text] */
        @JvmStatic
        fun fromString(text: String): Watermark {
            val bytes = text.encodeToByteArray()
            return Watermark(bytes)
        }

        /**
         *  Returns only the most frequent [Watermark] from [watermarks].
         *  Returns a Warning if more than one most frequent Watermark is found.
         *  Returns an Error if an Exception is thrown.
         *  */
        @JvmStatic
        fun mostFrequent(watermarks: List<Watermark>): Result<List<Watermark>> {
            var result: Result<List<Watermark>> = Result.success(watermarks)
            if (watermarks.isEmpty()) {
                return result
            }
            val frequencyMap: Map<Watermark, Int>
            val maxFrequency: Int
            val mostFrequent = mutableListOf<Watermark>()
            val filteredList = mutableListOf<Watermark>()

            try {
                frequencyMap = watermarks.groupingBy { it }.eachCount()
                maxFrequency = frequencyMap.maxOf { it.value }
                mostFrequent.addAll(frequencyMap.filter { it.value == maxFrequency }.keys)
                for (watermark in mostFrequent) {
                    for (i in 1..maxFrequency) {
                        filteredList.add(watermark)
                    }
                }
                result =
                    if (mostFrequent.size > 1) {
                        MultipleMostFrequentWarning(mostFrequent.size).into(filteredList)
                    } else {
                        Result.success(filteredList)
                    }
            } catch (e: Exception) {
                result =
                    FrequencyAnalysisError(
                        e::class.simpleName ?: "Unknown Exception",
                    ).into(emptyList())
            } finally {
                return result
            }
        }
    }

    /** Represents the bytes of the Watermark in hex */
    fun getContentAsText(): String = watermarkContent.toHexString()

    /** Represents the Watermark in a human-readable form */
    override fun toString(): String {
        return "Watermark(${this.getContentAsText()})"
    }

    /** Returns true if other is a watermark and contains the same bytes */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Watermark -> {
                this.watermarkContent.contentEquals(other.watermarkContent)
            }

            else -> false
        }
    }

    /** Exposes content.hashCode() */
    override fun hashCode(): Int = watermarkContent.contentHashCode()

    class StringDecodeWarning(
        private val SOURCE: String,
    ) : Event.Warning("$SOURCE.getWatermarkAsString") {
        /** Returns a String explaining the event */
        override fun getMessage() =
            "Malformed UTF-8 bytes were found in the Watermark and were " +
                "replaced by \uFFFD"
    }

    class MultipleMostFrequentWarning(
        private val WatermarkCount: Int,
    ) : Event.Warning("$SOURCE.mostFrequent") {
        /** Returns a String explaining the event */
        override fun getMessage() = "$WatermarkCount most frequent watermarks found!"
    }

    class FrequencyAnalysisError(
        private val type: String,
    ) : Event.Error("$SOURCE.mostFrequent") {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Frequency analysis failed with exception: $type!"
    }
}
