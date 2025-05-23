/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.textWatermarkers

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark.MultipleMostFrequentWarning
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark.StringDecodeWarning

/**
 * Interface for implementations facilitating watermarking of [String] covers
 */
interface TextWatermarker {
    /** Adds a watermark created from [watermark] String to [cover] */
    fun addWatermark(
        cover: String,
        watermark: String,
    ): Result<String>

    /** Adds a watermark created from [watermark] ByteArray to [cover] */
    fun addWatermark(
        cover: String,
        watermark: ByteArray,
    ): Result<String>

    /** Adds watermark object [watermark] to [cover] */
    fun addWatermark(
        cover: String,
        watermark: Watermark,
    ): Result<String>

    /** Returns a [Boolean] indicating whether [cover] contains watermarks */
    fun containsWatermark(cover: String): Boolean

    /**
     * Returns a [Result] containing the most frequent Watermark in [cover] as a String
     *
     * Result contains an empty String if no Watermarks were found.
     * Result contains a [MultipleMostFrequentWarning] in cases where an unambiguous Watermark could not be extracted.
     * Result contains a [StringDecodeWarning] in cases where a byte cannot be read as UTF-8.
     */
    fun getWatermarkAsString(cover: String): Result<String>

    /**
     * Returns a [Result] containing the most frequent Watermark in [cover] as a ByteArray
     *
     * Result contains an empty ByteArray if no Watermarks were found.
     * Result contains a [MultipleMostFrequentWarning] in cases where an unambiguous Watermark could not be extracted.
     */
    fun getWatermarkAsByteArray(cover: String): Result<ByteArray>

    /**
     * Returns a [Result] containing a list of [Watermark]s in [cover]
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    fun getWatermarks(
        cover: String,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>>

    /** Removes all watermarks from [cover] and returns a [Result] containing the cleaned cover */
    fun removeWatermarks(cover: String): Result<String>
}
