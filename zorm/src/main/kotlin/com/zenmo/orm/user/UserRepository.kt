package com.zenmo.orm.user

import com.zenmo.zummon.User
import com.zenmo.orm.companysurvey.ProjectRepository
import com.zenmo.orm.user.table.UserProjectTable
import com.zenmo.orm.user.table.UserTable
import com.zenmo.orm.companysurvey.table.ProjectTable
import com.zenmo.zummon.companysurvey.Project
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

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
    
    fun getUserAndProjects(userId: UUID): User {
        return getUsersAndProjects(
            (UserTable.id eq userId)
        ).first()
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

    fun getUserById(id: UUID): User {
        return getUsers(
            (UserTable.id eq id)
        ).first()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun save(user: User) {
        transaction(db) {
            UserTable.upsertReturning {
                it[id] = user.id.toJavaUuid()
                it[note] = user.note
                it[isAdmin] = user.isAdmin
            }.map {
                hydrateUser(it)
            }.first()
    
           // db project ids
            val currentProjectIds = UserProjectTable
            .selectAll()
            .where { UserProjectTable.userId eq user.id.toJavaUuid() }
            .map { it[UserProjectTable.projectId] }
            .toSet()

            // coming project ids
            val newProjectIds = user.projects.map { it.id }.toSet()

            // add and remove projects
            val projectsToAdd = newProjectIds - currentProjectIds
            val projectsToRemove = currentProjectIds - newProjectIds

            // insert new
            if (projectsToAdd.isNotEmpty()) {
                UserProjectTable.batchInsert(projectsToAdd) { projectId ->
                    this[UserProjectTable.projectId] = projectId
                    this[UserProjectTable.userId] = user.id.toJavaUuid()
                }
            }

            // remove old
            if (projectsToRemove.isNotEmpty()) {
                UserProjectTable.deleteWhere {
                    (UserProjectTable.userId eq user.id.toJavaUuid()) and
                    (UserProjectTable.projectId inList projectsToRemove)
                }
            }
        }
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

    fun isAdmin(userId: UUID): Boolean {
        val user = try {
            getUserById(userId)
        } catch (e: NoSuchElementException) {
            null
        }
    
        return user?.isAdmin ?: false
    }
    
    protected fun hydrateUser(row: ResultRow): User {
        return User(
            id = row[UserTable.id].toKotlinUuid(),
            note = row[UserTable.note],
            isAdmin = row[UserTable.isAdmin],
            projects = emptyList(), // data from different table
        )
    }
}


