package com.zenmo.orm.dbutil

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Map PostgreSQL Interval type to Kotlin Duration type.
 * This is only implemented in minutes for now.
 */
class IntervalColumnType : ColumnType<Duration>() {
    override fun sqlType(): String = "INTERVAL"

    override fun notNullValueToDB(value: Duration): String {
        val duration = value.inWholeMinutes
        return "'$duration minutes'"
    }

    private val regex = "(?<mins>\\d+) mins".toRegex()

    override fun valueFromDB(value: Any): Duration {
        if (value !is String) {
            return valueFromDB(value.toString())
        }

        val result = regex.find(value)?.value?.toInt()?.minutes
        if (result == null) {
            throw Exception("Unrecognised postgres interval format: $value")
        }

        return result
    }

//    override fun readObject(rs: ResultSet, index: Int): Any? {
//        // ResultSet.getLong returns 0 instead of null
//        return rs.getLong(index).takeIf { rs.getObject(index) != null }
//    }
}

fun Table.interval(name: String): Column<Duration> = registerColumn(name, IntervalColumnType())
