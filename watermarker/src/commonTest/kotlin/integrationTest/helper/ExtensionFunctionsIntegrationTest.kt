/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package integrationTest.helper

import de.fraunhofer.isst.trend.watermarker.helper.fromBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.helper.toBytesLittleEndian
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionFunctionsIntegrationTest {
    @Test
    fun uIntToBytesLittleEndian_UIntCompanionFromBytesLittleEndian_inversion() {
        // Arrange
        val uInt = 0x12345678u

        // Act
        val result = UInt.fromBytesLittleEndian(uInt.toBytesLittleEndian())

        // Assert
        assertEquals(uInt, result)
    }

    @Test
    fun uShortToBytesLittleEndian_UShortCompanionFromBytesLittleEndian_inversion() {
        // Arrange
        val uShort = 0x1234u.toUShort()

        // Act
        val result = UShort.fromBytesLittleEndian(uShort.toBytesLittleEndian())

        // Assert
        assertEquals(uShort, result)
    }
}
