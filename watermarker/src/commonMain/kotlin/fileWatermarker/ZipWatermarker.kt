/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.files.ZipFile
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import kotlin.js.JsExport

const val ZIP_WATERMARK_ID: UShort = 0x8777u

@JsExport
object ZipWatermarker : FileWatermarker<ZipFile> {
    const val SOURCE = "ZipWatermarker"

    /** Returns the name of the FileWatermark. Used in Event messages. */
    override fun getSource(): String = SOURCE

    /**
     * Adds a [watermark] to [file]
     * Returns an error if the size of the ExtraFields exceed UShort::MAX
     */
    override fun addWatermark(
        file: ZipFile,
        watermark: List<Byte>,
    ): Status {
        return file.header.addExtraField(ZIP_WATERMARK_ID, watermark)
    }

    /** Checks if [file] contains a watermark */
    override fun containsWatermark(file: ZipFile): Boolean {
        for (extraField in file.header.extraFields) {
            if (extraField.id == ZIP_WATERMARK_ID) return true
        }
        return false
    }

    /**
     * Returns all watermarks in [file]
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    override fun getWatermarks(
        file: ZipFile,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        val watermarks = ArrayList<Watermark>()
        for (extraField in file.header.extraFields) {
            if (extraField.id == ZIP_WATERMARK_ID) {
                watermarks.add(Watermark(extraField.data))
            }
        }
        if (singleWatermark) {
            return Watermark.mostFrequent(watermarks)
        }
        return Result.success(watermarks)
    }

    /**
     * Removes all watermarks in [file] and returns them
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    override fun removeWatermarks(
        file: ZipFile,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        val watermarks = ArrayList<Watermark>()
        for (extraField in file.header.removeExtraFields(ZIP_WATERMARK_ID)) {
            watermarks.add(Watermark(extraField.data))
        }
        if (singleWatermark) {
            return Watermark.mostFrequent(watermarks)
        }
        return Result.success(watermarks)
    }

    /**
     * Parses [bytes] as zip file.
     * Parsing includes separating the header from the content and parsing the header.
     *
     * Returns errors if it cannot parse [bytes] as zip file.
     * Returns warnings if the parser finds unexpected structures but is still able to parse it
     */
    override fun parseBytes(bytes: List<Byte>): Result<ZipFile> {
        return ZipFile.fromBytes(bytes.toByteArray())
    }
}
