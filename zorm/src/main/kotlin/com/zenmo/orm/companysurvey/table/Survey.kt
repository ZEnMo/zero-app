package com.zenmo.orm.companysurvey.table

import com.zenmo.orm.dbutil.ZenmoUUIDTable
import com.zenmo.orm.user.table.UserTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * [com.zenmo.zummon.companysurvey.Survey]
 */
object CompanySurveyTable: ZenmoUUIDTable("company_survey") {
    val created = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val includeInSimulation = bool("include_in_simulation").default(true)
    // Can be fetched at https://energiekeregio.nl/api/v1/zenmo?details=15989
    val energiekeRegioId = uinteger("energieke_regio_id").nullable()
    val projectId = uuid("project_id").references(ProjectTable.id)
    val createdById = uuid("created_by_id").references(UserTable.id).nullable()

    val companyName = varchar("company_name", 50)
    val personName = varchar("person_name", 50)
    val email = varchar("email", 50)
    val dataSharingAgreed = bool("data_sharing_agreed").default(false)
}
