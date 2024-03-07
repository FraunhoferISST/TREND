/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package integrationTest.files

import de.fraunhofer.isst.trend.watermarker.files.TextFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextFileIntegrationTest {
    private val loremIpsum =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
            "incididunt ut labore et dolore magna aliqua. Blandit volutpat maecenas " +
            "volutpat blandit aliquam etiam erat velit."
    private val loremIpsumBytes = loremIpsum.encodeToByteArray()

    @Test
    fun toBytes_fromBytes_inversion() {
        // Act
        val result = TextFile.fromBytes(loremIpsumBytes)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(loremIpsum, result.value?.content)
    }
}
