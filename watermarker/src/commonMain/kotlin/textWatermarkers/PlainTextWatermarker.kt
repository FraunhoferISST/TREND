/*
 * Copyright (c) 2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.textWatermarkers

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.SeparatorStrategy
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.Transcoding
import de.fraunhofer.isst.innamark.watermarker.files.TextFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Status
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

/**
 * Implementation of [de.fraunhofer.isst.innamark.watermarker.textWatermarkers.TextWatermarker] for watermarking plaintext
 *
 * Takes optional arguments to customize behavior:
 * - [transcoding]: for defining the watermarking alphabet, encoding, and decoding.
 * - [separatorStrategy]: for defining how multiple watermarks are separated.
 * - [placement]: function for finding positions where transcoding alphabet characters are inserted.
 */
class PlainTextWatermarker(
    private val transcoding: Transcoding = DefaultTranscoding,
    private val separatorStrategy: SeparatorStrategy =
        SeparatorStrategy.SingleSeparatorChar(DefaultTranscoding.SEPARATOR_CHAR),
    private val placement: (String) -> List<Int> = { string ->
        sequence {
            for ((index, char) in string.withIndex()) {
                if (char == ' ') yield(index)
            }
        }.toMutableList() // mutable for JS compatibility on empty lists
    },
) : de.fraunhofer.isst.innamark.watermarker.textWatermarkers.TextWatermarker {
    // create instance of (old) TextWatermarker with provided parameters (or defaults)
    private val watermarker = TextWatermarker(transcoding, separatorStrategy, placement)

    /**
     * Adds a watermark created from [watermark] String to [cover]
     *
     * Returns a warning if the watermark does not fit at least a single time into the file.
     * Returns an error if the text file contains a character from the transcoding alphabet.
     */
    override fun addWatermark(
        cover: String,
        watermark: ByteArray,
    ): Result<String> {
        val textFile = TextFile.fromString(cover)
        val status = watermarker.addWatermark(textFile, watermark)
        return status.into(textFile.content)
    }

    /**
     * Adds a watermark created from [watermark] ByteArray to [cover]
     *
     * Returns a warning if the watermark does not fit at least a single time into the file.
     * Returns an error if the text file contains a character from the transcoding alphabet.
     */
    override fun addWatermark(
        cover: String,
        watermark: String,
    ): Result<String> {
        return addWatermark(cover, watermark.encodeToByteArray())
    }

    /**
     * Adds watermark object [watermark] to [cover]
     *
     * Returns a warning if the watermark does not fit at least a single time into the file.
     * Returns an error if the text file contains a character from the transcoding alphabet.
     */
    override fun addWatermark(
        cover: String,
        watermark: Watermark,
    ): Result<String> {
        val textFile = TextFile.fromString(cover)
        val status = watermarker.addWatermark(textFile, watermark)
        return status.into(textFile.content)
    }

    override fun containsWatermark(cover: String): Boolean {
        return watermarker.containsWatermark(TextFile.fromString(cover))
    }

    override fun getWatermarkAsString(cover: String): Result<String> {
        val watermarks = getWatermarks(cover, false, true)
        if (watermarks.value?.isNotEmpty() ?: return Result.success("")) {
            val decoded =
                watermarks.status.into(
                    watermarks.value[0].watermarkContent
                        .decodeToString(),
                )
            if (decoded.value!!.contains('\uFFFD')) {
                decoded.appendStatus(Status(Watermark.StringDecodeWarning("PlainTextWatermarker")))
            }
            return decoded
        } else {
            return Result.success("")
        }
    }

    override fun getWatermarkAsByteArray(cover: String): Result<ByteArray> {
        val watermarks = getWatermarks(cover, false, true)
        if (watermarks.value?.isNotEmpty() ?: return Result.success(ByteArray(0))) {
            return watermarks.status.into(watermarks.value[0].watermarkContent)
        } else {
            return Result.success(ByteArray(0))
        }
    }

    override fun getWatermarks(
        cover: String,
        squash: Boolean,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        return watermarker.getWatermarks(TextFile.fromString(cover), squash, singleWatermark)
    }

    override fun removeWatermarks(cover: String): Result<String> {
        val textFile = TextFile.fromString(cover)
        val result = watermarker.removeWatermarks(textFile)
        return result.status.into(textFile.content)
    }
}
