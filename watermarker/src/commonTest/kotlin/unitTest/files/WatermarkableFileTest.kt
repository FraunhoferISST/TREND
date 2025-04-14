/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.files

import de.fraunhofer.isst.innamark.watermarker.files.WatermarkableFile
import kotlin.test.Test

class WatermarkableFileTest {
    @Test
    fun readError_string_success() {
        // Arrange
        val error = WatermarkableFile.ReadError("/some/path", "some reason")
        val expected = "Error (File.read): some reason"

        // Act
        val result = error.toString()

        // Assert
        kotlin.test.assertEquals(expected, result)
    }

    @Test
    fun writeError_string_success() {
        // Arrange
        val error = WatermarkableFile.WriteError("/some/path", "some reason")
        val expected = "Error (File.write): some reason"

        // Act
        val result = error.toString()

        // Assert
        kotlin.test.assertEquals(expected, result)
    }
}
