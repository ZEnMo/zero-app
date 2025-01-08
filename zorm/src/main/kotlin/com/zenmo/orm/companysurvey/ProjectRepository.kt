package com.zenmo.orm.companysurvey

import com.zenmo.zummon.companysurvey.Project
import com.zenmo.orm.user.table.UserProjectTable
import com.zenmo.orm.user.table.UserTable

import com.zenmo.orm.companysurvey.table.ProjectTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

class ProjectRepository(
    val db: Database
) {
    fun getProjects(filter: Op<Boolean> = Op.TRUE): List<Project> {
        return transaction(db) {
            ProjectTable
                .selectAll()
                .where{
                    filter
                }.mapNotNull {
                    hydrateProject(it)
                }
        }
    }

    fun getProjectById(id: UUID): Project {
        return getProjects(
            (ProjectTable.id eq id)
        ).first()
    }

    fun getProjectByUserId(userId: UUID, projectId: UUID): Project {
        return getProjects(
            (ProjectTable.id eq projectId) and
                  (ProjectTable.id eq anyFrom(
                    UserProjectTable.select(UserProjectTable.projectId)
                        .where { UserProjectTable.userId eq userId }
                    ))
        ).first()
    }

    fun getProjectsByUserId(userId: UUID): List<Project> =
        transaction(db) {
             getProjects(
                ( ProjectTable.id eq anyFrom(
                    UserProjectTable.select(UserProjectTable.projectId)
                        .where { UserProjectTable.userId eq userId }
                ))
            )
        }

    fun deleteProject(projectId: UUID): Boolean {
        return transaction(db) {
            ProjectTable.deleteWhere { ProjectTable.id eq projectId } > 0
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun save(project: Project): Project {
        return saveProject(project)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveToUser(project: Project, userId: UUID) {
        transaction(db) {
            val savedProject = saveProject(project)
            UserProjectTable.insert {
                it[projectId] = UUID.fromString(savedProject.id.toString())
                it[UserProjectTable.userId] = userId
            }
        }
    }

    private fun saveProject(project: Project): Project {
        return transaction(db) {
            ProjectTable.upsertReturning() {
                it[id] = UUID.fromString(project.id.toString())
                it[name] = project.name
                it[energiekeRegioId] = project.energiekeRegioId
                it[buurtCodes] = project.buurtCodes
            }.map {
                hydrateProject(it)
            }.first()
        }
    }

    fun saveNewProject(name: String): UUID =
        transaction(db) {
           ProjectTable.insertReturning(listOf(ProjectTable.id)) {
                it[ProjectTable.name] = name
            }.first()[ProjectTable.id]
        }

    fun getProjectByEnergiekeRegioId(energiekeRegioId: Int): Project =
        transaction(db) {
            getProjects(
                ProjectTable.energiekeRegioId eq energiekeRegioId
            )
        }.first()

    fun getBuurtCodesByProjectName(projectName: String): List<String> =
        transaction(db) {
            ProjectTable.select(ProjectTable.buurtCodes)
                .where(ProjectTable.name eq projectName)
                .single()[ProjectTable.buurtCodes]
        }

    @OptIn(ExperimentalUuidApi::class)
    fun hydrateProject(row: ResultRow): Project {
        return Project(
            id = row[ProjectTable.id],
            name = row[ProjectTable.name],
            energiekeRegioId = row[ProjectTable.energiekeRegioId],
            buurtCodes = row[ProjectTable.buurtCodes]
        )
    }
}
