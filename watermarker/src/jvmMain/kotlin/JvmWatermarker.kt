/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

package de.fraunhofer.isst.trend.watermarker

import de.fraunhofer.isst.trend.watermarker.fileWatermarker.FileWatermarker
import de.fraunhofer.isst.trend.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.trend.watermarker.files.writeToFile
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import de.fraunhofer.isst.trend.watermarker.watermarks.Textmark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
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
     * Adds a [textmark] to [source] and writes changes to [target].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun addTextmark(
        source: String,
        target: String,
        textmark: Textmark,
        fileType: String? = null,
    ): Status {
        return addWatermark(source, target, textmark.finish(), fileType)
    }

    private fun <T : WatermarkableFile, U : Watermark> addWatermarkDoWork(
        watermarker: FileWatermarker<T, U>,
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

    private fun <T : WatermarkableFile, U : Watermark> containsWatermarkDoWork(
        watermarker: FileWatermarker<T, U>,
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
     */
    fun getWatermarks(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
    ): Result<List<Watermark>> {
        val supportedFileType =
            with(SupportedFileType.getFileType(source, fileType)) {
                value ?: return into<_>()
            }

        return getWatermarksDoWork(supportedFileType.watermarker, source, squash)
    }

    private fun <T : WatermarkableFile, U : Watermark> getWatermarksDoWork(
        watermarker: FileWatermarker<T, U>,
        source: String,
        squash: Boolean,
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

        if (squash && watermarks != null) {
            watermarks = squashWatermarks(watermarks)
        }

        return status.into(watermarks)
    }

    /**
     * Returns all Trendmarks in [source].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     * When [squash] is true: watermarks with the same content are merged.
     *
     * Returns a warning if some watermarks could not be converted to Trendmarks.
     * Returns an error if no watermark could be converted to a Trendmark.
     */
    fun getTrendmarks(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
    ): Result<List<Trendmark>> {
        val (watermarks, status) =
            with(getWatermarks(source, fileType, squash)) {
                if (value == null) {
                    return status.into()
                }
                value to status
            }

        val trendmarks =
            watermarks.mapNotNull { watermark ->
                val trendmark = Trendmark.fromWatermark(watermark)
                status.appendStatus(trendmark.status)
                trendmark.value
            }

        if (status.isError && trendmarks.isNotEmpty()) {
            status.addEvent(
                FailedTrendmarkExtractionsWarning("$SOURCE.getTrendmarks"),
                overrideSeverity = true,
            )
        }

        return if (status.isError) {
            status.into()
        } else {
            status.into(trendmarks)
        }
    }

    /**
     * Returns all Textmarks in [source].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     * When [squash] is true: watermarks with the same content are merged.
     *
     * When [errorOnInvalidUTF8] is true: invalid bytes sequences cause an error
     *                           is false: invalid bytes sequences are replace with the char �
     *
     * Returns a warning if some watermarks could not be converted to Trendmarks.
     * Returns an error if no watermark could be converted to a Trendmark.
     *
     * Returns a warning if some Trendmarks could not be converted to Textmarks.
     * Returns an error if no Trendmark could be converted to a Textmark.
     */
    fun getTextmarks(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
        errorOnInvalidUTF8: Boolean = false,
    ): Result<List<Textmark>> {
        val (trendmarks, status) =
            with(getTrendmarks(source, fileType, squash)) {
                if (value == null) {
                    return status.into()
                }
                value to status
            }

        val textmarks =
            trendmarks.mapNotNull { trendmark ->
                val textmark = Textmark.fromTrendmark(trendmark, errorOnInvalidUTF8)
                status.appendStatus(textmark.status)
                textmark.value
            }

        if (status.isError && textmarks.isNotEmpty()) {
            status.addEvent(
                FailedTextmarkExtractionsWarning("$SOURCE.getTextmarks"),
                overrideSeverity = true,
            )
        }

        return if (status.isError) {
            status.into()
        } else {
            status.into(textmarks)
        }
    }

    /**
     * Removes all watermarks in [source] and returns them.
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun removeWatermarks(
        source: String,
        target: String,
        fileType: String? = null,
    ): Result<List<Watermark>> {
        val supportedFileType =
            with(SupportedFileType.getFileType(source, fileType)) {
                value ?: return into<_>()
            }

        return removeWatermarksDoWork(supportedFileType.watermarker, source, target)
    }

    private fun <T : WatermarkableFile, U : Watermark> removeWatermarksDoWork(
        watermarker: FileWatermarker<T, U>,
        source: String,
        target: String,
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
            with(watermarker.removeWatermarks(file)) {
                status.appendStatus(this.status)
                value
            }

        if (!status.isError) {
            status.appendStatus(file.writeToFile(target))
        }

        return status.into(watermarks)
    }
}
