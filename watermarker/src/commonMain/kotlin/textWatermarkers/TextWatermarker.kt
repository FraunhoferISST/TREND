/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.textWatermarkers

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

/**
 * Concept: Client application specific Transcoding, Strategy, and Placement are passed to the
 * concrete implementation at creation and not modified after
 *
 * Ideas like zerowidth etc. would be handled by separate, as yet unimplemented concrete
 * Watermarkers since it requires differences in the actual add functions, not just Transcoding,
 * Strategy, and Placement
 *
 * The basic idea of inserting Unicode Chars between words of a given String remains unchanged
 * throughout implementations of this Interface
 */

interface TextWatermarker {
    /**
     * Watermarks [cover] String with a Watermark containing [watermark] String
     */
    fun addWatermark(
        cover: String,
        watermark: String,
    ): Result<String>

    /**
     * Watermarks [cover] String with a Watermark containing [watermark] Bytes
     */
    fun addWatermark(
        cover: String,
        watermark: ByteArray,
    ): Result<String>

    fun addWatermark(
        cover: String,
        watermark: Watermark,
    ): Result<String>

    /**
     * Checks the provided [cover] String for Watermark characters
     */
    fun containsWatermark(cover: String): Boolean

    /**
     * Extracts and returns Watermarks
     */
    fun getWatermarks(
        cover: String,
        squash: Boolean = false,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>>

    /**
     * Returns the provided [cover] String with any Watermark characters replaced with regular
     * spaces.
     */
    fun removeWatermarks(cover: String): Result<String>
}
