/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.helper

import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

actual object Compression {
    /** Compresses [data] using the deflate algorithm */
    actual fun inflate(data: List<Byte>): Result<List<Byte>> {
        val input = data.toByteArray()
        val outputStream = ByteArrayOutputStream()

        val inflater = Inflater(true)
        inflater.setInput(input)

        val buffer = ByteArray(1024)
        var count = -1
        try {
            while (count != 0) {
                count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
        } catch (e: Exception) {
            return InflationError(e.message ?: e.stackTraceToString()).into<_>()
        }
        inflater.end()

        return Result.success(outputStream.toByteArray().asList())
    }

    /** Uncompresses [data] using the inflate algorithm */
    actual fun deflate(data: List<Byte>): List<Byte> {
        val input = data.toByteArray()

        // Compress the bytes - 1 to 4 bytes/char for UTF-8
        val deflater =
            Deflater(COMPRESSION_LEVEL, true).apply {
                setInput(input)
                finish()
            }
        val output = ByteArray(input.size * 4)
        val compressedLength = deflater.deflate(output)
        return output.copyOfRange(0, compressedLength).asList()
    }
}
