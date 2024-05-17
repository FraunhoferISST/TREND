/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.files

import areFilesEqual
import de.fraunhofer.isst.trend.watermarker.files.TextFile
import de.fraunhofer.isst.trend.watermarker.files.WatermarkableFile
import de.fraunhofer.isst.trend.watermarker.files.fromFile
import de.fraunhofer.isst.trend.watermarker.files.writeToFile
import openTextFile
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextFileTestJvm {
    private val loremIpsum =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
            "volutpat blandit aliquam etiam erat velit."

    @Test
    fun fromFile_validPath_success() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val expected =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
                "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
                "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd " +
                "gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem " +
                "ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
                "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
                "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd " +
                "gubergren, no sea takimata sanctus est Lorem ipsum\n"

        // Act
        val result = TextFile.fromFile(source)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value?.content)
    }

    @Test
    fun fromFile_invalidPath_error() {
        // Arrange
        val source = ""
        val expectedMessage =
            WatermarkableFile.ReadError(source, " (No such file or directory)")
                .into().toString()

        // Act
        val result = TextFile.fromFile(source)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
        assertNull(result.value)
    }

    @Test
    fun writeToFile_validPath_success() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val target = "src/jvmTest/resources/lorem_ipsum_test.txt"
        val textFile = openTextFile(source)

        // Act
        val result = textFile.writeToFile(target)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(areFilesEqual(source, target))

        // Cleanup
        File(target).delete()
    }

    @Test
    fun writeToFile_invalidPath_error() {
        // Arrange
        val target = ""
        val expectedMessage =
            WatermarkableFile.WriteError(target, " (No such file or directory)").into().toString()
        val textFile = TextFile.fromString(loremIpsum)

        // Act
        val result = textFile.writeToFile(target)

        // Assert
        assertTrue(result.isError)
        assertEquals(expectedMessage, result.toString())
    }

    @Test
    fun equals_sameFile_true() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val file1 = openTextFile(source)
        val file2 = openTextFile(source)

        // Act
        val result = file1 == file2

        // Assert
        assertTrue(result)
    }

    @Test
    fun equals_differentTxtFiles_false() {
        // Arrange
        val source1 = "src/jvmTest/resources/lorem_ipsum.txt"
        val source2 = "src/jvmTest/resources/lorem_ipsum_watermarked.txt"
        val file1 = openTextFile(source1)
        val file2 = openTextFile(source2)

        // Act
        val result = file1 == file2

        // Assert
        assertFalse(result)
    }

    @Test
    fun equals_differentObject_false() {
        // Arrange
        val source = "src/jvmTest/resources/lorem_ipsum.txt"
        val file = openTextFile(source)
        val obj = 0

        // Act
        val result = file.equals(obj)

        // Assert
        assertFalse(result)
    }
}
