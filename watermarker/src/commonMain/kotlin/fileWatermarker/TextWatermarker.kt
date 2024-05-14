/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.helper.Compression
import de.fraunhofer.isst.trend.watermarker.helper.toIntUnsigned
import de.fraunhofer.isst.trend.watermarker.helper.toUnicodeRepresentation
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.DecodeToStringError
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import kotlin.js.JsExport
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.pow

/** Defines how multiple watermarks are separated */
@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
sealed class SeparatorStrategy {
    /** Leaves one insertable position empty to mark the end of a watermark */
    object SkipInsertPosition : SeparatorStrategy()

    /** Inserts [char] as separator between watermarks */
    class SingleSeparatorChar(val char: Char) : SeparatorStrategy()

    /** Inserts [start] before a Watermark and [end] after a Watermark as separators */
    class StartEndSeparatorChars(val start: Char, val end: Char) : SeparatorStrategy()
}

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
interface Transcoding {
    val alphabet: List<Char>

    /** Encodes [bytes] to a chars of [alphabet] */
    fun encode(bytes: List<Byte>): Sequence<Char>

    /** Decodes [chars] of [alphabet] to bytes */
    fun decode(chars: Sequence<Char>): Result<List<Byte>>
}

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
object DefaultTranscoding : Transcoding {
    override val alphabet =
        listOf(
            // Punctuation space
            '\u2008',
            // Thin space
            '\u2009',
            // Narrow-no-break space
            '\u202F',
            // Medium mathematical space
            '\u205F',
        )
    const val SEPARATOR_CHAR = '\u2004' // Three-per-em space

    private val base = alphabet.size
    private val digitsPerByte = calculateDigitsPerByte(base)
    private val digitToNumber = HashMap<Char, Int>()

    init {
        // Generate HashMap from Digit to numerical Value (i.e. index of the digit)
        for ((index, char) in alphabet.withIndex()) {
            digitToNumber[char] = index
        }
    }

    /** Encodes [bytes] to a chars of [alphabet] */
    override fun encode(bytes: List<Byte>): Sequence<Char> =
        sequence {
            for (byte in bytes) {
                var iByte = byte.toIntUnsigned()
                repeat(digitsPerByte) {
                    val digit = iByte % base
                    iByte /= base

                    yield(alphabet[digit])
                }
                check(iByte == 0)
            }
        }

    /** Decodes [chars] of [alphabet] to bytes */
    override fun decode(chars: Sequence<Char>): Result<List<Byte>> {
        val status = Status()
        val dBase = base.toDouble()

        val result = ArrayList<Byte>()
        for (byte in chars.chunked(digitsPerByte)) {
            var iByte = 0
            for ((index, digit) in byte.withIndex()) {
                iByte += digitToNumber[digit]!! * dBase.pow(index).toInt()
            }
            if (iByte in 0..255) {
                result.add(iByte.toByte())
            } else {
                status.addEvent(DecodingInvalidByteError(iByte))
            }
        }

        return status.into(result)
    }

    /** Calculates how many digits are required for a byte in given base ([alphabetSize]) */
    private fun calculateDigitsPerByte(alphabetSize: Int) =
        ceil(log(256.0, 2.0) / log(alphabetSize.toDouble(), 2.0)).toInt()

    private const val SOURCE = "DefaultTranscoding"

