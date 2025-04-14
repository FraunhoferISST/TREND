/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.helper

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Event
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import kotlin.js.JsExport

const val COMPRESSION_LEVEL: Int = 9

expect object Compression {
    /** Compresses [data] using the deflate algorithm */
    fun deflate(data: List<Byte>): List<Byte>

    /** Uncompresses [data] using the inflate algorithm */
    fun inflate(data: List<Byte>): Result<List<Byte>>
}

@JsExport
class InflationError(val reason: String) : Event.Error("Compression.inflate") {
    /** Returns a String explaining the event */
    override fun getMessage() = "Error inflating bytes: $reason."
}
