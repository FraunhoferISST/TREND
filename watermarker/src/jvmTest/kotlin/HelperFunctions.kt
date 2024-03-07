/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.files.ZipFile
import de.fraunhofer.isst.trend.watermarker.files.fromFile
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Compares [file1] with [file2] and returns true if they are equal */
fun areFilesEqual(
    file1: String,
    file2: String,
): Boolean {
    val fileA = File(file1)
    val fileB = File(file2)

    if (!fileA.exists()) {
        throw NoSuchFileException(fileA)
    }

    if (!fileB.exists()) {
        throw NoSuchFileException(fileB)
    }

    if (fileA.length() != fileB.length()) return false

    return fileA.readBytes().contentEquals(fileB.readBytes())
}

/** Opens a zip file and expecting it to work */
fun openZipFile(path: String): ZipFile {
    val tryFile = ZipFile.fromFile(path)
    assertTrue(tryFile.isSuccess)
    assertNotNull(tryFile.value)
    return tryFile.value!!
}

/** Opens a text file and expecting it to work */
fun openTextFile(path: String): TextFile {
    val tryFile = TextFile.fromFile(path)
    assertTrue(tryFile.isSuccess)
    assertNotNull(tryFile.value)
    return tryFile.value!!
}
