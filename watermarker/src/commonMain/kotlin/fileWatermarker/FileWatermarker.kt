/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

package de.fraunhofer.isst.trend.watermarker.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.Textmark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.TrendmarkBuilder
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.toTextmarks
import de.fraunhofer.isst.trend.watermarker.watermarks.toTrendmarks
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
interface FileWatermarker<File : WatermarkableFile> {
    @JsName("addWatermarkBytes")
    fun addWatermark(
        file: File,
        watermark: List<Byte>,
    ): Status

    /** Adds a [watermark] to [file] */
    fun addWatermark(
        file: File,
        watermark: Watermark,
    ): Status {
        return addWatermark(file, watermark.watermarkContent)
    }

    /** Adds a [trendmarkBuilder] to [file] */
    @JsName("addTrendmarkBuilder")
    fun addWatermark(
        file: File,
        trendmarkBuilder: TrendmarkBuilder,
    ): Status {
        return addWatermark(file, trendmarkBuilder.finish())
    }

    /** Checks if [file] contains watermarks */
    fun containsWatermark(file: File): Boolean

    /** Returns all watermarks in [file] */
    fun getWatermarks(file: File): Result<List<Watermark>>

    /**
     * Returns all watermarks in [file] as Trendmarks.
     *
     * Returns a warning if some watermarks could not be converted to Trendmarks.
     * Returns an error if no watermark could be converted to a Trendmark.
     */
    fun getTrendmarks(file: File): Result<List<Trendmark>> =
        getWatermarks(file).toTrendmarks("${getSource()}.getTrendmarks")

    /**
     * Returns all watermarks in [file] as Textmark.
     *
     * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error.
     *                           is false: invalid bytes sequences are replace with the char �.
     *
     * Returns a warning if some watermarks could not be converted to Trendmarks.
     * Returns an error if no watermark could be converted to a Trendmark.
     *
     * Returns a warning if some Trendmarks could not be converted to Textmarks.
     * Returns an error if no Trendmark could be converted to a Textmark.
     */
    fun getTextmarks(
        file: File,
        errorOnInvalidUTF8: Boolean = false,
    ): Result<List<Textmark>> =
        getWatermarks(file).toTextmarks(errorOnInvalidUTF8, "${getSource()}.getTextmarks")

    /** Removes all watermarks in [file] and returns them */
    fun removeWatermarks(file: File): Result<List<Watermark>>

    /** Parses [bytes] as File */
    fun parseBytes(bytes: List<Byte>): Result<File>

    /** Returns the name of the FileWatermark. Used in Event messages. */
    fun getSource(): String
}
