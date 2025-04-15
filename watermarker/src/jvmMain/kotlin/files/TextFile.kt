/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.innamark.watermarker.files

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import java.io.File

/**
 * Parses the file [path] as TextFile
 *
 * Returns an error if:
 *  - Unable to read the file [path]
 *  - The file [path] contains invalid UTF-8 bytes
 */
fun TextFile.Companion.fromFile(path: String): Result<TextFile> {
    val bytes =
        try {
            File(path).readBytes()
        } catch (e: Exception) {
            return WatermarkableFile.ReadError(path, e.message ?: e.stackTraceToString()).into<_>()
        }
    return fromBytes(bytes, path)
}
