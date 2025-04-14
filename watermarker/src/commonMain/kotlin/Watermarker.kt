/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.FileWatermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.ZipWatermarker
import de.fraunhofer.isst.innamark.watermarker.files.TextFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Event
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Innamark
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkBuilder
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.toInnamarks
import de.fraunhofer.isst.innamark.watermarker.watermarks.toTextWatermarks
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

@JsExport
sealed class SupportedFileType {
    abstract val watermarker: FileWatermarker<*>

    object Text : SupportedFileType() {
        override var watermarker: TextWatermarker = TextWatermarker.default()
    }

    object Zip : SupportedFileType() {
        override var watermarker: ZipWatermarker = ZipWatermarker
    }

    companion object {
        private val extensionMap =
            mutableMapOf(
                "zip" to Zip,
                "jar" to Zip,
                "txt" to Text,
                "md" to Text,
            )

        /*
         * TODO: Write test when builder pattern is implemented for
         *  TextWatermarker / ZipWatermarker
         */

        /** Returns a variant of SupportedFileType if [extension] is supported */
        @JvmStatic
        fun fromExtension(extension: String): Result<SupportedFileType> {
            val fileType = extensionMap[extension]
            return if (fileType == null) {
                UnsupportedTypeError(extension).into<_>()
            } else {
                Result.success(fileType)
            }
        }

        /** Registers an [extension] to a variant of SupportedFileType */
        @JvmStatic
        fun registerExtension(
            extension: String,
            fileType: SupportedFileType,
        ) {
            extensionMap[extension] = fileType
        }

        /** Registers [watermarker] for zip files */
        @JvmStatic
        fun registerZipWatermarker(watermarker: ZipWatermarker) {
            Zip.watermarker = watermarker
        }

        /** Registers [watermarker] for TextWatermarker */
        @JvmStatic
        fun registerTextWatermarker(watermarker: TextWatermarker) {
            Text.watermarker = watermarker
        }

        const val SOURCE: String = "SupportedFileType"
    }

    class NoFileTypeError(val path: String) : Event.Error(SOURCE) {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Could not determine file type of $path!"
    }

    class UnsupportedTypeError(val type: String) : Event.Error(SOURCE) {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Unsupported file type: $type!"
    }
}

@JsExport
open class Watermarker {
    companion object {
        const val SOURCE = "Watermarker"
    }

    private val textWatermarker: TextWatermarker = TextWatermarker.default()

    /** Watermarks string [text] with [watermark] */
    @JsName("textAddWatermarkBytes")
    fun textAddWatermark(
        text: String,
        watermark: List<Byte>,
    ): Result<String> {
        val watermarker = textWatermarker

        val textFile = TextFile.fromString(text)

        val status = watermarker.addWatermark(textFile, watermark)

        return if (status.isError) {
            status.into()
        } else {
            status.into(textFile.content)
        }
    }

    /** Watermarks string [text] with [watermark] */
    fun textAddWatermark(
        text: String,
        watermark: Watermark,
    ): Result<String> {
        val watermarkBytes = watermark.watermarkContent
        return textAddWatermark(text, watermarkBytes)
    }

    /** Watermarks string [text] with [innamarkBuilder] */
    @JsName("textAddInnamarkBuilder")
    fun textAddWatermark(
        text: String,
        innamarkBuilder: InnamarkBuilder,
    ): Result<String> {
        return textAddWatermark(text, innamarkBuilder.finish())
    }

    /** Checks if [text] contains a watermark */
    fun textContainsWatermark(text: String): Boolean {
        val watermarker = textWatermarker

        val textFile = TextFile.fromString(text)

        return watermarker.containsWatermark(textFile)
    }

    /**
     * Returns all watermarks in [text].
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    fun textGetWatermarks(
        text: String,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
    ): Result<List<Watermark>> {
        val watermarker = textWatermarker

        val textFile = TextFile.fromString(text)
        var result = watermarker.getWatermarks(textFile)

        if (singleWatermark && result.hasValue) {
            val mostFrequent = Watermark.mostFrequent(result.value!!)
            // append Status in case of warning/error
            result.appendStatus(mostFrequent.status)
            // replace List in result.value
            result = result.into(mostFrequent.value)
        }
        if (squash && result.hasValue) {
            return result.into(squashWatermarks(result.value!!))
        }

        return result
    }

    /**
     * Returns all watermarks in [text] as Innamarks.
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     * When [validateAll] is true: All resulting Innamarks are validated to check for errors.
     *
     * Returns a warning if some watermarks could not be converted to Innamarks.
     * Returns an error if no watermark could be converted to a Innamark.
     */
    fun textGetInnamarks(
        text: String,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
        validateAll: Boolean = true,
    ): Result<List<Innamark>> {
        val result =
            textGetWatermarks(text, squash, singleWatermark)
                .toInnamarks("$SOURCE.textGetInnamarks")
        if (validateAll && result.hasValue && result.value!!.isNotEmpty()) {
            for (innamark in result.value) {
                val validationStatus = innamark.validate()
                result.appendStatus(validationStatus)
            }
        }
        return result
    }

    /**
     * Returns all watermarks in [text] as TextWatermarks.
     *
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error.
     *                           is false: invalid bytes sequences are replace with the char �.
     *
     * Returns a warning if some watermarks could not be converted to Innamarks.
     * Returns an error if no watermark could be converted to a Innamark.
     *
     * Returns a warning if some Innamarks could not be converted to TextWatermarks.
     * Returns an error if no Innamark could be converted to a TextWatermark.
     */
    fun textGetTextWatermarks(
        text: String,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
        errorOnInvalidUTF8: Boolean = false,
    ): Result<List<TextWatermark>> =
        textGetWatermarks(text, squash, singleWatermark)
            .toTextWatermarks(errorOnInvalidUTF8, "$SOURCE.textGetTextWatermarks")

    /** Returns [text] without watermarks */
    fun textRemoveWatermarks(text: String): Result<String> {
        val watermarker = textWatermarker

        val textFile = TextFile.fromString(text)

        val status = watermarker.removeWatermarks(textFile).status

        return status.into(textFile.content)
    }
}

/** Returns [watermarks] without duplicates */
fun <T : Watermark> squashWatermarks(watermarks: List<T>): List<T> {
    return watermarks.toSet().toList()
}
