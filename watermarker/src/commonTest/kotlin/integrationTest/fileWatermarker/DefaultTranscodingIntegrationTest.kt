/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package integrationTest.fileWatermarker

import de.fraunhofer.isst.trend.watermarker.fileWatermarker.DefaultTranscoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTranscodingIntegrationTest {
    private val loremIpsum =
        listOf<Byte>(
            0x00, 0x01, 0x0F, 0x41, 0x62, 0xAA.toByte(), 0xF0.toByte(), 0xFE.toByte(),
            0xFF.toByte(), 0x42, 0xef.toByte(), 0xfc.toByte(), 0x2d, 0xe3.toByte(), 0xc9.toByte(),
            0xfa.toByte(), 0x08, 0x87.toByte(), 0x2d,
        )

    @Test
    fun decode_encode_inversion() {
        // Act
        val result = DefaultTranscoding.decode(DefaultTranscoding.encode(loremIpsum))

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(loremIpsum, result.value)
    }
}
