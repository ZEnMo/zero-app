package com.zenmo.orm.dbutil

import kotlinx.datetime.DateTimeUnit
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGInterval

/**
 * Map PostgreSQL Interval type to Kotlin DateTimeUnit type.
 * Implemented up to whole second granularity.
 */
class DateTimeUnitColumnType : ColumnType<DateTimeUnit>() {
    override fun sqlType(): String = "INTERVAL"

    override fun notNullValueToDB(value: DateTimeUnit): PGInterval {
        return when (value) {
            is DateTimeUnit.TimeBased -> {
                val hours =  value.duration.inWholeHours.toInt()
                if (hours > 0 ) {
                    return PGInterval(0, 0, 0, hours, 0, 0.0)
                }

                val minutes = value.duration.inWholeMinutes.toInt()
                if (minutes > 0 ) {
                    return PGInterval(0, 0, 0, 0, minutes, 0.0)
                }

                val seconds = value.duration.inWholeSeconds
                if (seconds > 0) {
                    return PGInterval(0, 0, 0, 0, 0, seconds.toDouble())
                }

                throw Exception("Mapping DateTimeUnit ${value.duration.toIsoString()} to database is not implemented")
            }

            is DateTimeUnit.DayBased -> PGInterval(0, 0, value.days, 0, 0, 0.0)
            is DateTimeUnit.MonthBased -> PGInterval(0, value.months, 0, 0, 0, 0.0)
        }
    }

    private val regex = "^(?<years>\\d+) years (?<months>\\d+) mons (?<days>\\d+) days (?<hours>\\d+) hours (?<minutes>\\d+) mins (?<seconds>\\d+).0 secs$".toRegex()

    override fun valueFromDB(value: Any): DateTimeUnit? {
        if (value !is String) {
            return valueFromDB(value.toString())
        }

        val match =  regex.find(value) ?: throw Exception("Unrecognised postgres interval format: $value")
        val groupValues = match.groupValues

        val years = groupValues[1].toInt()
        val months = groupValues[2].toInt()
        val days = groupValues[3].toInt()
        val hours = groupValues[4].toInt()
        val minutes = groupValues[5].toInt()
        val seconds = groupValues[6].toInt()

        return when {
            years > 0 -> DateTimeUnit.YEAR * years
            months > 0 -> DateTimeUnit.MONTH * months
            days > 0 -> DateTimeUnit.DAY * days
            hours > 0 -> DateTimeUnit.HOUR * hours
            minutes > 0 -> DateTimeUnit.MINUTE * minutes
            seconds > 0 -> DateTimeUnit.SECOND * seconds
            else -> throw Exception("Got empty postgres interval: $value")
        }
    }

    override fun nonNullValueAsDefaultString(value: DateTimeUnit): String =
        buildString {
            append("'")
            append(notNullValueToDB(value).toString())
            append("'::interval")
        }
}

fun Table.dateTimeUnit(name: String): Column<DateTimeUnit> = registerColumn(name, DateTimeUnitColumnType())
