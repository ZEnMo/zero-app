package com.zenmo.orm.user

import com.zenmo.orm.cleanDb
import com.zenmo.orm.companysurvey.ProjectRepository
import com.zenmo.orm.connectToPostgres
import com.zenmo.orm.user.table.UserProjectTable
import com.zenmo.orm.user.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.*
import kotlin.uuid.toKotlinUuid

class UserRepositoryTest {
    val db: Database = connectToPostgres()
    val userRepository = UserRepository(db)
    val projectRepository = ProjectRepository(db)

    @BeforeTest
    fun cleanUp() {
        cleanDb(db)
    }

    @Test
    fun `test saveUser inserts user with given id and note`() {
        val userId = UUID.randomUUID()
        val note = "This is a test note"
        userRepository.saveUser(userId, note = note)
        val result = userRepository.getUserById(userId)
        assertNotNull(result)
        assertEquals(userId.toKotlinUuid(), result.id)
        assertEquals(note, result.note)
    }

    @Test
    fun `test saveUser inserts user with empty note by default`() {
        val userId = UUID.randomUUID()
        userRepository.saveUser(userId)

        val result = userRepository.getUserById(userId)
        assertNotNull(result)
        assertEquals(userId.toKotlinUuid(), result.id)
        assertEquals("", result.note)
    }

    @Test
    fun `test saveUser inserts projects for given user`() {
        val userId = UUID.randomUUID()

        val tmpProjectName = "testTMP"
        val projectId = projectRepository.saveNewProject(tmpProjectName)
        userRepository.saveUser(userId, listOf(projectId))

        transaction(db) {
            val result = UserProjectTable.selectAll()
                .where { UserProjectTable.userId eq userId and (UserProjectTable.projectId eq projectId) }.singleOrNull()
            assertNotNull(result)
            assertEquals(userId, result[UserProjectTable.userId])
            assertEquals(projectId, result[UserProjectTable.projectId])
        }
    }

    @Test
    fun `test saveUser inserts no projects if no projectIds are given`() {
        val userId = UUID.randomUUID()
        userRepository.saveUser(userId)

        transaction(db) {
            val results = UserProjectTable.selectAll().where { UserProjectTable.userId eq userId }.toList()
            assertTrue(results.isEmpty())
        }
    }

    @Test
    fun `test getUsers returns all users when no filter is provided`() {
        for (i in 1..5) {
            userRepository.saveUser(UUID.randomUUID(), note = "User $i note")
        }
        val users = userRepository.getUsers()
        assertEquals(5, users.size)
    }

    @Test
    fun `test getUsers filters users based on given filter`() {
        val userId = UUID.randomUUID()
        userRepository.saveUser(userId, note = "Specific user note")
        for (i in 1..4) {
            userRepository.saveUser(UUID.randomUUID(), note = "User $i note")
        }
        val users = userRepository.getUsers((UserTable.note eq "Specific user note"))
        assertEquals(1, users.size)
        assertEquals(userId.toKotlinUuid(), users.first().id)
    }

    @Test
    fun `test getUsersAndProjects loads projects`() {
        val userId = UUID.randomUUID()
        val project1Id = projectRepository.saveNewProject("Project 1")
        val project2Id = projectRepository.saveNewProject("Project 2")

        // Save user with projects
        userRepository.saveUser(userId, listOf(project1Id, project2Id), "Test Note")

        // Retrieve users
        val users = userRepository.getUsersAndProjects(( UserTable.id eq userId ))
        val user = users.firstOrNull()

        // Assertions
        assertNotNull(user)
        assertEquals(userId.toKotlinUuid(), user.id)
        assertEquals("Test Note", user.note)
        assertEquals(2, user.projects.size)

        user.projects.forEach { project ->
            when (project.id) {
                project1Id -> assertEquals("Project 1", project.name)
                project2Id -> assertEquals("Project 2", project.name)
                else -> fail("Unexpected project ID ${project.id}")
            }
        }
    }

    @Test
    fun `test getUsersAndProjects handles users without projects`() {
        val userId = UUID.randomUUID()
        // Save user without projects
        userRepository.saveUser(userId, note = "User without projects")

        // Retrieve users
        val users = userRepository.getUsersAndProjects()
        val user = users.find { it.id == userId.toKotlinUuid() }

        // Assertions
        assertNotNull(user)
        assertEquals(userId.toKotlinUuid(), user?.id)
        assertEquals("User without projects", user?.note)
        assertTrue(user?.projects?.isEmpty() == true)
    }
    

    @Test
    fun `test deleteUserById removes user correctly`() {
        val userId = UUID.randomUUID()
        userRepository.saveUser(userId, note = "Delete Test User")

        val userBeforeDelete = userRepository.getUserById(userId)
        assertNotNull(userBeforeDelete)

        userRepository.deleteUserById(userId)
        assertFailsWith(NoSuchElementException::class) {
            userRepository.getUserById(userId)
        }
    }

    @Test
    fun `test deleteUserById removes user projects as well`() {
        val userId = UUID.randomUUID()
        userRepository.saveUser(userId, note = "Delete Test User with Projects")
        val projectIds = listOf(projectRepository.saveNewProject("Test Project"))
        userRepository.saveUser(userId, projectIds)

        val userProjectsBeforeDelete = transaction(db) {
            UserProjectTable.selectAll().where { UserProjectTable.userId eq userId }.toList()
        }
        assertTrue(userProjectsBeforeDelete.isNotEmpty())

        userRepository.deleteUserById(userId)

        val userProjectsAfterDelete = transaction(db) {
            UserProjectTable.selectAll().where { UserProjectTable.userId eq userId }.toList()
        }
        assertTrue(userProjectsAfterDelete.isEmpty())
    }

    @Test
    fun `test deleteUserById does nothing if user does not exist`() {
        val nonExistentUserId = UUID.randomUUID()

        userRepository.deleteUserById(nonExistentUserId)
        // If no exceptions are thrown, the test passes as there's no user to delete
        assertTrue(true)
    }
}
