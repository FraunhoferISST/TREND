/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import de.fraunhofer.isst.trend.watermarker.JvmWatermarker
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.optional
import kotlin.math.min
import kotlin.system.exitProcess

val watermarker = JvmWatermarker()

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        cli(arrayOf("-h"))
    } else {
        cli(args)
    }
}

/**
 * Parses the CLI arguments and calls the corresponding functions
 * Prints usage information if the CLI arguments cannot be parsed
 */
@OptIn(ExperimentalCli::class)
fun cli(args: Array<String>) {
    val parser = ArgParser("watermarker", strictSubcommandOptionsOrder = true)

    val fileType by parser.option(ArgType.String, "file-type", "t", "Specify the file type")
    val verbose by parser.option(ArgType.Boolean, "verbose", "v", "Verbose Output").default(false)

    class Add : Subcommand("add", "Add watermark to a file") {
        val message by argument(
            ArgType.String,
            fullName = "message",
            description = "Watermark message",
        )
        val source by argument(ArgType.String, fullName = "source", description = "Source file")
        val target by argument(
            ArgType.String,
            fullName = "target",
            description = "Target file - if omitted <source> gets overwritten",
        ).optional()

        override fun execute() = add(message, source, target, fileType)
    }

    class List : Subcommand("list", "List watermarks in a file") {
        val source by argument(ArgType.String, fullName = "source", description = "Source file")

        override fun execute() {
            list(source, fileType, verbose)
        }
    }

    class Remove : Subcommand("remove", "Removes all watermarks from a file") {
        val source by argument(ArgType.String, fullName = "source", description = "Source file")
        val target by argument(
            ArgType.String,
            fullName = "target",
            description = "Target file - if omitted <source> gets overwritten",
        ).optional()

        override fun execute() = remove(source, target, fileType)
    }

    class TextAdd : Subcommand("add", "Add watermark to a string") {
        val text by argument(
            ArgType.String,
            fullName = "text",
            description = "String to watermark",
        )
        val message by argument(
            ArgType.String,
            fullName = "message",
            description = "Watermark message",
        )

        override fun execute() {
            textAdd(text, message)
        }
    }

    class TextList : Subcommand("list", "List watermarks in a string") {
        val text by argument(
            ArgType.String,
            fullName = "text",
            description = "String containing a watermark",
        )

        override fun execute() {
            textList(text, verbose)
        }
    }

    class TextRemove : Subcommand("remove", "Removes all watermarks from a string") {
        val text by argument(
            ArgType.String,
            fullName = "text",
            description = "String containing watermark(s)",
        )

        override fun execute() {
            textRemove(text)
        }
    }

    class Text : Subcommand("text", "Watermark text directly") {
        init {
            this.subcommands(TextAdd())
            this.subcommands(TextList())
            this.subcommands(TextRemove())
        }

        override fun execute() {
            if (args.size == 1) {
                cli(arrayOf("text", "-h"))
            }
        }
    }

    parser.subcommands(Add(), List(), Remove(), Text())
    parser.parse(args)
}

/**
 * Adds a watermark containing [message] to file [source]
 * Changes are written to [target] or [source] if target is null
 */
fun add(
    message: String,
    source: String,
    target: String?,
    fileType: String?,
) {
    val realTarget = target ?: source

    val watermark = Watermark.fromString(message)

    watermarker
        .addWatermark(source, realTarget, watermark, fileType)
        .handle()

    println()
    println("Added watermark to ${getTargetHint(source, target)}")
}

/**
 * Prints a list of all watermarks in [source]
 *
 * Uses watermark squashing when [verbose] is true
 */
fun list(
    source: String,
    fileType: String?,
    verbose: Boolean,
) {
    val watermarks = watermarker.getWatermarks(source, fileType, !verbose).unwrap()

    println()
    println("Found ${watermarks.size} watermark(s) in '$source':")
    printWatermarks(watermarks)
}

/**
 * Removes all watermarks from [source] and prints them
 * Changes are written to [target] or [source] if target is null
 *
 * Handles the Results of parsing, removing and writing
 */
fun remove(
    source: String,
    target: String?,
    fileType: String?,
) {
    val realTarget = target ?: source

    val removedWatermarks = watermarker.removeWatermarks(source, realTarget, fileType).unwrap()

    println()
    println("Removed ${removedWatermarks.size} watermark(s) from ${getTargetHint(source, target)}:")
    printWatermarks(removedWatermarks)
}

/** Adds a watermark containing [message] to [text] and prints the resulting string */
fun textAdd(
    text: String,
    message: String,
) {
    val watermarkedText =
        watermarker.textAddWatermark(text, message.encodeToByteArray().asList()).unwrap()
    println("-- Watermarked Text " + "-".repeat(60))
    println(watermarkedText)
    println("-".repeat(80))
}

/**
 * Prints a list of all watermarks in [text]
 *
 * Uses watermark squashing when [verbose] is true
 */
fun textList(
    text: String,
    verbose: Boolean,
) {
    val watermarks = watermarker.textGetWatermarks(text, !verbose).unwrap()
    println()
    println("Found ${watermarks.size} watermark(s):")
    printWatermarks(watermarks)
}

/** Removes all watermarks from [text] and prints the resulting string */
fun textRemove(text: String) {
    if (watermarker.textContainsWatermark(text)) {
        val cleaned = watermarker.textRemoveWatermarks(text).unwrap()
        println("-- Cleaned Text " + "-".repeat(60))
        println(cleaned)
        println("-".repeat(80))
    } else {
        println("Unable to find any watermarks in the text.")
    }
}

/**
 * returns a String that indicates if changes where written to
 *  - [target]: if [target] is not null
 *  - [source]: else
 */
fun getTargetHint(
    source: String,
    target: String?,
): String =
    if (target != null) {
        "$source and wrote changes to $target"
    } else {
        source
    }

/** Prints each watermark in [watermarks] with a separator between each one */
fun printWatermarks(watermarks: List<Watermark>) {
    val indexStringLen = min(watermarks.size.toString().length, 70)

    for ((index, watermark) in watermarks.withIndex()) {
        print("-- %${indexStringLen}d ".format(index + 1))
        println("-".repeat(80 - indexStringLen - 4))
        println("'${watermark.watermarkContent.toByteArray().decodeToString()}'")
    }

    if (watermarks.isNotEmpty()) println("-".repeat(80))
}

/** Prints each event to STDOUT or STDERR using the toString Method if type is not SUCCESS */
fun Status.print() {
    if (this.getEvents().isEmpty()) {
        when {
            this.isSuccess -> println("Success")
            this.isWarning -> System.err.println("Warning")
            this.isError -> System.err.println("Error")
        }
    } else {
        for (event in this.getEvents()) {
            when (event) {
                is Event.Success -> println(event)
                is Event.Warning, is Event.Error -> System.err.println(event)
            }
        }
    }
}

/**
 * Handles a status depending on its variant:
 * Variant Error:
 *  - print error and exit with code -1
 * Variant Warning:
 *  - print warning
 * Variant Success:
 *  - nop
 */
fun Status.handle() {
    if (isSuccess && !hasCustomMessage) {
        return
    }

    this.print()

    if (this.isError) {
        exitProcess(-1)
    }
}

/**
 * Unwraps a Result depending on its variant:
 * Variant Error:
 *  - print error and exit with code -1
 * Variant Warning:
 *  - print warning
 *  - return non-null value
 * Variant Success:
 *  - return non-null value
 */
fun <T> Result<T>.unwrap(): T {
    this.status.handle()
    checkNotNull(value) {
        "A Result with a Status of type Success or Warning are expected to have a value"
    }

    return value!!
}
