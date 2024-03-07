/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package unitTest.returnTypes

import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val success = Event.Success()

object TestWarning : Event.Warning("Test") {
    /** Returns a String explaining the event */
    override fun getMessage(): String = "Test Warning."
}

object TestError : Event.Error("Test") {
    /** Returns a String explaining the event */
    override fun getMessage(): String = "Test Error."
}

class EventTest {
    @Test
    fun toString_success_success() {
        // Arrange
        val expected = "Success"

        // Act
        val result = success.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun toString_warning_success() {
        // Arrange
        val expected = "Warning (Test): Test Warning."

        // Act
        val result = TestWarning.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun toString_error_success() {
        // Arrange
        val expected = "Error (Test): Test Error."

        // Act
        val result = TestError.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun into_success_success() {
        // Arrange
        val expected = Status(success)

        // Act
        val result: Status = success.into()

        // Assert
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun intoResult_warning_success() {
        // Arrange
        val expected = Result(TestWarning.into(), true)

        // Act
        val result: Result<Boolean> = TestWarning.into(true)

        // Assert
        assertEquals(expected.toString(), result.toString())
    }
}

class StatusTest {
    @Test
    fun addEvent_success_success() {
        // Arrange
        val status = Status.success()

        // Act
        status.addEvent(success)

        // Assert
        assertTrue(status.isSuccess)
        assertTrue(status.getEvents().isEmpty())
    }

    @Test
    fun addEvent_warning_success() {
        // Arrange
        val status = Status.success()
        val expected = listOf(TestWarning)

        // Act
        status.addEvent(TestWarning)

        // Assert
        assertTrue(status.isWarning)
        assertEquals(expected, status.getEvents())
    }

    @Test
    fun addEvent_error_success() {
        // Arrange
        val status = Status.success()
        val expected = listOf(TestError)

        // Act
        status.addEvent(TestError)

        // Assert
        assertTrue(status.isError)
        assertEquals(expected, status.getEvents())
    }

    @Test
    fun addEvent_override_success() {
        // Arrange
        val status = TestError.into()
        val expected = listOf(TestError, TestWarning)

        // Act
        status.addEvent(TestWarning, true)

        // Assert
        assertTrue(status.isWarning)
        assertEquals(expected, status.getEvents())
    }

    @Test
    fun appendStatus_warning_success() {
        // Arrange
        val status1 = TestWarning.into()
        val status2 = TestError.into()
        val expected1 = listOf(TestWarning, TestError)
        val expected2 = listOf(TestError)

        // Act
        status1.appendStatus(status2)

        // Assert
        assertEquals(expected1, status1.getEvents())
        assertEquals(expected2, status2.getEvents())
    }

    @Test
    fun prependStatus_warning_success() {
        // Arrange
        val status1 = TestWarning.into()
        val status2 = TestError.into()
        val expected1 = listOf(TestError, TestWarning)
        val expected2 = listOf(TestError)

        // Act
        status1.prependStatus(status2)

        // Assert
        assertEquals(expected1, status1.getEvents())
        assertEquals(expected2, status2.getEvents())
    }

    @Test
    fun getMessage_success_success() {
        // Arrange
        val expected = "Success"

        // Act
        val result = success.into().getMessage()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun getMessage_warning_success() {
        // Arrange
        val expected = "Test Warning."

        // Act
        val result = TestWarning.into().getMessage()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun toString_success_success() {
        // Arrange
        val expected = "Success"

        // Act
        val result = success.into().getMessage()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun toString_error_success() {
        // Arrange
        val expected = "Error (Test): Test Error."

        // Act
        val result = TestError.into().toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun into_success_success() {
        // Arrange
        val status = success.into()
        val value = true
        val expected = Result(status, value)

        // Act
        val result = status.into(value)

        // Assert
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun companionSuccess_success_success() {
        // Arrange
        val expected = success.into()

        // Act
        val result = Status.success()

        // Assert
        assertEquals(expected.toString(), result.toString())
    }
}
