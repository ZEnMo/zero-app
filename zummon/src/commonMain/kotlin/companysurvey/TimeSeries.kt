package com.zenmo.zummon.companysurvey

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zenmo.zummon.BenasherUuidSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.time.Duration

/**
 * This contains values parsed from a CSV/excel or fetched from an API.
 */
@JsExport
@Serializable
data class TimeSeries (
    @Serializable(with = BenasherUuidSerializer::class)
    val id: Uuid = uuid4(),
    val type: TimeSeriesType,
    /** Start of the first measurement interval */
    val start: Instant,
    /** Duration of the measurement interval */
    val timeStep: Duration = type.defaultStep(),
    val unit: TimeSeriesUnit = type.defaultUnit(),
    val values: FloatArray = floatArrayOf(),
) {
    @Deprecated("Use .values", ReplaceWith("values"))
    fun getFlatDataPoints(): FloatArray = values

    fun calculateEnd(): Instant = start + (timeStep * values.size)

    fun sum(): Float = values.sum()

    fun isValid() = !isEmpty() && !sum().isNaN()

    fun withStartEpochSeconds(epochSeconds: Double) = copy(
        start = Instant.fromEpochSeconds(epochSeconds.toLong())
    )

    /**
     * The number of values needed to fill a year using the specified time step.
     */
    fun numValuesNeededForFullYear() = (1.days / timeStep * 365).toInt()

    fun hasNumberOfValuesForOneYear() = values.size >= numValuesNeededForFullYear()

    fun assertHasNumberOfValuesForOneYear(): Unit {
        val numValuesNeededForFullYear = numValuesNeededForFullYear()
        if (values.size < numValuesNeededForFullYear) {
            throw Exception("Not enough values for year: needed $numValuesNeededForFullYear got ${values.size}")
        }
    }

    fun withValues(values: FloatArray) = copy(values = values)

    fun isEmpty() = values.isEmpty()

    fun hasFullYear(year: Int? = null): Boolean {
        return try {
            val workingYear = year ?: this.start.toLocalDateTime(TimeZone.of("UTC+01:00")).year
            val startOfYear = Instant.parse("$workingYear-01-01T00:00:00+01:00")
            val endOfYear = Instant.parse("${workingYear + 1}-01-01T00:00:00+01:00")
            this.start <= startOfYear && this.calculateEnd() >= endOfYear
        } catch (e: Exception) {
            println("Time zone error: Falling back to UTC. ${this.start} Error: ${e.message}")
            false
        }
    }

    fun lengthEmptyGapsData(): Int {
        var maxNullSequence = 0
        var currentNullSequence = 0

        for (value in values) {
            if (value.isNaN() || value == 0f) {
                currentNullSequence++
                if (currentNullSequence > maxNullSequence) {
                    maxNullSequence = currentNullSequence
                }
            } else {
                currentNullSequence = 0 // Reset counter when encountering a non-null value
            }
        }

        return maxNullSequence
    }

    fun getPeak(): DataPoint = DataPoint(values.max(), unit, timeStep)

    /**
     * Get a full calendar year of data if it is present.
     * If it isn't, put together a year by appending the last part of the previous year to the data of the most-recent year.
     * Always returns 365 days worth of values.
     */
    fun getFullYearOrFudgeIt(year: Int): FloatArray {
        assertHasNumberOfValuesForOneYear()

        val startOfYear = Instant.parse("$year-01-01T00:00:00+01:00")
        // can be negative
        val diff = startOfYear - start
        val numValuesToSliceOffStart = (diff / timeStep).toInt()

        if (numValuesToSliceOffStart >= 0) {
            val rangeEnd = numValuesToSliceOffStart + numValuesNeededForFullYear()
            if (rangeEnd > values.size) {
                return sliceMostRecentDataToAlignedYear()
            }
            return values.sliceArray(IntRange(numValuesToSliceOffStart, rangeEnd - 1))
        } else {
            return sliceMostRecentDataToAlignedYear()
        }
    }

    /**
     * Put together a year of data by appending the last part of the previous year to the data of the most-recent year.
     * Always returns 365 days worth of values.
     */
    fun sliceMostRecentDataToAlignedYear(): FloatArray {
        assertHasNumberOfValuesForOneYear()

        val end = calculateEnd()
        val year = end.toLocalDateTime(TimeZone.of("UTC+01:00")).year
        val start = Instant.parse("$year-01-01T00:00:00+01:00")
        val numValuesInCurrentYear = ((end - start) / timeStep).toInt()

        return values.sliceArray(IntRange(values.size - numValuesInCurrentYear, values.size - 1))
            .plus(values.sliceArray(IntRange(values.size - numValuesNeededForFullYear(), values.size - numValuesInCurrentYear - 1)))
    }

    /**
     * Generated code
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TimeSeries

        if (id != other.id) return false
        if (type != other.type) return false
        if (start != other.start) return false
        if (timeStep != other.timeStep) return false
        if (unit != other.unit) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    /**
     * Generated code
     */
    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + timeStep.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }

    fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun timeSeriesFromJson(json: String): TimeSeries = Json.decodeFromString(TimeSeries.serializer(), json)

@JsExport
enum class TimeSeriesUnit {
    KWH,
    M3,
}

@JsExport
enum class TimeSeriesType {
    // Delivery from grid to end-user
    ELECTRICITY_DELIVERY {
        override fun defaultUnit() = TimeSeriesUnit.KWH
        override fun defaultStep() = 15.minutes
    },
    // Feed-in of end-user back in to the rid
    ELECTRICITY_FEED_IN {
        override fun defaultUnit() = TimeSeriesUnit.KWH
        override fun defaultStep() = 15.minutes
    },
    // Solar panel production
    ELECTRICITY_PRODUCTION {
        override fun defaultUnit() = TimeSeriesUnit.KWH
        override fun defaultStep() = 15.minutes
    },
    GAS_DELIVERY {
        override fun defaultUnit() = TimeSeriesUnit.M3
        override fun defaultStep() = 1.hours
    };

    abstract fun defaultUnit(): TimeSeriesUnit
    abstract fun defaultStep(): Duration
}

@JsExport
fun createEmptyTimeSeriesForYear(type: TimeSeriesType, year: Int) =
    TimeSeries(
        type = type,
        start = Instant.parse("$year-01-01T00:00:00+01:00")
    )

/**
 * Represents a single point within the time series.
 * Improvement: add timestamp
 */
data class DataPoint (
    val value: Float,
    val unit: TimeSeriesUnit,
    val timeStep: kotlin.time.Duration,
) {
    fun kWh(): Double {
        if (this.unit != TimeSeriesUnit.KWH) {
            throw UnsupportedOperationException("Can only get the kWh from a kWh data point")
        }

        return value.toDouble()
    }

    fun kW(): Double {
        if (this.unit != TimeSeriesUnit.KWH) {
            throw UnsupportedOperationException("Can only get the kW from a kWh data point")
        }

        return value * (1.hours / this.timeStep)
    }
}

@JsExport
fun instantToEpochSeconds(instant: Instant) = instant.epochSeconds.toDouble()
