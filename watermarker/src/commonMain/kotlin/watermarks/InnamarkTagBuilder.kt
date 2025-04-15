/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.watermarks

import kotlin.js.JsExport

/**
 * The InnamarkTagBuilder interface is designed to create classes for generating a InnamarkTag from
 * various types of data. Implementing classes will provide the specific logic to convert different
 * data types, such as text or other formats, into a list of bytes which will make up the
 * InnamarkTag's content. This interface ensures that all types of InnamarksTag maintain a
 * consistent structure and can be processed in a standardized way by the watermarking library.
 */
@JsExport
interface InnamarkTagBuilder {
    /** Generates a InnamarkTag with the specific content */
    fun finish(): InnamarkTag

    /** Represents the InnamarkTag with its content in a human-readable way */
    override fun toString(): String

    /** Returns true if [this].finish() and [other].finish() produce an equal InnamarkTag */
    override fun equals(other: Any?): Boolean
}
