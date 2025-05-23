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
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark.MultipleMostFrequentWarning
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark.StringDecodeWarning

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

    /** Returns a [Boolean] indicating whether [cover] contains watermarks */
    override fun containsWatermark(cover: ZipFile): Boolean {
        return watermarker.containsWatermark(cover)
    }

    /**
     * Returns a [Result] containing the most frequent Watermark in [cover] as a String
     *
     * Result contains an empty String if no Watermarks were found.
     * Result contains a [MultipleMostFrequentWarning] in cases where an unambiguous Watermark could not be extracted.
     * Result contains a [StringDecodeWarning] in cases where a byte cannot be read as UTF-8.
     */
    override fun getWatermarkAsString(cover: ZipFile): Result<String> {
        val watermarks = getWatermarks(cover, false, true)
        if (watermarks.value?.isNotEmpty() ?: return Result.success("")) {
            val decoded =
                watermarks.status.into(
                    watermarks.value[0].watermarkContent.decodeToString(),
                )
            if (decoded.value!!.contains('\uFFFD')) {
                decoded.appendStatus(Status(StringDecodeWarning("ZipWatermarkerImpl")))
            }
            return decoded
        } else {
            return Result.success("")
        }
    }

    /**
     * Returns a [Result] containing the most frequent Watermark in [cover] as a ByteArray
     *
     * Result contains an empty ByteArray if no Watermarks were found.
     * Result contains a [MultipleMostFrequentWarning] in cases where an unambiguous Watermark could not be extracted.
     */
    override fun getWatermarkAsByteArray(cover: ZipFile): Result<ByteArray> {
        val watermarks = getWatermarks(cover, false, true)
        if (watermarks.value?.isNotEmpty() ?: return Result.success(ByteArray(0))) {
            return watermarks.status.into(watermarks.value[0].watermarkContent)
        } else {
            return Result.success(ByteArray(0))
        }
    }

    /**
     * Returns a [Result] containing a list of [Watermark]s in [cover]
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    override fun getWatermarks(
        cover: ZipFile,
        squash: Boolean,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        return watermarker.getWatermarks(cover, squash, singleWatermark)
    }

    /** Removes all watermarks from [cover] and returns a [Result] containing the cleaned cover */
    override fun removeWatermark(cover: ZipFile): Result<ZipFile> {
        val status = watermarker.removeWatermarks(cover).status
        return status.into(cover)
    }
}
