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
open class Watermark(var watermarkContent: List<Byte>) {
    companion object {
        /** Creates a Watermark from [text] */
        fun fromString(text: String): Watermark {
            val bytes = text.encodeToByteArray().asList()
            return Watermark(bytes)
        }
    }

    /** Represents the bytes of the Watermark in hex */
    fun getContentAsText(): String = watermarkContent.toHexString()

    /** Represents the Watermark in a human-readable form */
    override fun toString(): String {
        return "Watermark(${this.getContentAsText()})"
    }

    /** Returns true if other is a watermark and contains the same bytes */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Watermark -> {
                this.watermarkContent == other.watermarkContent
            }

            else -> false
        }
    }

    /** Exposes content.hashCode() */
    override fun hashCode(): Int = watermarkContent.hashCode()
}
