package com.zenmo.orm.companysurvey.table

import com.zenmo.orm.dbutil.PGEnum
import com.zenmo.orm.dbutil.ZenmoUUIDTable
import com.zenmo.orm.dbutil.dateTimeUnit
import com.zenmo.zummon.companysurvey.TimeSeriesType
import com.zenmo.zummon.companysurvey.TimeSeriesUnit
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TimeSeriesTable: ZenmoUUIDTable("time_series") {
    // The grid connection this time series belongs to
    val gridConnectionId = uuid("grid_connection_id")
        .references(GridConnectionTable.id, onDelete = ReferenceOption.CASCADE)

    // Metadata of the time series
    val type = customEnumeration(
        "type",
        TimeSeriesType::class.simpleName,
        fromDb = { TimeSeriesType.valueOf(it as String) },
        toDb = { PGEnum(TimeSeriesType::class.simpleName!!, it) })
    val start = timestamp("start").default(Instant.parse("2023-01-01T00:00:00+01"))
    val timeStep = dateTimeUnit("time_step").default(DateTimeUnit.MINUTE * 15)
    val unit = customEnumeration(
        "unit",
        TimeSeriesUnit::class.simpleName,
        fromDb = { TimeSeriesUnit.valueOf(it as String) },
        toDb = { PGEnum(TimeSeriesUnit::class.simpleName!!, it) })

    // The data.
    // TODO: this mapping currently requires an intermediate List<Float> to be created.
    val values = array<Float>("value")
}
