package com.zenmo.orm

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun cleanDb(db: Database) {
    val schema = Schema(db.connector().schema)
    transaction(db) {
        SchemaUtils.dropSchema(schema, cascade = true)
        SchemaUtils.createSchema(schema)
    }
    createSchema(db)
}
