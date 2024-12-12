package com.zenmo.orm.user

import com.zenmo.orm.companysurvey.ProjectRepository
import com.zenmo.orm.user.table.UserProjectTable
import com.zenmo.orm.user.table.UserTable
import com.zenmo.orm.companysurvey.table.ProjectTable
import com.zenmo.zummon.companysurvey.Project
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

class UserRepository(
    private val db: Database,
) {
    fun getUsers(filter: Op<Boolean> = Op.TRUE): List<User> {
        return transaction(db) {
            UserTable
                .selectAll()
                .where{
                    filter
                }.mapNotNull {
                    hydrateUser(it)
                }
        }
    }

    fun getUsersAndProjects(filter: Op<Boolean> = Op.TRUE): List<User> {
        return transaction(db) {
            val projectRepo = ProjectRepository(db)

            UserTable
            .join(UserProjectTable, JoinType.LEFT, UserTable.id, UserProjectTable.userId)
            .join(ProjectTable, JoinType.LEFT, UserProjectTable.projectId, ProjectTable.id)
                .selectAll()
                .where{
                    filter
                }
                .map { row ->
                    val user = hydrateUser(row)
                    val project = if (!row[ProjectTable.name].isNullOrEmpty()) {
                        projectRepo.hydrateProject(row)
                    } else null

                    // Safely return the user-project pair
                    if (project != null) user to project else user to null
                }
                .groupBy({ it.first }, { it.second })
                .map { (user, projects) ->
                    user.copy(projects = projects.filterNotNull().distinct())
                }
        }
    }

    fun getUserById(id: UUID): User? {
        return getUsers(
            (UserTable.id eq id)
        ).firstOrNull()
    }

    fun saveUser(
        userId: UUID,
        projectIds: List<UUID> = emptyList(),
        note: String = "",
    ) {
        transaction(db) {
            UserTable.upsert {
                it[id] = userId
                it[UserTable.note] = note
            }

            UserProjectTable.batchInsert(projectIds) {
                this[UserProjectTable.projectId] = it
                this[UserProjectTable.userId] = userId
            }
        }
    }

    fun deleteUserById(userId: UUID) {
        transaction(db) {
            UserProjectTable.deleteWhere {
                UserProjectTable.userId eq userId
            }
            UserTable.deleteWhere {
                id eq userId
            }
        }
    }

    protected fun hydrateUser(row: ResultRow): User {
        return User(
            id = row[UserTable.id],
            note = row[UserTable.note],
            projects = emptyList(), // data from different table
        )
    }
}


