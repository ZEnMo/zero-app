package com.zenmo.orm

import com.zenmo.orm.blob.BlobPurpose
import com.zenmo.orm.companysurvey.table.GridConnectionTable
import com.zenmo.orm.companysurvey.table.CompanySurveyTable
import com.zenmo.orm.companysurvey.table.FileTable
import com.zenmo.orm.companysurvey.table.gridConnectionSequence
import com.zenmo.orm.dbutil.createEnumTypeSql
import com.zenmo.orm.dbutil.createKleinverbruikEnumTypeSql
import com.zenmo.orm.energieprestatieonline.RawPandTable
import com.zenmo.orm.user.table.UserTable
import com.zenmo.zummon.companysurvey.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

val enums = listOf(
    KleinverbruikElectricityConsumptionProfile::class.java,
    HeatingType::class.java,
    PVOrientation::class.java,
    BlobPurpose::class.java,
    MissingPvReason::class.java,
)

val tables = arrayOf(
    CompanySurveyTable,
    GridConnectionTable,
    FileTable,
    RawPandTable,
    UserTable,
)

fun createSchema(db: Database) {
    transaction(db) {
        execInBatch(schemaSql(db))
    }
}



// this is a utility function for development only
fun createMissingTablesAndColumns(db: Database) {
    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(*tables)
    }
}

fun schemaSql(db: Database): List<String> {
    val statements = mutableListOf<String>()

    enums.forEach {
        statements.add(createEnumTypeSql(it))
    }

    statements.add(createKleinverbruikEnumTypeSql())

    transaction(db) {
        statements.addAll(gridConnectionSequence.createStatement())
        statements.addAll(SchemaUtils.createStatements(*tables))
    }

    return statements
}

fun echoSchemaSql(db: Database) {
    val sql = schemaSql(db).reduce { acc, s -> acc + ";\n" + s } + ";"
    println(sql)
}
