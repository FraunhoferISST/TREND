/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.watermarks

/**
 * The TrendmarkBuilder interface is designed to create classes for generating a Trendmark from
 * various types of data. Implementing classes will provide the specific logic to convert different
 * data types, such as text or other formats, into a list of bytes which will make up the
 * Trendmark's content. This interface ensures that all types of Trendmarks maintain a consistent
 * structure and can be processed in a standardized way by the watermarking library.
 */
interface TrendmarkBuilder {
    /** Generates a Trendmark with the specific content */
    fun finish(): Trendmark

    /** Represents the Trendmark with its content in a human-readable way */
    override fun toString(): String

    /** Returns true if [this].finish() and [other].finish() produce an equal Trendmark */
    override fun equals(other: Any?): Boolean
}
