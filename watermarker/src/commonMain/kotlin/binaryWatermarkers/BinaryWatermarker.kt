/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.binaryWatermarkers

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

/**
 * Interface for implementations facilitating watermarking of arbitrary non-text covers
 */
interface BinaryWatermarker<CoverType> {
    /** Adds a watermark created from [watermark] String to [cover] */
    fun addWatermark(
        cover: CoverType,
        watermark: String,
    ): Result<CoverType>

    /** Adds a watermark created from [watermark] ByteArray to [cover] */
    fun addWatermark(
        cover: CoverType,
        watermark: ByteArray,
    ): Result<CoverType>

    /** Adds watermark object [watermark] to [cover] */
    fun addWatermark(
        cover: CoverType,
        watermark: Watermark,
    ): Result<CoverType>

    /** Returns a [Boolean] indicating whether [cover] contains watermarks */
    fun containsWatermark(cover: CoverType): Boolean

    /**
     * Returns a [Result] containing a list of [Watermark]s in [cover]
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    fun getWatermarks(
        cover: CoverType,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>>

    /** Removes all watermarks from [cover] and returns a [Result] containing the cleaned cover */
    fun removeWatermark(cover: CoverType): Result<CoverType>
}
