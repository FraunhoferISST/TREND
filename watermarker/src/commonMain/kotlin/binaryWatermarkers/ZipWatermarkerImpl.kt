package de.fraunhofer.isst.innamark.watermarker.binaryWatermarkers

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.ZipWatermarker
import de.fraunhofer.isst.innamark.watermarker.files.ZipFile
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark

class ZipWatermarkerImpl : BinaryWatermarker<ZipFile> {
    private val watermarker = ZipWatermarker

    override fun addWatermark(
        cover: ZipFile,
        watermark: String,
    ): Result<ZipFile> {
        val status = watermarker.addWatermark(cover, Watermark.fromString(watermark))
        return status.into(cover)
    }

    override fun addWatermark(
        cover: ZipFile,
        watermark: ByteArray,
    ): Result<ZipFile> {
        val status = watermarker.addWatermark(cover, watermark.toList())
        return status.into(cover)
    }

    override fun addWatermark(
        cover: ZipFile,
        watermark: Watermark,
    ): Result<ZipFile> {
        val status = watermarker.addWatermark(cover, watermark)
        return status.into(cover)
    }

    override fun containsWatermark(cover: ZipFile): Boolean {
        return watermarker.containsWatermark(cover)
    }

    override fun getWatermarks(
        cover: ZipFile,
        squash: Boolean,
        singleWatermark: Boolean,
    ): Result<List<Watermark>> {
        return watermarker.getWatermarks(cover, squash, singleWatermark)
    }

    override fun removeWatermark(cover: ZipFile): Result<ZipFile> {
        val status = watermarker.removeWatermarks(cover).status
        return status.into(cover)
    }
}
