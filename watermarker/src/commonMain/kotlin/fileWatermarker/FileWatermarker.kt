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
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark

interface FileWatermarker<File : WatermarkableFile, SpecificWatermark : Watermark> {
    /** Adds a [watermark] to [file] */
    fun addWatermark(
        file: File,
        watermark: Watermark,
    ): Status

    /** Checks if [file] contains watermarks */
    fun containsWatermark(file: File): Boolean

    /** Returns all watermarks in [file] */
    fun getWatermarks(file: File): Result<List<SpecificWatermark>>

    /** Removes all watermarks in [file] and returns them */
    fun removeWatermarks(file: File): Result<List<SpecificWatermark>>

    /** Parses [bytes] as File */
    fun parseBytes(bytes: List<Byte>): Result<File>
}
