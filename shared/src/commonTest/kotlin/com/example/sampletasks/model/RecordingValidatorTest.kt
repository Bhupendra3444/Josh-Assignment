package com.example.sampletasks.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecordingValidatorTest {
    @Test
    fun `duration below 10 seconds is invalid`() {
        val result = RecordingValidator.validate(9)
        assertFalse(result.isValid)
    }

    @Test
    fun `duration above 20 seconds is invalid`() {
        val result = RecordingValidator.validate(21)
        assertFalse(result.isValid)
    }

    @Test
    fun `duration inside range is valid`() {
        val result = RecordingValidator.validate(15)
        assertTrue(result.isValid)
    }
}
