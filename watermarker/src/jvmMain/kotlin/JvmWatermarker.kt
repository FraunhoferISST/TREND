/*
 * Copyright (c) 2023-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

package de.fraunhofer.isst.innamark.watermarker

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.FileWatermarker
import de.fraunhofer.isst.innamark.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.innamark.watermarker.files.writeToFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTag
import de.fraunhofer.isst.innamark.watermarker.watermarks.InnamarkTagBuilder
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.toInnamarkTags
import de.fraunhofer.isst.innamark.watermarker.watermarks.toTextWatermarks
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.extension

/**
 * Parses the file type from [fileType] or if it is null from [path]'s extension.
 * Returns an error if the file type cannot be determined.
 */
fun SupportedFileType.Companion.getFileType(
    path: String,
    fileType: String?,
): Result<SupportedFileType> {
    val parsedFileType =
        if (fileType != null) {
            fileType
        } else {
            val parsedPath = Path(path)
            if (parsedPath.extension == "") return SupportedFileType.NoFileTypeError(path).into<_>()
            parsedPath.extension
        }

    return fromExtension(parsedFileType)
}

fun readFile(path: String): Result<ByteArray> {
    return try {
        Result.success(File(path).readBytes())
    } catch (e: Exception) {
        WatermarkableFile.ReadError(path, e.message ?: e.stackTraceToString()).into<_>()
    }
}

class JvmWatermarker : Watermarker() {
    companion object {
        const val SOURCE = "JvmWatermarker"
    }

    /**
     * Adds a [watermark] to [source] and writes changes to [target].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun addWatermark(
        source: String,
        target: String,
        watermark: Watermark,
        fileType: String? = null,
    ): Status {
        val supportedFileType =
            with(SupportedFileType.getFileType(source, fileType)) {
                value ?: return into()
            }

        return addWatermarkDoWork(supportedFileType.watermarker, source, target, watermark)
    }

    /**
     * Adds a [innamarkTagBuilder] to [source] and writes changes to [target].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun addWatermark(
        source: String,
        target: String,
        innamarkTagBuilder: InnamarkTagBuilder,
        fileType: String? = null,
    ): Status {
        return addWatermark(source, target, innamarkTagBuilder.finish(), fileType)
    }

    private fun <T : WatermarkableFile> addWatermarkDoWork(
        watermarker: FileWatermarker<T>,
        source: String,
        target: String,
        watermark: Watermark,
    ): Status {
        val (status, bytes) =
            with(readFile(source)) {
                status to (value?.asList() ?: return into())
            }

        val file =
            with(watermarker.parseBytes(bytes)) {
                status.appendStatus(this.status)
                value ?: return status
            }

        status.appendStatus(watermarker.addWatermark(file, watermark))

        if (!status.isError) {
            status.appendStatus(file.writeToFile(target))
        }

        return status
    }

    /**
     * Checks if [source] contains watermarks.
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun containsWatermark(
        source: String,
        fileType: String? = null,
    ): Result<Boolean> {
        val supportedFileType =
            with(SupportedFileType.getFileType(source, fileType)) {
                value ?: return into<_>()
            }

        return containsWatermarkDoWork(supportedFileType.watermarker, source)
    }

    private fun <T : WatermarkableFile> containsWatermarkDoWork(
        watermarker: FileWatermarker<T>,
        source: String,
    ): Result<Boolean> {
        val (status, bytes) =
            with(readFile(source)) {
                status to (value?.asList() ?: return into<_>())
            }

        val file =
            with(watermarker.parseBytes(bytes)) {
                status.appendStatus(this.status)
                value ?: return status.into()
            }

        val contains = watermarker.containsWatermark(file)
        return status.into(contains)
    }

    /**
     * Returns all watermarks in [source].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     */
    fun getWatermarks(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
    ): Result<List<Watermark>> {
        val supportedFileType =
            with(SupportedFileType.getFileType(source, fileType)) {
                value ?: return into<_>()
            }

        return getWatermarksDoWork(supportedFileType.watermarker, source, squash, singleWatermark)
    }

