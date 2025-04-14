/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

package de.fraunhofer.isst.innamark.watermarker.fileWatermarker

import de.fraunhofer.isst.innamark.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTagBuilder
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.toInnamarks
import de.fraunhofer.isst.innamark.watermarker.watermarks.toTextWatermarks
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

    /** Adds a [innamarkTagBuilder] to [file] */
    @JsName("addInnamarkBuilder")
    fun addWatermark(
        file: File,
        innamarkTagBuilder: InnamarkTagBuilder,
    ): Status {
        return addWatermark(file, innamarkTagBuilder.finish())
    }

    /** Checks if [file] contains watermarks */
    fun containsWatermark(file: File): Boolean

    /**
     * Returns all watermarks in [file]
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    fun getWatermarks(
        file: File,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>>

    /**
     * Returns all watermarks in [file] as Trendmarks.
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     * Returns a warning if some watermarks could not be converted to Trendmarks.
     * Returns an error if no watermark could be converted to a Trendmark.
     */
    fun getTrendmarks(
        file: File,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<InnamarkTag>> =
        getWatermarks(file, squash, singleWatermark).toInnamarks(
            "${getSource()}" +
                ".getInnamarks",
        )

    /**
     * Returns all watermarks in [file] as TextWatermarks.
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error.
     *                           is false: invalid bytes sequences are replace with the char �.
     *
     * Returns a warning if some watermarks could not be converted to Trendmarks.
     * Returns an error if no watermark could be converted to a Trendmark.
     *
     * Returns a warning if some Trendmarks could not be converted to TextWatermarks.
     * Returns an error if no Trendmark could be converted to a TextWatermark.
     */
    fun getTextWatermarks(
        file: File,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
        errorOnInvalidUTF8: Boolean = false,
    ): Result<List<TextWatermark>> =
        getWatermarks(file, squash, singleWatermark).toTextWatermarks(
            errorOnInvalidUTF8,
            "${getSource()}" +
                ".getTextWatermarks",
        )

    /**
     * Removes all watermarks in [file] and returns them
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    fun removeWatermarks(
        file: File,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>>

    /** Parses [bytes] as File */
    fun parseBytes(bytes: List<Byte>): Result<File>

    /** Returns the name of the FileWatermark. Used in Event messages. */
    fun getSource(): String
}
