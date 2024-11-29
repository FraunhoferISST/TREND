/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.helper.toIntUnsigned
import de.fraunhofer.isst.trend.watermarker.helper.toUnicodeRepresentation
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.TrendmarkBuilder
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.math.ceil
import kotlin.math.log
import kotlin.math.pow

/** Defines how multiple watermarks are separated */
@JsExport
sealed class SeparatorStrategy {
    /** Leaves one insertable position empty to mark the end of a watermark */
    object SkipInsertPosition : SeparatorStrategy()

    /** Inserts [char] as separator between watermarks */
    class SingleSeparatorChar(val char: Char) : SeparatorStrategy()

    /** Inserts [start] before a Watermark and [end] after a Watermark as separators */
    class StartEndSeparatorChars(val start: Char, val end: Char) : SeparatorStrategy()
}

@JsExport
interface Transcoding {
    val alphabet: List<Char>

    /** Encodes [bytes] to chars of [alphabet] */
    fun encode(bytes: List<Byte>): Sequence<Char>

    /** Decodes [chars] of [alphabet] to bytes */
    fun decode(chars: Sequence<Char>): Result<List<Byte>>
}

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

@JsExport
class TextWatermarker(
    private val transcoding: Transcoding,
    private val separatorStrategy: SeparatorStrategy,
    val placement: (String) -> List<Int>,
) : FileWatermarker<TextFile> {
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
     * Adds a [watermark] to [file].
     *
     * Returns a warning if the watermark does not fit at least a single time into the file.
     * Returns an error if the text file contains a character from the transcoding alphabet.
     */
    override fun addWatermark(
        file: TextFile,
        watermark: List<Byte>,
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

        val result = StringBuilder()
        var lastPosition = 0
        for (positions in positionChunks) {
            startPositions.add(positions.first())
            for ((position, char) in positions.asSequence().zip(separatedWatermark)) {
                result.append(file.content.substring(lastPosition, position))
                result.append(char)
                lastPosition = position + 1
            }
        }
        result.append(file.content.substring(lastPosition, file.content.length))

        file.content = result.toString()

        // Check if watermark fits at least one time into the text file with given positioning
        if (insertPositions.count() < getMinimumInsertPositions(watermark)) {
            return OversizedWatermarkWarning(
                getMinimumInsertPositions(watermark),
                insertPositions.count(),
            ).into()
        }

        return Success(startPositions).into()
    }

    /** Checks if [file] contains any [Char] from full watermarking alphabet */
    override fun containsWatermark(file: TextFile): Boolean =
        file.content.any { char ->
            char in fullAlphabet
        }

    /** Returns all watermarks in [file] */
    override fun getWatermarks(file: TextFile): Result<List<Watermark>> {
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
                            if (lastSeparatorPosition != separatorPosition - 1) {
                                yield(lastSeparatorPosition to separatorPosition - 1)
                            }
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
        val watermarks = ArrayList<Watermark>()
        for ((start, end) in sanitizedWatermarkRanges) {
            val content =
                StringBuilder(file.content)
                    .drop(start)
                    .take(end - start + 1)
                    .filter { char -> char in transcoding.alphabet }

            if (content.count() > 0) {
                val decoded =
                    with(transcoding.decode(content.asSequence())) {
                        if (!hasValue) {
                            return this.status.into()
                        }
                        status.appendStatus(this.status)
                        value!!
                    }

                watermarks.add(Watermark(decoded))
            }
        }

        if (watermarkRanges.count() <= 0 && watermarks.isNotEmpty()) {
            status.addEvent(IncompleteWatermarkWarning())
        }

        return status.into(watermarks)
    }

    /**
     * Removes all watermarks in [file] and returns them.
     *
     * Returns a warning if getWatermarks() returns a warning or error.
     */
    override fun removeWatermarks(file: TextFile): Result<List<Watermark>> {
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
    @JsName("getMinimumInsertPositionsBytes")
    fun getMinimumInsertPositions(watermark: List<Byte>): Int {
        val separatedWatermark = getSeparatedWatermark(watermark)
        return if (separatorStrategy is SeparatorStrategy.StartEndSeparatorChars) {
            separatedWatermark.count()
        }else separatedWatermark.count()+1
    }

    /** Counts the minimum number of insert positions needed in a text to insert the [watermark] */
    fun getMinimumInsertPositions(watermark: Watermark): Int =
        getMinimumInsertPositions(watermark.watermarkContent)

    /**
     * Counts the minimum number of insert positions needed in a text to insert the
     * [trendmarkBuilder]
     */
    @JsName("getMimimumInsertPositionsTrendmarkBuilder")
    fun getMinimumInsertPositions(trendmarkBuilder: TrendmarkBuilder): Int =
        getMinimumInsertPositions(trendmarkBuilder.finish())

    /** Transforms a [watermark] into a separated watermark */
    private fun getSeparatedWatermark(watermark: List<Byte>): Sequence<Char> {
        val encodedWatermark = transcoding.encode(watermark)

        val separatedWatermark =
            when (separatorStrategy) {
                is SeparatorStrategy.SkipInsertPosition -> encodedWatermark
                is SeparatorStrategy.SingleSeparatorChar ->
                    sequence {
                        yield(separatorStrategy.char)
                        yieldAll(encodedWatermark)
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

        /** Returns the builder for TextWatermarker */
        fun builder(): TextWatermarkerBuilder = TextWatermarkerBuilder()

        /** Returns an instance of TextWatermarker in default configuration */
        fun default(): TextWatermarker = TextWatermarkerBuilder().build().value!!
    }

    override fun getSource(): String = SOURCE

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

            return "The input contains characters of the watermark " +
                "transcoding alphabet. It is only possible to add a watermark to and input " +
                "that doesn't contain any watermark. Adding another watermark would potentially " +
                "make the input unusable! Maybe the input already contains a watermark?\n\n" +
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

@JsExport
class TextWatermarkerBuilder {
    private var transcoding: Transcoding = DefaultTranscoding
    private var separatorStrategy: SeparatorStrategy =
        SeparatorStrategy.SingleSeparatorChar(DefaultTranscoding.SEPARATOR_CHAR)

    /** Yields all positions where a Char of the watermark can be inserted */
    private var placement: (string: String) -> List<Int> = { string ->
        sequence {
            for ((index, char) in string.withIndex()) {
                if (char == ' ') yield(index)
            }
        }.toMutableList() // mutable for JS compatibility on empty lists
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

    /** Sets a custom placement function used to identify insertion positions */
    fun setPlacement(placement: (String) -> List<Int>): TextWatermarkerBuilder {
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
                if (separatorStrategy.end in transcoding.alphabet) {
                    list.add(separatorStrategy.end)
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
            status.into(TextWatermarker(transcoding, separatorStrategy, placement))
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
