package companysurvey

import com.zenmo.zummon.companysurvey.TimeSeries
import com.zenmo.zummon.companysurvey.TimeSeriesType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeSeriesTest {
    @Test
    fun testEndOfEmptyTimeSeries() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2023-01-01T00:00:00+01:00"),
        )

        assertEquals(timeSeries.start, timeSeries.calculateEnd())
        assertFalse(timeSeries.hasNumberOfValuesForOneYear())
    }

    @Test
    fun testEndOfTwoAndAHalfDaySeries() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2023-01-01T00:00:00Z"),
            values = generateSequence { 0.0f }.take((2.5 * 4 * 24).toInt()).toList().toFloatArray()
        )

        assertEquals(
            Instant.parse("2023-01-03T12:00:00Z"),
            timeSeries.calculateEnd()
        )
        assertFalse(timeSeries.hasNumberOfValuesForOneYear())
    }

    @Test
    fun testFullYear() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2023-01-01T00:00:00+01:00"),
            values = iterator {
                yield(100f)
                yieldAll(generateSequence { 0.0f }.take((365 * 4 * 24) - 2))
                yield(200f)
            }.asSequence().toList().toFloatArray()
        )

        assertTrue(timeSeries.hasNumberOfValuesForOneYear())
        val yearValues = timeSeries.getFullYearOrFudgeIt(2023)
        assertEquals(100f, yearValues.first())
        assertEquals(200f, yearValues.last())
        assertEquals(300f, yearValues.sum(), 0.001f)
    }

    @Test
    fun testFullYearPlusOne() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2022-12-31T23:45:00+01:00"),
            values = iterator {
                yield(100f) // outside of year
                yield(200f) // jan 1st
                yieldAll(generateSequence { 0.0f }.take((365 * 4 * 24) - 2))
                yield(300f) // dec 31st
                yield(400f) // outside of year
            }.asSequence().toList().toFloatArray()
        )

        assertTrue(timeSeries.hasNumberOfValuesForOneYear())
        val yearValues = timeSeries.getFullYearOrFudgeIt(2023)
        assertEquals(200f, yearValues.first())
        assertEquals(300f, yearValues.last())
        assertEquals(500f, yearValues.sum(), 0.001f)
    }

    @Test
    fun testAlmostFullYear() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2023-01-01T00:00:00+01:00"),
            values = generateSequence { 1f }.take(((365 * 4 * 24) -1)).toList().toFloatArray()
        )

        assertFalse(timeSeries.hasNumberOfValuesForOneYear())
        assertFails {
            timeSeries.getFullYearOrFudgeIt(2023)
        }
    }

    @Test
    fun testFudgeYear() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2023-07-01T00:00:00+01:00"),
            values = iterator {
                yield(100f)
                yieldAll(generateSequence { 0.0f }.take((184 * 4 * 24) - 2))
                yield(200f) // dec 31st
                yield(300f) // jan 1st
                yieldAll(generateSequence { 0.0f }.take((181 * 4 * 24) - 2))
                yield(400f)
            }.asSequence().toList().toFloatArray()
        )

        assertTrue(timeSeries.hasNumberOfValuesForOneYear())
        val yearValues = timeSeries.getFullYearOrFudgeIt(2023)
        assertEquals(timeSeries.numValuesNeededForFullYear(), yearValues.size)
        assertEquals(1000f, yearValues.sum(), 0.1f)
        assertEquals(300f, yearValues.first())
        assertEquals(200f, yearValues.last())
    }

    @Test
    fun testGetPeakKw() {
        val timeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2024-01-01T00:00:00+01:00"),
            values = floatArrayOf(1f, 2f, 1f)
        )

        val peak = timeSeries.getPeak()
        assertEquals(2.0, peak.kWh())
        assertEquals(8.0, peak.kW())
    }

    @Test
    fun testConvertMonthlyToQuarterHourly() {
        val monthlyTimeSeries = TimeSeries(
            type = TimeSeriesType.ELECTRICITY_DELIVERY,
            start = Instant.parse("2024-01-01T00:00:00+01:00"),
            timeStep = DateTimeUnit.MONTH,
            values = floatArrayOf(100f, 200f)
        )

        val quarterHourlyTimeSeries = monthlyTimeSeries.convertToQuarterHourly()

        val smallestRepresentable = 300.0 / 2.0.pow(23)
        val tolerance = smallestRepresentable
        assertEquals(monthlyTimeSeries.sum(), quarterHourlyTimeSeries.sum(), tolerance)
        assertEquals(Instant.parse("2024-03-01T00:00:00+01:00"), quarterHourlyTimeSeries.calculateEnd())

        val numValuesInJanuary = 31 * 24 * 4;
        val numValuesInFebruary = 29 * 24 * 4;
        val numValues = numValuesInJanuary + numValuesInFebruary
        assertEquals(numValues, quarterHourlyTimeSeries.values.size)
        assertEquals(100.0, quarterHourlyTimeSeries.values.sliceArray(IntRange(0, numValuesInJanuary - 1)).sumOf { it.toDouble() }, tolerance)
        assertEquals(200.0, quarterHourlyTimeSeries.values.sliceArray(IntRange(numValuesInJanuary, numValues - 1)).sumOf { it.toDouble() }, tolerance)
    }
}