    private fun <T : WatermarkableFile> getWatermarksDoWork(
        watermarker: FileWatermarker<T>,
        source: String,
        squash: Boolean,
        singleWatermark: Boolean = false,
    ): Result<List<Watermark>> {
        val (status, bytes) =
            with(readFile(source)) {
                status to (value?.asList() ?: return into<_>())
            }

        val file =
            with(watermarker.parseBytes(bytes)) {
                status.appendStatus(this.status)
                value ?: return status.into()
            }

        var watermarks =
            with(watermarker.getWatermarks(file)) {
                status.appendStatus(this.status)
                value
            }

        if (singleWatermark && bytes.isNotEmpty()) {
            val mostFrequent = Watermark.mostFrequent(watermarks ?: emptyList())
            // append Status in case of warning/error
            status.appendStatus(mostFrequent.status)
            // replace List in result.value
            watermarks = mostFrequent.value
        }

        if (squash && watermarks != null) {
            watermarks = squashWatermarks(watermarks)
        }

        return status.into(watermarks)
    }

    /**
     * Returns all watermarks in [source] as InnamarkTag.
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     * When [validateAll] is true: All resulting InnamarkTags are validated to check for errors.
     *
     * Returns a warning if some watermarks could not be converted to InnamarksTag.
     * Returns an error if no watermark could be converted to a InnamarkTag.
     */
    fun getInnamarkTags(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
        validateAll: Boolean = true,
    ): Result<List<InnamarkTag>> {
        val result = getWatermarks(source, fileType, squash, singleWatermark).toInnamarkTags(SOURCE)

        if (validateAll && result.hasValue && result.value!!.isNotEmpty()) {
            for (innamarkTag in result.value) {
                val validationStatus = innamarkTag.validate()
                result.appendStatus(validationStatus)
            }
        }

        return result
    }

    /**
     * Returns all watermarks in [source] as TextWatermarks.
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent  watermark is returned.
     *
     * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error
     *                           is false: invalid bytes sequences are replace with the char �
     *
     * Returns a warning if some watermarks could not be converted to InnamarksTag.
     * Returns an error if no watermark could be converted to a InnamarkTag.
     *
     * Returns a warning if some InnamarkTags could not be converted to TextWatermarks.
     * Returns an error if no InnamarkTag could be converted to a TextWatermark.
     */
    fun getTextWatermarks(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
        errorOnInvalidUTF8: Boolean = false,
    ): Result<List<TextWatermark>> {
        return getWatermarks(source, fileType, squash, singleWatermark).toTextWatermarks(
            errorOnInvalidUTF8,
            SOURCE,
        )
    }

    /**
     * Removes all watermarks in [source] and returns them.
     * When [squash] is true: watermarks with the same content are merged.
     * When [singleWatermark] is true: only the most frequent watermark is returned.
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun removeWatermarks(
        source: String,
        target: String,
        fileType: String? = null,
        squash: Boolean = true,
        singleWatermark: Boolean = true,
    ): Result<List<Watermark>> {
        val supportedFileType =
            with(SupportedFileType.getFileType(source, fileType)) {
                value ?: return into<_>()
            }

        return removeWatermarksDoWork(
            supportedFileType.watermarker,
            source,
            target,
            squash,
            singleWatermark,
        )
    }

    private fun <T : WatermarkableFile> removeWatermarksDoWork(
        watermarker: FileWatermarker<T>,
        source: String,
        target: String,
        squash: Boolean,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        val (status, bytes) =
            with(readFile(source)) {
                status to (value?.asList() ?: return into<_>())
            }

        val file =
            with(watermarker.parseBytes(bytes)) {
                status.appendStatus(this.status)
                value ?: return status.into()
            }

        val watermarks =
            with(watermarker.removeWatermarks(file, squash, singleWatermark)) {
                status.appendStatus(this.status)
                value
            }

        if (!status.isError) {
            status.appendStatus(file.writeToFile(target))
        }

        return status.into(watermarks)
    }
}
