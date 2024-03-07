/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.files

import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import java.io.File

/** Writes the text file from memory into the file [path] */
fun WatermarkableFile.writeToFile(path: String): Status {
    try {
        val file = File(path)
        file.writeBytes(this.toBytes().toByteArray())
    } catch (e: Exception) {
        return WatermarkableFile.WriteError(path, e.message ?: e.stackTraceToString()).into()
    }
    return Status.success()
}
