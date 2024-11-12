package com.zenmo.orm.companysurvey

import com.zenmo.orm.companysurvey.table.ProjectTable
import com.zenmo.orm.user.table.UserProjectTable
import com.zenmo.zummon.companysurvey.Project
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.anyFrom
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
                .where {
                    ProjectTable.energiekeRegioId eq energiekeRegioId
                }
                .map {
                    Project(
                        it[ProjectTable.id].toKotlinUuid(),
                        it[ProjectTable.name],
                        it[ProjectTable.energiekeRegioId],
                        it[ProjectTable.buurtCodes],
                    )
                }
                .single()
        }

    @OptIn(ExperimentalUuidApi::class)
    fun getProjects(userId: UUID): List<Project> =
        transaction(db) {
            ProjectTable.selectAll()
                .where {
                    ProjectTable.id eq anyFrom(
                        UserProjectTable.select(UserProjectTable.projectId)
                            .where { UserProjectTable.userId eq userId }
                    )
                }
                .map {
                    Project(
                        it[ProjectTable.id].toKotlinUuid(),
                        it[ProjectTable.name],
                        it[ProjectTable.energiekeRegioId],
                        it[ProjectTable.buurtCodes],
                    )
                }
        }
}
