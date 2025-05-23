/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.binaryWatermarkers

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.ZipWatermarker
import de.fraunhofer.isst.innamark.watermarker.files.ZipFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

/**
 * Implementation of [BinaryWatermarker] for [ZipFile] covers
 */
class ZipWatermarkerImpl : BinaryWatermarker<ZipFile> {
    private val watermarker = ZipWatermarker

    /**
     * Adds a watermark created from [watermark] String to [cover]
     * Returns an error if the size of the ExtraFields exceed UShort::MAX
     */
    override fun addWatermark(
        cover: ZipFile,
        watermark: String,
    ): Result<ZipFile> {
        val status = watermarker.addWatermark(cover, Watermark.fromString(watermark))
        return status.into(cover)
    }

    /**
     * Adds a watermark created from [watermark] ByteArray to [cover]
     * Returns an error if the size of the ExtraFields exceed UShort::MAX
     */
    override fun addWatermark(
        cover: ZipFile,
        watermark: ByteArray,
    ): Result<ZipFile> {
        val status = watermarker.addWatermark(cover, watermark)
        return status.into(cover)
    }

    /**
     * Adds watermark object [watermark] to [cover]
     * Returns an error if the size of the ExtraFields exceed UShort::MAX
     */
    override fun addWatermark(
        cover: ZipFile,
        watermark: Watermark,
    ): Result<ZipFile> {
        val status = watermarker.addWatermark(cover, watermark)
        return status.into(cover)
    }

    override fun containsWatermark(cover: ZipFile): Boolean {
        return watermarker.containsWatermark(cover)
    }

    override fun getWatermarkAsString(cover: ZipFile): Result<String> {
        val watermarks = getWatermarks(cover, false, true)
        if (watermarks.value?.isNotEmpty() ?: return Result.success("")) {
            return watermarks.status.into(watermarks.value[0].watermarkContent.decodeToString())
        } else {
            return Result.success("")
        }
    }

    override fun getWatermarkAsByteArray(cover: ZipFile): Result<ByteArray> {
        val watermarks = getWatermarks(cover, false, true)
        if (watermarks.value?.isNotEmpty() ?: return Result.success(ByteArray(0))) {
            return watermarks.status.into(watermarks.value[0].watermarkContent)
        } else {
            return Result.success(ByteArray(0))
        }
    }

    override fun getWatermarks(
        cover: ZipFile,
        squash: Boolean,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        return watermarker.getWatermarks(cover, squash, singleWatermark)
    }

    override fun removeWatermark(cover: ZipFile): Result<ZipFile> {
        val status = watermarker.removeWatermarks(cover).status
        return status.into(cover)
    }
}
