package de.fraunhofer.isst.innamark.watermarker.textWatermarkers

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.SeparatorStrategy
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.Transcoding
import de.fraunhofer.isst.innamark.watermarker.files.TextFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

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

    // override functions
    override fun addWatermark(
        cover: String,
        watermark: ByteArray,
    ): Result<String> {
        val textFile = TextFile.fromString(cover)
        val status = watermarker.addWatermark(textFile, watermark.toList())
        return status.into(textFile.content)
    }

    override fun addWatermark(
        cover: String,
        watermark: String,
    ): Result<String> {
        return addWatermark(cover, watermark.encodeToByteArray())
    }

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
