/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest

import areFilesEqual
import openTextFile
import openZipFile
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HelperFunctionsTestJvm {
    @Test
    fun areFilesEqual_sameContent_true() {
        // Arrange
        val file1 = "src/jvmTest/resources/lorem_ipsum.txt"
        val file2 = "src/jvmTest/resources/lorem_ipsum.unsupported"

        // Act
        val result = areFilesEqual(file1, file2)

        // Assert
        assertTrue(result)
    }

    @Test
    fun areFilesEqual_differentContent_false() {
        // Arrange
        val file1 = "src/jvmTest/resources/lorem_ipsum.txt"
        val file2 = "src/jvmTest/resources/lorem_ipsum_watermarked.txt"

        // Act
        val result = areFilesEqual(file1, file2)

        // Assert
        assertFalse(result)
    }

    @Test
    fun areFilesEqual_invalidPath_error() {
        // Arrange
        val file1 = "src/jvmTest/resources/lorem_ipsum.txt"
        val file2 = ""

        // Act + Assert
        assertFailsWith<NoSuchFileException> { areFilesEqual(file1, file2) }
    }

    @Test
    fun openZipFile_validPath_success() {
        // Arrange
        val file = "src/jvmTest/resources/multiple_files.zip"

        // Act
        openZipFile(file)
    }

    @Test
    fun openZipFile_invalidPath_error() {
        // Arrange
        val file = ""

        // Act + Assert
        assertFailsWith<AssertionError> { openZipFile(file) }
    }

    @Test
    fun openZipFile_invalidFile_error() {
        // Arrange
        val file = "src/jvmTest/resources/lorem_ipsum.txt"

        // Act + Assert
        assertFailsWith<AssertionError> { openZipFile(file) }
    }

    @Test
    fun openTextFile_validPath_success() {
        // Arrange
        val file = "src/jvmTest/resources/lorem_ipsum.txt"

        // Act
        openTextFile(file)
    }

    @Test
    fun openTextFile_invalidPath_error() {
        // Arrange
        val file = ""

        // Act + Assert
        assertFailsWith<AssertionError> { openTextFile(file) }
    }

    @Test
    fun openTextFile_invalidFile_error() {
        // Arrange
        val file = "src/jvmTest/resources/multiple_files.zip"

        // Act + Assert
        assertFailsWith<AssertionError> { openTextFile(file) }
    }
}
