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
import de.fraunhofer.isst.trend.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import de.fraunhofer.isst.trend.watermarker.watermarks.TrendmarkBuilder
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.toTextWatermarks
import de.fraunhofer.isst.trend.watermarker.watermarks.toTrendmarks
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
     * Adds a [trendmarkBuilder] to [source] and writes changes to [target].
     *
     * When [fileType] is null the type is taken from [source]'s extension.
     */
    fun addWatermark(
        source: String,
        target: String,
        trendmarkBuilder: TrendmarkBuilder,
        fileType: String? = null,
    ): Status {
        return addWatermark(source, target, trendmarkBuilder.finish(), fileType)
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

    private fun <T : WatermarkableFile> getWatermarksDoWork(
        watermarker: FileWatermarker<T>,
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
     * Returns all watermarks in [source] as Trendmark.
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
        return getWatermarks(source, fileType, squash).toTrendmarks(SOURCE)
    }

    /**
     * Returns all watermarks in [source] as TextWatermarks.
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
     * Returns a warning if some Trendmarks could not be converted to TextWatermarks.
     * Returns an error if no Trendmark could be converted to a TextWatermark.
     */
    fun getTextWatermarks(
        source: String,
        fileType: String? = null,
        squash: Boolean = true,
        errorOnInvalidUTF8: Boolean = false,
    ): Result<List<TextWatermark>> {
        return getWatermarks(source, fileType, squash).toTextWatermarks(errorOnInvalidUTF8, SOURCE)
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

    private fun <T : WatermarkableFile> removeWatermarksDoWork(
        watermarker: FileWatermarker<T>,
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
