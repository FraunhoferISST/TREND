/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.files

import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

@JsExport
class TextFile private constructor(path: String?, var content: String) : WatermarkableFile(path) {
    /** Converts the TextFile into raw bytes */
    override fun toBytes(): List<Byte> {
        return this.content.encodeToByteArray().asList()
    }

    companion object {
        internal val source = TextFile::class.simpleName!!

        /** Creates a TextFile with the text parsed from [content] */
        @JvmStatic
        fun fromBytes(
            bytes: ByteArray,
            path: String? = null,
        ): Result<TextFile> {
            val text =
                try {
                    bytes.decodeToString(throwOnInvalidSequence = true)
                } catch (e: CharacterCodingException) {
                    // TODO: Check available information
                    return InvalidByteError().into<_>()
                }

            val textFile = TextFile(path, text)
            return Result.success(textFile)
        }

        /** Creates a TextFile with text [text] */
        @JvmStatic
        fun fromString(text: String): TextFile {
            return TextFile(null, text)
        }
    }

    /** Checks if [this] and [other] are equal */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is TextFile -> this.content == other.content
            else -> false
        }
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    class InvalidByteError : Event.Error(source) {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Cannot parse text file: File contains invalid byte(s)."
    }
}