    class DecodingInvalidByteError(val invalidByte: Int) :
        Event.Warning("$SOURCE.decode") {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Decoding produced an invalid byte: $invalidByte"
    }
}

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
class TextWatermark private constructor(
    content: List<Byte>,
    private val compression: Boolean,
) : Watermark(content) {
    /** Returns the uncompressed bytes from the Watermark */
    fun getBytes(): Result<List<Byte>> {
        return if (compression) {
            Compression.inflate(content)
        } else {
            Result.success(content)
        }
    }

    /** Parses the uncompressed bytes as String */
    fun getText(): Result<String> {
        val (status, bytes) =
            with(getBytes()) {
                status to (value ?: return into<_>())
            }
        return try {
            status.into(bytes.toByteArray().decodeToString())
        } catch (e: Exception) {
            status.addEvent(DecodeToStringError(e.message ?: e.stackTraceToString()))
            status.into()
        }
    }

    /** Represents the Watermark as String from [content] */
    override fun toString(): String {
        val result = getText()
        return if (result.hasValue) {
            result.value!!
        } else {
            return result.toString()
        }
    }

    companion object {
        const val COMPRESSION_DEFAULT = false

        /**
         * Converts a Watermark to a TextWatermark
         *
         * [compression] defines if the bytes are compressed
         */
        fun fromWatermark(
            watermark: Watermark,
            compression: Boolean = COMPRESSION_DEFAULT,
        ): TextWatermark = TextWatermark(watermark.content, compression)

        /**
         * Creates a TextWatermark from [text] using deflate compression
         *
         * [compression] defines whether the bytes will be compressed
         */
        fun fromText(
            text: String,
            compression: Boolean = COMPRESSION_DEFAULT,
        ): TextWatermark {
            val bytes = text.encodeToByteArray().asList()
            return fromUncompressedBytes(bytes, compression)
        }

        /**
         * Creates a TextWatermark from [bytes]
         *
         * [compression] defines whether the bytes will be compressed
         */
        fun fromUncompressedBytes(
            bytes: List<Byte>,
            compression: Boolean = COMPRESSION_DEFAULT,
        ): TextWatermark {
            return if (compression) {
                val deflated = Compression.deflate(bytes)
                TextWatermark(deflated, compression)
            } else {
                TextWatermark(bytes, compression)
            }
        }

        /**
         * Creates a TextWatermark from [bytes]
         *
         * [compression] defines whether the bytes will be compressed
         */
        fun fromCompressedBytes(
            bytes: List<Byte>,
            compression: Boolean = COMPRESSION_DEFAULT,
        ): Result<TextWatermark> {
            return if (compression) {
                Result.success(TextWatermark(bytes, compression))
            } else {
                val (status, inflated) =
                    with(Compression.inflate(bytes)) {
                        if (!hasValue) {
                            return status.into()
                        }
                        status to value!!
                    }
                status.into(TextWatermark(inflated, compression))
            }
        }
    }
}

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
class TextWatermarker(
    private val transcoding: Transcoding,
    private val separatorStrategy: SeparatorStrategy,
    val compression: Boolean,
    val placement: (String) -> Sequence<Int>,
) : FileWatermarker<TextFile, TextWatermark> {
    // Build a list of all chars that are contained in a watermark
    private val fullAlphabet: List<Char> =
        when (separatorStrategy) {
            is SeparatorStrategy.SkipInsertPosition -> transcoding.alphabet
            is SeparatorStrategy.SingleSeparatorChar ->
                listOf(separatorStrategy.char) + transcoding.alphabet

            is SeparatorStrategy.StartEndSeparatorChars ->
                listOf(separatorStrategy.start, separatorStrategy.end) + transcoding.alphabet
        }

    /**
     * Adds a [watermark] to [file]
     * Returns a warning if the watermark does not fit at least a single time into the file
     * Returns an error if the text file contains a character from the transcoding alphabet
     */
    override fun addWatermark(
        file: TextFile,
        watermark: Watermark,
    ): Status {
        // Check if file contains any chars from the used alphabet
        if (file.content.any { char -> char in fullAlphabet }) {
            val containedChars =
                fullAlphabet.asSequence()
                    .filter { char -> char in file.content }

            return ContainsAlphabetCharsError(containedChars).into()
        }

        val insertPositions = placement(file.content)
        val separatedWatermark = getSeparatedWatermark(watermark)

        // Insert watermark
        val positionChunks =
            when (separatorStrategy) {
                is SeparatorStrategy.SkipInsertPosition ->
                    insertPositions.chunked(separatedWatermark.count() + 1)

                else -> insertPositions.chunked(separatedWatermark.count())
            }
        val startPositions = ArrayList<Int>()

        var result = ""
        var lastPosition = 0
        for (positions in positionChunks) {
            startPositions.add(positions.first())
            for ((position, char) in positions.asSequence().zip(separatedWatermark)) {
                result += file.content.substring(lastPosition, position) + char
                lastPosition = position + 1
            }
        }
        result += file.content.substring(lastPosition, file.content.length)

        file.content = result

        // Check if watermark fits at least one time into the text file with given positioning
        if (insertPositions.count() < separatedWatermark.count()) {
            return OversizedWatermarkWarning(
                separatedWatermark.count(),
                insertPositions.count(),
            ).into()
        }

        return Success(startPositions).into()
    }

    /** Checks if [file] contains a watermark */
    override fun containsWatermark(file: TextFile): Boolean =
        file.content.any { char ->
            char in fullAlphabet
        }

    /** Returns all watermarks in [file] */
    override fun getWatermarks(file: TextFile): Result<List<TextWatermark>> {
        val watermarkRanges: Sequence<Pair<Int, Int>> =
            when (separatorStrategy) {
                is SeparatorStrategy.SkipInsertPosition -> {
                    val insertPositions = placement(file.content)
                    val separatorPositions =
                        insertPositions.filter { position ->
                            position > 0 && file.content[position - 1] !in transcoding.alphabet
                        }
                    sequence {
                        var lastSeparatorPosition = 0
                        for (separatorPosition in separatorPositions) {
                            yield(lastSeparatorPosition to separatorPosition)
                            lastSeparatorPosition = separatorPosition
                        }
                    }
                }

                is SeparatorStrategy.SingleSeparatorChar -> {
                    val separatorPositions =
                        file.content.asSequence().withIndex().filter { (_, char) ->
                            char == separatorStrategy.char
                        }
                            .map { (position, _) -> position }

                    sequence {
                        var lastSeparatorPosition = 0
                        for (separatorPosition in separatorPositions) {
                            yield(lastSeparatorPosition to separatorPosition - 1)
                            lastSeparatorPosition = separatorPosition + 1
                        }
                    }
                }

                is SeparatorStrategy.StartEndSeparatorChars -> {
                    sequence {
                        var lastEndPosition = 0
                        var startPosition: Int? = null
                        for ((position, char) in file.content.withIndex()) {
                            when (char) {
                                separatorStrategy.start -> startPosition = position + 1
                                separatorStrategy.end -> {
                                    if (startPosition == null) {
                                        startPosition = lastEndPosition + 1
                                    }
                                    yield(startPosition to position - 1)
                                    lastEndPosition = position
                                }
                            }
                        }
                    }
                }
            }

        val sanitizedWatermarkRanges =
            if (watermarkRanges.count() <= 0) {
                sequenceOf(0 to file.content.length)
            } else {
                watermarkRanges
            }

        val status = Status()
        val watermarks = ArrayList<TextWatermark>()
        for ((start, end) in sanitizedWatermarkRanges) {
            val content =
                file.content.asSequence()
                    .drop(start)
                    .take(end - start + 1)
                    .filter { char -> char in transcoding.alphabet }

            if (content.count() > 0) {
                val decoded =
                    with(transcoding.decode(content)) {
                        if (isSuccess) {
                            value!!
                        } else if (value != null) {
                            status.appendStatus(this.status)
                            value
                        } else {
                            return this.status.into()
                        }
                    }

                if (compression) {
                    with(TextWatermark.fromCompressedBytes(decoded, compression)) {
                        if (!status.isSuccess) {
                            this.status.appendStatus(status)
                        }
                        if (hasValue) {
                            watermarks.add(value!!)
                        }
                    }
                } else {
                    watermarks.add(TextWatermark.fromUncompressedBytes(decoded, compression))
                }
            }
        }

        if (watermarkRanges.count() <= 0 && watermarks.size > 0) {
            status.addEvent(IncompleteWatermarkWarning())
        }

        return status.into(watermarks)
    }

    /**
     * Removes all watermarks in [file] and returns them
     *
     * Returns a warning if getWatermarks() returns a warning or error
     */
    override fun removeWatermarks(file: TextFile): Result<List<TextWatermark>> {
        val (status, watermarks) =
            with(this.getWatermarks(file)) {
                status to (value ?: listOf())
            }

        // Replace all chars from the file that are in the transcoding alphabet with a whitespace
        val cleaned =
            file.content.asSequence().map { char ->
                if (char in fullAlphabet) ' ' else char
            }

        file.content = cleaned.toList().toCharArray().concatToString()

        if (!status.isSuccess) {
            status.addEvent(RemoveWatermarksGetProblemWarning(), true)
        }

        return status.into(watermarks)
    }

    /** Parses [bytes] as text and returns it as TextFile */
    override fun parseBytes(bytes: List<Byte>): Result<TextFile> {
        return TextFile.fromBytes(bytes.toByteArray())
    }

    /** Counts the minimum number of insert positions needed in a text to insert the [watermark] */
    fun getMinimumInsertPositions(watermark: Watermark): Int {
        val separatedWatermark = getSeparatedWatermark(watermark)
        return separatedWatermark.count()
    }
    
    /** Counts the available number of insert positions in a [file] */
    fun getAvailableInsertPositions(file: TextFile): Int {
        return placement(file.content).count()
    }

    /** Transforms a [watermark] into a separated watermark */
    private fun getSeparatedWatermark(watermark: Watermark): Sequence<Char> {
        val encodedWatermark = transcoding.encode(watermark.content)

        val separatedWatermark =
            when (separatorStrategy) {
                is SeparatorStrategy.SkipInsertPosition -> encodedWatermark
                is SeparatorStrategy.SingleSeparatorChar ->
                    sequence {
                        yieldAll(encodedWatermark)
                        yield(separatorStrategy.char)
                    }

                is SeparatorStrategy.StartEndSeparatorChars ->
                    sequence {
                        yield(separatorStrategy.start)
                        yieldAll(encodedWatermark)
                        yield(separatorStrategy.end)
                    }
            }

        return separatedWatermark
    }

    companion object {
        private const val SOURCE = "TextWatermarker"

        fun builder(): TextWatermarkerBuilder = TextWatermarkerBuilder()

        fun default(): TextWatermarker = TextWatermarkerBuilder().build().value!!
    }

    class IncompleteWatermarkWarning : Event.Warning("$SOURCE.getWatermark") {
        /** Returns a String explaining the event */
        override fun getMessage() = "Could not restore a complete watermark!"
    }

    class OversizedWatermarkWarning(
        private val watermarkSize: Int,
        private val insertableSize: Int,
    ) : Event.Warning("$SOURCE.addWatermark") {
        /** Returns a String explaining the event */
        override fun getMessage() =
            "Could only insert $insertableSize of $watermarkSize bytes from the Watermark into " +
                "the text file."
    }

    class ContainsAlphabetCharsError(val chars: Sequence<Char>) :
        Event.Error("$SOURCE.addWatermark") {
        /** Returns a String explaining the event */
        override fun getMessage(): String {
            val containedChars =
                chars.joinToString(prefix = "[", separator = ",", postfix = "]") { char ->
                    "'${char.toUnicodeRepresentation()}'"
                }

            return "The file contains characters of the watermark " +
                "transcoding alphabet. Adding a Watermarking would potentially make the " +
                "file unusable! Maybe the file already contains a watermark?\n\n" +
                "Contained Chars:\n" +
                "$containedChars."
        }
    }

    class Success(val startPositions: List<Int>) : Event.Success() {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "Added Watermark ${startPositions.size} times. Positions: $startPositions."
    }

    class RemoveWatermarksGetProblemWarning : Event.Warning("$SOURCE.removeWatermarks") {
        /** Returns a String explaining the event */
        override fun getMessage(): String =
            "There was a problem extracting the watermarks. They got removed anyways."
    }
}

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
class TextWatermarkerBuilder {
    private var transcoding: Transcoding = DefaultTranscoding
    private var separatorStrategy: SeparatorStrategy =
        SeparatorStrategy.SingleSeparatorChar(DefaultTranscoding.SEPARATOR_CHAR)
    private var compression: Boolean = TextWatermark.COMPRESSION_DEFAULT

