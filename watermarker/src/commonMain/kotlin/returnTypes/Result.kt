/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

package de.fraunhofer.isst.trend.watermarker.returnTypes

import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmName

/**
 * This class represents the result of a function that returns a value but also can fail or produce
 * warnings.
 *
 * A Result with a Status of type Success or Warning are expected to have a value.
 */
@JsExport
class Result<T> constructor(
    val status: Status,
    val value: T? = null,
) {
    /** Checks if the result contains a value */
    @get:JvmName("hasValue")
    val hasValue get() = value != null

    /** Creates a new Result<T> from [status] and [value] */
    @JsName("intoResult")
    fun <T> into(value: T? = null) = Result(this.status, value)

    /** Converts Result<T> to Status */
    @JsName("intoStatus")
    fun into(): Status = this.status

    /**
     * Combines [this.status] with [status] by appending all events from [status] to [this.status].
     * By default, the highest severity from [this.status] and [status] gets chosen. If
     * [overrideSeverity] is true [status]'s severity gets chosen.
     */
    fun appendStatus(status: Status) = this.status.appendStatus(status)

    /**
     * Combines [this.status] with [status] by prepending all events from [status] to [this.status].
     * By default, the highest severity from [this.status] and [status] gets chosen. If
     * [overrideSeverity] is true [status]'s severity gets chosen.
     */
    fun prependStatus(status: Status) = this.status.prependStatus(status)

    val isSuccess: Boolean get() = status.isSuccess
    val isWarning: Boolean get() = status.isWarning
    val isError: Boolean get() = status.isError

    /**
     * Returns the message representing the Result containing the messages of all events.
     * It omits the type of each event and only adds the message. Use toString if the event type
     * should be added as well.
     */
    fun getMessage() = status.getMessage()

    /**
     * Returns a String representing the Result containing the messages of all events.
     * It adds the type of each event as well as the message. Use toMessage if the event type
     * should be omitted.
     */
    override fun toString() = status.toString()

    companion object {
        /** Creates a Result<T> with the default Success variant and [value] */
        fun <T> success(value: T): Result<T> = Event.Success().into(value)
    }
}
