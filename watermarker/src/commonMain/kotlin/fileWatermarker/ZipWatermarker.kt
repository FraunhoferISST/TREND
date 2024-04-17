/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
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
class ZipWatermark(content: List<Byte>) : Watermark(content) {
    /** Represents the Watermark by [watermarkContent] as String */
    override fun toString(): String = this.watermarkContent.toByteArray().contentToString()
}

@JsExport
object ZipWatermarker : FileWatermarker<ZipFile, ZipWatermark> {
    /**
     * Adds a [watermark] to [file]
     * Returns an error if the size of the ExtraFields exceed UShort::MAX
     */
    override fun addWatermark(
        file: ZipFile,
        watermark: Watermark,
    ): Status {
        return file.header.addExtraField(ZIP_WATERMARK_ID, watermark.watermarkContent)
    }

    /** Checks if [file] contains a watermark */
    override fun containsWatermark(file: ZipFile): Boolean {
        for (extraField in file.header.extraFields) {
            if (extraField.id == ZIP_WATERMARK_ID) return true
        }
        return false
    }

    /** Returns all watermarks in [file] */
    override fun getWatermarks(file: ZipFile): Result<List<ZipWatermark>> {
        val watermarks = ArrayList<ZipWatermark>()
        for (extraField in file.header.extraFields) {
            if (extraField.id == ZIP_WATERMARK_ID) {
                watermarks.add(ZipWatermark(extraField.data))
            }
        }
        return Result.success(watermarks)
    }

    /** Removes all watermarks in [file] and returns them */
    override fun removeWatermarks(file: ZipFile): Result<List<ZipWatermark>> {
        val watermarks = ArrayList<ZipWatermark>()
        for (extraField in file.header.removeExtraFields(ZIP_WATERMARK_ID)) {
            watermarks.add(ZipWatermark(extraField.data))
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
