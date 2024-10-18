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
 * An Event represents different kinds of events that can occur during the execution of a function.
 * There are 3 base variants:
 *  - Success
 *  - Warning
 *  - Error.
 *
 * The variants Warning and Error are abstract: For each event that can occur, a unique class must
 * be created that overrides fun getMessage(): String to explain the event.
 */
@JsExport
sealed class Event(
    val source: String?,
    val severity: Int,
    val representation: String,
) {
    /** Returns a String explaining the event */
    abstract fun getMessage(): String?

    @get:JvmName("hasCustomMessage")
    val hasCustomMessage: Boolean get() = getMessage() != null

    /** Returns a String explaining the event: "Type (source): message" */
    override fun toString(): String {
        var message = this.representation

        if (this.source != null) {
            message += " (${this.source})"
        }

        if (this.hasCustomMessage) {
            message += ": ${this.getMessage()}"
        }

        return message
    }

    /**
     * Variant Success can be used directly if no custom message is needed.
     * Otherwise, a new class has to be created that inherits from Success.
     */
    open class Success(source: String? = null) : Event(source, 0, SUCCESS) {
        /** Returns a String explaining the event */
        override fun getMessage(): String? = null
    }

    /**
     * Variant Warning must contain a custom message and therefore a new class has to be created
     * that inherits from Warning.
     */
    abstract class Warning(source: String) : Event(source, 1, WARNING)

    /**
     * Variant Error must contain a custom message and therefore a new class has to be created
     * that inherits from Error.
     */
    abstract class Error(source: String) : Event(source, 2, ERROR)

    /** Creates a Status containing [this] event */
    @JsName("intoStatus")
    fun into(): Status = Status(this)

    /** Creates a Result<T> with a Status containing [this] event */
    @JsName("intoResult")
    fun <T> into(value: T? = null): Result<T> = Result(this.into(), value)

    companion object {
        // String representation of the respective events
        const val SUCCESS: String = "Success"
        const val WARNING: String = "Warning"
        const val ERROR: String = "Error"
    }
}

/**
 * A Status represents the outcome of a function that can produces Errors and Warnings, holding any
 * number of events.
 */
@JsExport
class Status(event: Event? = null) {
    /**
     * Represents the severity of the Status. Used to be able to override the severity given by
     * the events.
     */
    internal enum class Type(val representation: String, val severity: Int) {
        SUCCESS(Event.SUCCESS, 0),
        WARNING(Event.WARNING, 1),
        ERROR(Event.ERROR, 2),
    }

    /** List of all Events that belong to the Status */
    private val eventList: MutableList<Event> = ArrayList()

    /**
     * The severity of the Status. Automatically set by choosing the highest severity from
     * [eventList] but can be overridden with the addEvent overrideSeverity parameter.
     */
    private var type: Type = Type.SUCCESS

    init {
        event?.let { addEvent(it) }
    }

    /** Checks if type is SUCCESS */
    val isSuccess: Boolean get() = type == Type.SUCCESS

    /** Checks if type is WARNING */
    val isWarning: Boolean get() = type == Type.WARNING

    /** Checks if type is ERROR */
    val isError: Boolean get() = type == Type.ERROR

    /** Check if any event contains a custom message */
    @get:JvmName("hasCustomMessage")
    val hasCustomMessage: Boolean get() = eventList.any { it.hasCustomMessage }

    /**
     * Adds [event] to [eventList] (if it contains a custom message) and updates [type] if the
     * severity of [event] is higher than the current severity of [type]. This behavior can be
     * changed to always override the severity to the [event]'s severity by setting
     * [overrideSeverity] to true.
     */
    fun addEvent(
        event: Event,
        overrideSeverity: Boolean = false,
    ) {
        if (overrideSeverity || event.severity > type.severity) {
            type =
                when (event) {
                    is Event.Error -> Type.ERROR
                    is Event.Warning -> Type.WARNING
                    is Event.Success -> Type.SUCCESS
                }
        }

        // only add events that contain additional information
        if (event.hasCustomMessage) {
            eventList.add(event)
        }
    }

    /** Returns a list containing all Events */
    fun getEvents(): List<Event> = eventList.toList()

    /**
     * Combines [this] with [other] by appending all events from [other] to [this]. By default, the
     * highest severity from [this] and [other] gets chosen. If [overrideSeverity] is true
     * [other]'s severity gets chosen.
     */
    fun appendStatus(
        other: Status,
        overrideSeverity: Boolean = false,
    ) {
        other.eventList.forEach { event -> addEvent(event) }

        if (overrideSeverity || this.type.severity < other.type.severity) {
            this.type = other.type
        }
    }

    /**
     * Combines [this] with [other] by prepending all events from [other] to [this]. By default, the
     * highest severity from [this] and [other] gets chosen. If [overrideSeverity] is true
     * [other]'s severity gets chose.
     */
    fun prependStatus(
        other: Status,
        overrideSeverity: Boolean = false,
    ) {
        if (overrideSeverity || this.type.severity < other.type.severity) {
            this.type = other.type
        }

        this.eventList.addAll(0, other.eventList)
    }

    /**
     * Returns the message representing the Status containing the messages of all events.
     * It omits the type of each event and only adds the message. Use toString if the event type
     * should be added as well.
     */
    fun getMessage(): String {
        if (eventList.isEmpty()) {
            return type.representation
        }

        return eventList.map { e -> e.getMessage() }.joinToString(separator = "\n")
    }

    /**
     * Returns a String representing the Status containing the messages of all events.
     * It adds the type of each event as well as the message. Use toMessage if the event type
     * should be omitted.
     */
    override fun toString(): String {
        if (eventList.isEmpty()) {
            return type.representation
        }

        return eventList.joinToString(separator = "\n")
    }

    /** Creates a Result<T> containing [this] as Status and [value]. */
    fun <T> into(value: T? = null): Result<T> = Result(this, value)

    companion object {
        /** Creates a Status with the default Success variant */
        fun success(): Status = Event.Success().into()
    }
}
