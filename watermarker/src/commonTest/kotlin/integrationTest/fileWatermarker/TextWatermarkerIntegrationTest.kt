/*
 * Copyright (c) 2024-2025 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package integrationTest.fileWatermarker

import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.files.TextFile
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextWatermarkerIntegrationTest {
    private val textWatermarker = TextWatermarker.default()

    @Test
    fun watermarkInsertionExtraction_successiveSpaces_equality() {
        // Arrange
        val text =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Tempus imperdiet nulla malesuada " +
                "pellentesque elit. Pellentesque id nibh tortor id aliquet lectus proin nibh nisl" +
                ". Viverra accumsan in  nisl  nisi.  Condimentum  lacinia quis vel eros donec. " +
                "Urna neque viverra justo"
        val watermark = "Hello World"
        val textFile = TextFile.fromString(text)
        val parsedWatermark = Watermark.fromString(watermark)

        // Add watermark
        // Act
        val status = textWatermarker.addWatermark(textFile, parsedWatermark)

        // Assert
        assertTrue(status.isSuccess)

        // Extract Watermark
        // Act
        val watermarks = textWatermarker.getWatermarks(textFile)

        // Assert
        assertTrue(watermarks.isSuccess)
        assertEquals(watermarks.value!!.size, 1)
        val firstWatermark = watermarks.value!![0].watermarkContent.decodeToString()
        assertEquals(watermark, firstWatermark)
    }

    @Test
    fun watermarkInsertionExtraction_loremIpsum_equality() {
        // Arrange
        val text =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
                "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu " +
                "fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
                "culpa qui officia deserunt mollit anim id est laborum."
        val watermark = "Hello World"
        val textFile = TextFile.fromString(text)
        val parsedWatermark = Watermark.fromString(watermark)

        // Add watermark
        // Act
        val status = textWatermarker.addWatermark(textFile, parsedWatermark)

        // Assert
        assertTrue(status.isSuccess)

        // Extract Watermark
        // Act
        val watermarks = textWatermarker.getWatermarks(textFile)

        // Assert
        assertTrue(watermarks.isSuccess)
        assertEquals(watermarks.value!!.size, 1)
        val firstWatermark = watermarks.value!![0].watermarkContent.decodeToString()
        assertEquals(watermark, firstWatermark)
    }
}
