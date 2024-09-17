package com.zenmo.orm.companysurvey

import com.zenmo.orm.companysurvey.table.ProjectTable
import com.zenmo.zummon.companysurvey.Project
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class ProjectRepository(
    val db: Database
) {
    fun saveNewProject(name: String): UUID =
        transaction(db) {
            ProjectTable.insertReturning(listOf(ProjectTable.id)) {
                it[ProjectTable.name] = name
            }.first()[ProjectTable.id]
        }

    @OptIn(ExperimentalUuidApi::class)
    fun getProjectByEnergiekeRegioId(energiekeRegioId: Int): Project =
        transaction(db) {
            ProjectTable.selectAll()
                .map {
                    Project(
                        it[ProjectTable.id].toKotlinUuid(),
                        it[ProjectTable.name],
                        it[ProjectTable.energiekeRegioId],
                        it[ProjectTable.buurtCodes],
                    )
                }
                .first()
        }
}
