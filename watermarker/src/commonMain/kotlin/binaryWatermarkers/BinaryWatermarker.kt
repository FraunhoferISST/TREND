package de.fraunhofer.isst.innamark.watermarker.binaryWatermarkers

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

interface BinaryWatermarker<CoverType> {
    fun addWatermark(
        cover: CoverType,
        watermark: String,
    ): Result<CoverType>

    fun addWatermark(
        cover: CoverType,
        watermark: ByteArray,
    ): Result<CoverType>

    fun addWatermark(
        cover: CoverType,
        watermark: Watermark,
    ): Result<CoverType>

    fun containsWatermark(cover: CoverType): Boolean

    fun getWatermarks(
        cover: CoverType,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>>

    fun removeWatermark(cover: CoverType): Result<CoverType>
}
