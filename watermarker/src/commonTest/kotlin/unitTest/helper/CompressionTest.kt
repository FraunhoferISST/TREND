/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.helper

import de.fraunhofer.isst.trend.watermarker.helper.Compression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompressionTest {
    @Test
    fun deflate_empty_success() {
        // Arrange
        val input = TestCases.empty
        val expected = TestCases.emptyDeflated

        // Act
        val result = Compression.deflate(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun inflate_empty_success() {
        // Arrange
        val input = TestCases.emptyDeflated
        val expected = TestCases.empty

        // Act
        val result = Compression.inflate(input)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun deflate_allBytes_success() {
        // Arrange
        val input = TestCases.allBytes
        val expected = TestCases.allBytesDeflated

        // Act
        val result = Compression.deflate(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun inflate_allBytes_success() {
        // Arrange
        val input = TestCases.allBytesDeflated
        val expected = TestCases.allBytes

        // Act
        val result = Compression.inflate(input)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun deflate_helloWorld_success() {
        // Arrange
        val input = TestCases.helloWorld
        val expected = TestCases.helloWorldDeflated

        // Act
        val result = Compression.deflate(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun inflate_helloWorld_success() {
        // Arrange
        val input = TestCases.helloWorldDeflated
        val expected = TestCases.helloWorld

        // Act
        val result = Compression.inflate(input)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }

    @Test
    fun deflate_loremIpsum_success() {
        // Arrange
        val input = TestCases.loremIpsum
        val expected = TestCases.loremIpsumDeflated

        // Act
        val result = Compression.deflate(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun inflate_loremIpsum_success() {
        // Arrange
        val input = TestCases.loremIpsumDeflated
        val expected = TestCases.loremIpsum

        // Act
        val result = Compression.inflate(input)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.value)
    }
}

object TestCases {
    val empty = listOf<Byte>()
    val emptyDeflated = listOf<Byte>()

    val helloWorld =
        listOf<Byte>(
            0x48, 0x65, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21,
        )
    val helloWorldDeflated =
        listOf<Byte>(
            0xf3.toByte(), 0x48, 0xcd.toByte(), 0xc9.toByte(), 0x57, 0x08, 0xcf.toByte(), 0x2f,
            0xca.toByte(), 0x49, 0x51, 0x04, 0x00,
        )

    val allBytes = (0..255).toList().map { it.toByte() }
    val allBytesDeflated = listOf<Byte>(0x01, 0x00, 0x01, 0xff.toByte(), 0xfe.toByte()) + allBytes

    val loremIpsum =
        (
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Nisl condimentum id venenatis a " +
                "condimentum. Donec enim diam vulputate ut pharetra sit amet aliquam. " +
                "Sollicitudin ac orci phasellus egestas tellus rutrum tellus. Semper quis lectus " +
                "nulla at volutpat diam ut venenatis. Id ornare arcu odio ut. Nisi est sit amet " +
                "facilisis. Ipsum dolor sit amet consectetur adipiscing elit ut. Quam id leo in " +
                "vitae turpis massa sed elementum. Orci dapibus ultrices in iaculis nunc sed " +
                "augue lacus. Enim tortor at auctor urna nunc id cursus metus aliquam. Feugiat " +
                "vivamus at augue eget arcu dictum varius. Nascetur ridiculus mus mauris vitae " +
                "ultricies leo integer. Cum sociis natoque penatibus et. Turpis egestas maecenas " +
                "pharetra convallis posuere morbi leo urna molestie. Lobortis elementum nibh " +
                "tellus molestie. Ac felis donec et odio. Ipsum suspendisse ultrices gravida " +
                "dictum."
        ).encodeToByteArray().asList()
    val loremIpsumDeflated =
        listOf<Byte>(
            0x7d, 0x52, 0x5b, 0x8a.toByte(), 0xdd.toByte(), 0x30, 0x0c, 0xdd.toByte(),
            0x8a.toByte(), 0x16, 0x50, 0xb2.toByte(), 0x87.toByte(), 0xd2.toByte(), 0x76,
            0xa0.toByte(), 0x30, 0x4c, 0x29, 0xed.toByte(), 0x06, 0x74, 0x6d, 0x4d, 0x46,
            0xe0.toByte(), 0xd8.toByte(), 0xb9.toByte(), 0x96.toByte(), 0x94.toByte(),
            0xf5.toByte(), 0xf7.toByte(), 0xd8.toByte(), 0xb9.toByte(), 0x8f.toByte(), 0x7e,
            0x94.toByte(), 0x42, 0x12, 0x92.toByte(), 0x58, 0x47, 0x3a, 0x0f, 0xbd.toByte(),
            0xb6.toByte(), 0x2e, 0x1b, 0xe9.toByte(), 0x6e, 0xb1.toByte(), 0x51, 0x6e,
            0xa5.toByte(), 0x75, 0x32, 0x75, 0xe2.toByte(), 0x4d, 0xfc.toByte(), 0x13,
            0xa5.toByte(), 0x56, 0x4d, 0x92.toByte(), 0x8b.toByte(), 0x47, 0x27, 0xce.toByte(),
            0xba.toByte(), 0xab.toByte(), 0x25, 0xad.toByte(), 0x2b, 0x49, 0x51, 0x1c,
            0x9a.toByte(), 0x64, 0x00, 0x48, 0x34, 0x6c, 0x6b, 0x99.toByte(), 0x5c, 0xb6.toByte(),
            0x1d, 0x60, 0xad.toByte(), 0x49, 0xb3.toByte(), 0xe6.toByte(), 0xa8.toByte(), 0x4e,
            0xe1.toByte(), 0x54, 0xf8.toByte(), 0x82.toByte(), 0xf6.toByte(), 0x24, 0x7e,
            0xb6.toByte(), 0x16, 0xda.toByte(), 0x78, 0xad.toByte(), 0x4c, 0x5c, 0xf4.toByte(),
            0x1a, 0xbc.toByte(), 0xd0.toByte(), 0x9b.toByte(), 0x5a, 0x19, 0x43, 0xb2.toByte(),
            0x6e, 0x52, 0x1d, 0x04, 0x34, 0xd3.toByte(), 0x21, 0x55, 0x2a, 0xbb.toByte(), 0x1a,
            0xf1.toByte(), 0xdf.toByte(), 0x47, 0x0b, 0x7d, 0x6d, 0x55, 0x12, 0x49, 0x55,
            0xf0.toByte(), 0x54, 0xde.toByte(), 0xe8.toByte(), 0x88.toByte(), 0xb2.toByte(),
            0x87.toByte(), 0xb3.toByte(), 0xcb.toByte(), 0x18, 0xb4.toByte(), 0x7f, 0x70, 0x17,
            0xef.toByte(), 0xfc.toByte(), 0x20, 0x7f, 0x9b.toByte(), 0x01, 0xdc.toByte(),
            0xaf.toByte(), 0x56, 0x8a.toByte(), 0x26, 0xf5.toByte(), 0xc8.toByte(), 0x5a,
            0x89.toByte(), 0x13, 0xb5.toByte(), 0x9e.toByte(), 0x74, 0xd4.toByte(), 0x9b.toByte(),
            0x94.toByte(), 0x12, 0x46, 0xb2.toByte(), 0x8a.toByte(), 0x39, 0x1b, 0xf8.toByte(),
            0xcf.toByte(), 0xcf.toByte(), 0x1e, 0xde.toByte(), 0x41, 0xe4.toByte(), 0xfc.toByte(),
            0x02, 0x18, 0xaa.toByte(), 0xa4.toByte(), 0xd3.toByte(), 0x35, 0xc0.toByte(),
            0xa7.toByte(), 0xc0.toByte(), 0x0a, 0x54, 0xd4.toByte(), 0x28, 0x05, 0x12,
            0x9c.toByte(), 0x8e.toByte(), 0x56, 0xc2.toByte(), 0x77, 0xbc.toByte(), 0x4c, 0x3a,
            0x20, 0xf1.toByte(), 0xa0.toByte(), 0xbe.toByte(), 0xd0.toByte(), 0xf7.toByte(),
            0x8c.toByte(), 0x39, 0x15, 0x9c.toByte(), 0x88.toByte(), 0x7b, 0x0a, 0x6a, 0x59, 0x1b,
            0x2a, 0xa6.toByte(), 0x62, 0x25, 0x0c, 0x7c, 0xf2.toByte(), 0x7c, 0xe7.toByte(),
            0xa4.toByte(), 0x05, 0x7f, 0x07, 0xe8.toByte(), 0x1f, 0x21, 0xfc.toByte(), 0x2f,
            0x83.toByte(), 0xd9.toByte(), 0xf1.toByte(), 0x27, 0x44, 0x0e, 0xdf.toByte(),
            0x8a.toByte(), 0x34, 0x98.toByte(), 0x4f, 0x87.toByte(), 0x3a, 0x0b, 0xa1.toByte(),
            0x18, 0x85.toByte(), 0x70, 0xdb.toByte(), 0x8c.toByte(), 0x67, 0x50, 0x52,
            0xe4.toByte(), 0xee.toByte(), 0xe3.toByte(), 0x8f.toByte(), 0xa1.toByte(), 0x3f,
            0xf3.toByte(), 0xae.toByte(), 0x17, 0x88.toByte(), 0x89.toByte(), 0xe2.toByte(), 0x5d,
            0x93.toByte(), 0xd8.toByte(), 0x80.toByte(), 0x2a, 0xa7.toByte(), 0x00, 0x13, 0x08,
            0xac.toByte(), 0x69, 0x82.toByte(), 0x38, 0xd6.toByte(), 0x10, 0x64, 0x98.toByte(),
            0x86.toByte(), 0x13, 0xdf.toByte(), 0x86.toByte(), 0xf1.toByte(), 0xde.toByte(), 0x3a,
            0xae.toByte(), 0x21, 0x9e.toByte(), 0x23, 0x8d.toByte(), 0xb7.toByte(), 0x80.toByte(),
            0xc8.toByte(), 0x13, 0x00, 0x0a, 0x29, 0xba.toByte(), 0xa1.toByte(), 0x27, 0x58,
            0xe3.toByte(), 0xf9.toByte(), 0xf0.toByte(), 0xff.toByte(), 0x45, 0x62, 0xd5.toByte(),
            0x61, 0x97.toByte(), 0x1e, 0xbc.toByte(), 0x8d.toByte(), 0x03, 0xbf.toByte(), 0x35,
            0x86.toByte(), 0xf3.toByte(), 0x7e, 0xda.toByte(), 0x93.toByte(), 0x35, 0x8d.toByte(),
            0xf4.toByte(), 0x0f, 0xee.toByte(), 0x3a, 0x46, 0xbd.toByte(), 0xb1.toByte(),
            0xa5.toByte(), 0xa9.toByte(), 0xb7.toByte(), 0x63, 0x95.toByte(), 0xc0.toByte(), 0x69,
            0xf4.toByte(), 0x1c, 0x37, 0x47, 0x07, 0xbd.toByte(), 0x53, 0xe1.toByte(), 0x49, 0x5c,
            0xc5.toByte(), 0x6e, 0xc2.toByte(), 0x1d, 0xdd.toByte(), 0xfa.toByte(), 0x42, 0x5f,
            0xd0.toByte(), 0xc6.toByte(), 0x5a, 0xd2.toByte(), 0x21, 0x83.toByte(), 0xbd.toByte(),
            0x5d, 0x31, 0x66, 0x9f.toByte(), 0x99.toByte(), 0x0c, 0xb1.toByte(), 0x02,
            0xbb.toByte(), 0x7e, 0x9f.toByte(), 0xce.toByte(), 0xdc.toByte(), 0x53, 0xdf.toByte(),
            0x58, 0x12, 0xce.toByte(), 0xed.toByte(), 0xb9.toByte(), 0x40, 0xf0.toByte(),
            0xfb.toByte(), 0xe0.toByte(), 0x32, 0x7c, 0xd8.toByte(), 0x9b.toByte(), 0x85.toByte(),
            0x8c.toByte(), 0x9d.toByte(), 0x6d, 0xfd.toByte(), 0xa2.toByte(), 0x73, 0xcc.toByte(),
            0x54, 0xbb.toByte(), 0xb5.toByte(), 0x02, 0xac.toByte(), 0xca.toByte(), 0x42,
            0xaf.toByte(), 0x0d, 0xdb.toByte(), 0x3d, 0x36, 0xf5.toByte(), 0xe1.toByte(), 0x2e,
            0x55, 0xbd.toByte(), 0x7c, 0xdc.toByte(), 0x57, 0xe9.toByte(), 0x59, 0xf8.toByte(),
            0x39, 0xd1.toByte(), 0xbb.toByte(), 0x8c.toByte(), 0x96.toByte(), 0xf9.toByte(),
            0xdc.toByte(), 0x61, 0x9f.toByte(), 0x0b, 0x71, 0x0f, 0x1c, 0x9e.toByte(),
            0x81.toByte(), 0x63, 0x56, 0x33, 0x79, 0xe6.toByte(), 0xb1.toByte(), 0x76, 0x3e, 0x34,
            0xf3.toByte(), 0xcd.toByte(), 0x9a.toByte(), 0xe5.toByte(), 0x0f,
        )
}
