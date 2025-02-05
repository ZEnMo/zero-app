package com.zenmo.zummon.companysurvey

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zenmo.zummon.BenasherUuidSerializer
import kotlinx.datetime.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport
import kotlin.math.roundToInt
import kotlin.reflect.typeOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds

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
    /**
     * Duration of the measurement interval.
     * It would be easier to use [DateTimeUnit.TimeBased] or [Duration] instead of [DateTimeUnit]
     * but then we would lose the ability to process month-based time series.
     */
    @Serializable(with = BackwardCompatilbeDateTimeUnitSerializer::class)
    val timeStep: DateTimeUnit = type.defaultStep(),
    val unit: TimeSeriesUnit = type.defaultUnit(),
    val values: FloatArray = floatArrayOf(),
) {
    @Deprecated("Use .values", ReplaceWith("values"))
    fun getFlatDataPoints(): FloatArray = values

    fun calculateEnd(): Instant = start.plus(values.size, timeStep, TimeZone.of("Europe/Amsterdam"))

    fun sum(): Float = values.sum()

    fun isValid() = !isEmpty() && !sum().isNaN()

    fun withStartEpochSeconds(epochSeconds: Double) = copy(
        start = Instant.fromEpochSeconds(epochSeconds.toLong())
    )

    fun withTimeStep(timeStep: DateTimeUnit) = copy(timeStep = timeStep)

    /**
     * This can be used to create a J_EAProfile in Zero Engine.
     */
    fun timeStepInHours() = this.timeStep.toHours()

    /**
     * The number of values needed to fill a year using the specified time step.
     * So far our simulation always assumes 365 days in a year.
     */
    fun numValuesNeededForFullYear() = (DateTimeUnit.YEAR.toDuration() / timeStep.toDuration()).roundToInt()

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
        val numValuesToSliceOffStart = (diff / timeStep.toDuration()).toInt()

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
        val numValuesInCurrentYear = ((end - start) / timeStep.toDuration()).toInt()

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
        override fun defaultStep() = DateTimeUnit.MINUTE * 15
    },
    // Feed-in of end-user back in to the rid
    ELECTRICITY_FEED_IN {
        override fun defaultUnit() = TimeSeriesUnit.KWH
        override fun defaultStep() = DateTimeUnit.MINUTE * 15
    },
    // Solar panel production
    ELECTRICITY_PRODUCTION {
        override fun defaultUnit() = TimeSeriesUnit.KWH
        override fun defaultStep() = DateTimeUnit.MINUTE * 15
    },
    GAS_DELIVERY {
        override fun defaultUnit() = TimeSeriesUnit.M3
        override fun defaultStep() = DateTimeUnit.HOUR
    };

    abstract fun defaultUnit(): TimeSeriesUnit
    abstract fun defaultStep(): DateTimeUnit
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
    val timeStep: DateTimeUnit,
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

        return value * (1.hours / this.timeStep.toDuration())
    }
}

@JsExport
fun instantToEpochSeconds(instant: Instant) = instant.epochSeconds.toDouble()

fun DateTimeUnit.toHours(): Double = this.toDuration() / 1.hours

/**
 * This assumes a 365-day year.
 * It looks like we will stick with this assumption in the simulation so we might get away with this.
 */
fun DateTimeUnit.toDuration(): Duration = when (this) {
    is DateTimeUnit.DayBased -> this.days.days
    is DateTimeUnit.MonthBased -> (this.months.toDouble() * (365.0 / 12.0)).days
    is DateTimeUnit.TimeBased -> this.duration
}

@JsExport
fun isoStringToDateTimeUnit(isoString: String): DateTimeUnit = when (isoString) {
    "PT15M" -> DateTimeUnit.MINUTE * 15
    "PT1H" -> DateTimeUnit.HOUR
    "P1D" -> DateTimeUnit.DAY
    "P1M" -> DateTimeUnit.MONTH
    else -> throw Exception("Not implemented parsing iso string \"$isoString\"")
}

@JsExport
fun dateTimeUnitToIsoString(dateTimeUnit: DateTimeUnit): String = when (dateTimeUnit) {
    DateTimeUnit.MINUTE * 15 -> "PT15M"
    DateTimeUnit.HOUR -> "PT1H"
    DateTimeUnit.DAY -> "P1D"
    DateTimeUnit.MONTH -> "P1M"
    else -> throw Exception("Not implemented creating iso string from ${dateTimeUnit}")
}

/**
 * This makes serialization of DateTimeUnit backwards compatible with the previous format.
 * The previous format was based on Duration.
 */
private class BackwardCompatilbeDateTimeUnitSerializer : KSerializer<DateTimeUnit> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlinx.datetime.DateTimeUnit", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTimeUnit) {
        encoder.encodeString(dateTimeUnitToIsoString(value))
    }

    override fun deserialize(decoder: Decoder): DateTimeUnit = isoStringToDateTimeUnit(decoder.decodeString())
}
