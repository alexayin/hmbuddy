package com.example.hmbuddy.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    // RunType conversion tests

    @Test
    fun `fromRunType converts ZONE2 to string`() {
        val result = converters.fromRunType(RunType.ZONE2)
        assertEquals("ZONE2", result)
    }

    @Test
    fun `fromRunType converts TEMPO to string`() {
        val result = converters.fromRunType(RunType.TEMPO)
        assertEquals("TEMPO", result)
    }

    @Test
    fun `toRunType converts string to ZONE2`() {
        val result = converters.toRunType("ZONE2")
        assertEquals(RunType.ZONE2, result)
    }

    @Test
    fun `toRunType converts string to TEMPO`() {
        val result = converters.toRunType("TEMPO")
        assertEquals(RunType.TEMPO, result)
    }

    @Test
    fun `RunType round trip conversion preserves ZONE2`() {
        val original = RunType.ZONE2
        val converted = converters.fromRunType(original)
        val restored = converters.toRunType(converted)
        assertEquals(original, restored)
    }

    @Test
    fun `RunType round trip conversion preserves TEMPO`() {
        val original = RunType.TEMPO
        val converted = converters.fromRunType(original)
        val restored = converters.toRunType(converted)
        assertEquals(original, restored)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toRunType throws exception for invalid string`() {
        converters.toRunType("INVALID")
    }

    // Gender conversion tests

    @Test
    fun `fromGender converts MALE to string`() {
        val result = converters.fromGender(Gender.MALE)
        assertEquals("MALE", result)
    }

    @Test
    fun `fromGender converts FEMALE to string`() {
        val result = converters.fromGender(Gender.FEMALE)
        assertEquals("FEMALE", result)
    }

    @Test
    fun `toGender converts string to MALE`() {
        val result = converters.toGender("MALE")
        assertEquals(Gender.MALE, result)
    }

    @Test
    fun `toGender converts string to FEMALE`() {
        val result = converters.toGender("FEMALE")
        assertEquals(Gender.FEMALE, result)
    }

    @Test
    fun `Gender round trip conversion preserves MALE`() {
        val original = Gender.MALE
        val converted = converters.fromGender(original)
        val restored = converters.toGender(converted)
        assertEquals(original, restored)
    }

    @Test
    fun `Gender round trip conversion preserves FEMALE`() {
        val original = Gender.FEMALE
        val converted = converters.fromGender(original)
        val restored = converters.toGender(converted)
        assertEquals(original, restored)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toGender throws exception for invalid string`() {
        converters.toGender("INVALID")
    }

    // All enum values tests

    @Test
    fun `all RunType values can be converted to string and back`() {
        RunType.entries.forEach { runType ->
            val stringValue = converters.fromRunType(runType)
            val restoredValue = converters.toRunType(stringValue)
            assertEquals("Failed for $runType", runType, restoredValue)
        }
    }

    @Test
    fun `all Gender values can be converted to string and back`() {
        Gender.entries.forEach { gender ->
            val stringValue = converters.fromGender(gender)
            val restoredValue = converters.toGender(stringValue)
            assertEquals("Failed for $gender", gender, restoredValue)
        }
    }

    // LocalDate conversion tests

    @Test
    fun `fromLocalDate converts date to ISO string`() {
        val date = LocalDate.of(2026, 2, 20)
        val result = converters.fromLocalDate(date)
        assertEquals("2026-02-20", result)
    }

    @Test
    fun `fromLocalDate returns null for null input`() {
        val result = converters.fromLocalDate(null)
        assertNull(result)
    }

    @Test
    fun `toLocalDate converts ISO string to LocalDate`() {
        val result = converters.toLocalDate("2026-02-20")
        assertEquals(LocalDate.of(2026, 2, 20), result)
    }

    @Test
    fun `toLocalDate returns null for null input`() {
        val result = converters.toLocalDate(null)
        assertNull(result)
    }

    @Test
    fun `LocalDate round trip conversion preserves date`() {
        val original = LocalDate.of(2026, 6, 15)
        val converted = converters.fromLocalDate(original)
        val restored = converters.toLocalDate(converted)
        assertEquals(original, restored)
    }

    @Test
    fun `LocalDate conversion handles leap year date`() {
        val leapYearDate = LocalDate.of(2024, 2, 29)
        val converted = converters.fromLocalDate(leapYearDate)
        val restored = converters.toLocalDate(converted)
        assertEquals(leapYearDate, restored)
    }

    @Test
    fun `LocalDate conversion handles year boundary dates`() {
        val startOfYear = LocalDate.of(2026, 1, 1)
        val endOfYear = LocalDate.of(2026, 12, 31)

        assertEquals(startOfYear, converters.toLocalDate(converters.fromLocalDate(startOfYear)))
        assertEquals(endOfYear, converters.toLocalDate(converters.fromLocalDate(endOfYear)))
    }
}
