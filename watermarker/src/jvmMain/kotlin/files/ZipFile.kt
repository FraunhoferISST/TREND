/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.files

import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import java.io.File

/**
 * Reads the file [path] and parses it as .zip-file Parsing includes
 * separating the header from the content and parsing the header
 *
 * Returns errors if it cannot parse the file.
 * Returns warnings if the parser find unexpected structure but is still able to parse it
 */
fun ZipFile.Companion.fromFile(path: String): Result<ZipFile> {
    val bytes =
        try {
            File(path).readBytes()
        } catch (e: Exception) {
            return WatermarkableFile.ReadError(path, e.message ?: e.stackTraceToString()).into<_>()
        }
    return fromBytes(bytes, path)
}
