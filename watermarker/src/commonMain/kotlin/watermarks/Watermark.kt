/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.watermarks

import de.fraunhofer.isst.trend.watermarker.helper.toHexString
import kotlin.js.JsExport

@JsExport
open class Watermark(val content: List<Byte>) {
    /** Represents the bytes of the watermark in hex */
    override fun toString(): String = content.toHexString()

    /** Returns true if other is a watermark and contains the same bytes */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Watermark -> {
                this.content == other.content
            }

            else -> false
        }
    }

    /** Exposes content.hashCode() */
    override fun hashCode(): Int = content.hashCode()
}
