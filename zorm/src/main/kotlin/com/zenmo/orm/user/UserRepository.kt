package com.zenmo.orm.user

import com.zenmo.orm.user.table.UserProjectTable
import com.zenmo.orm.user.table.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun getUserById(db: Database, id: UUID): User? {
    return null
}

fun saveUser(
    db: Database,
    userId: UUID,
    projectIds: List<UUID> = emptyList(),
    note: String = "",
) {
    transaction(db) {
        UserTable.insert {
            it[id] = userId
            it[UserTable.note] = note
        }

        UserProjectTable.batchInsert(projectIds) {
            this[UserProjectTable.projectId] = it
            this[UserProjectTable.userId] = userId
        }
    }
}
