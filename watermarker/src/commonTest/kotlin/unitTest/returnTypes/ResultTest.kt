/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.returnTypes

import de.fraunhofer.isst.innamark.watermarker.returnTypes.Event
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTest {
    @Test
    fun intoResult_warning_success() {
        // Arrange
        val status = TestWarning.into()
        val value = 0
        val res = status.into(true)

        // Act
        val result: Result<Int> = res.into(value)

        // Assert
        assertEquals(res.toString(), result.toString())
        assertEquals(value, result.value)
    }

    @Test
    fun into_warning_success() {
        // Arrange
        val status = TestWarning.into()
        val res = status.into(true)

        // Act
        val result = res.into()

        // Assert
        assertEquals(status.toString(), result.toString())
    }

    @Test
    fun companionSuccess_success_success() {
        // Arrange
        val value = true
        val expected = Event.Success().into(value)

        // Act
        val result = Result.success(value)

        // Assert
        assertEquals(expected.toString(), result.toString())
        assertEquals(value, result.value)
    }
}