    /** Yields all positions where a Char of the watermark can be inserted */
    private var placement: (string: String) -> Sequence<Int> = { string ->
        sequence {
            for ((index, char) in string.withIndex()) {
                if (char == ' ') yield(index)
            }
        }
    }

    /** Sets a custom transcoding alphabet */
    fun setTranscoding(transcoding: Transcoding): TextWatermarkerBuilder {
        this.transcoding = transcoding
        return this
    }

    /** Set a custom separator strategy */
    fun setSeparatorStrategy(separatorStrategy: SeparatorStrategy): TextWatermarkerBuilder {
        this.separatorStrategy = separatorStrategy
        return this
    }

    /** Enables text watermark compression */
    fun enableCompression(): TextWatermarkerBuilder {
        compression = true
        return this
    }

    /** Disables text watermark compression */
    fun disableCompression(): TextWatermarkerBuilder {
        compression = false
        return this
    }

    /** Sets a custom placement function used to identify insertion positions */
    fun setPlacement(placement: (String) -> Sequence<Int>): TextWatermarkerBuilder {
        this.placement = placement
        return this
    }

    /** Validates the TextWatermarker configuration */
    private fun validate(): Status {
        val status = Status.success()

        when (val separatorStrategy = separatorStrategy) {
            is SeparatorStrategy.SkipInsertPosition -> {}
            is SeparatorStrategy.SingleSeparatorChar -> {
                if (separatorStrategy.char in transcoding.alphabet) {
                    status.addEvent(AlphabetContainsSeparatorError(listOf(separatorStrategy.char)))
                }
            }

            is SeparatorStrategy.StartEndSeparatorChars -> {
                val list = ArrayList<Char>()
                if (separatorStrategy.start in transcoding.alphabet) {
                    list.add(separatorStrategy.start)
                }
                if (separatorStrategy.start in transcoding.alphabet) {
                    list.add(separatorStrategy.start)
                }
                if (!list.isEmpty()) {
                    status.addEvent(AlphabetContainsSeparatorError(list))
                }
            }
        }

        return status
    }

    /**
     * Creates a TextWatermarker.
     *
     * Fails if transcoding alphabet contains separator chars.
     */
    fun build(): Result<TextWatermarker> {
        val status = validate()
        return if (status.isError) {
            return status.into()
        } else {
            status.into(TextWatermarker(transcoding, separatorStrategy, compression, placement))
        }
    }

    companion object {
        private const val SOURCE = "TextWatermarkerBuilder"
    }

    class AlphabetContainsSeparatorError(val chars: List<Char>) : Event.Error(SOURCE) {
        override fun getMessage(): String {
            val containedChars =
                chars.joinToString(prefix = "[", separator = ",", postfix = "]") { char ->
                    "'${char.toUnicodeRepresentation()}'"
                }
            return "The alphabet contains separator char(s): $containedChars"
        }
    }
}
