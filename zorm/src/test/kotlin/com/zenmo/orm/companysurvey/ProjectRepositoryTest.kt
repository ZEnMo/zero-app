package com.zenmo.orm.companysurvey

import com.zenmo.orm.cleanDb
import com.zenmo.orm.companysurvey.table.ProjectTable
import com.zenmo.orm.connectToPostgres
import com.zenmo.orm.user.UserRepository
import com.zenmo.zummon.companysurvey.Project
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import kotlin.test.*
import java.util.UUID

class ProjectRepositoryTest {
    val db: Database = connectToPostgres()
    val projectRepository = ProjectRepository(db)
    val userRepository = UserRepository(db)

    @BeforeTest
    fun cleanUp() {
        cleanDb(db)
    }

    @Test
    fun testSaveProject() {
        val project = Project(
            name = "Updated Project",
            energiekeRegioId = 456,
            buurtCodes = listOf("B001", "B002")
        )
        val savedProject = projectRepository.save(project)

        transaction(db) {
            val result = ProjectTable.selectAll()
                .where { ProjectTable.id eq savedProject.id }
                .singleOrNull()

            assertNotNull(result)
            assertEquals(savedProject.name, result[ProjectTable.name])
            assertEquals(savedProject.energiekeRegioId, result[ProjectTable.energiekeRegioId])
            assertEquals(savedProject.buurtCodes, result[ProjectTable.buurtCodes])
        }
    }

    @Test
    fun testGetAllProjects() {
        val firstProject = projectRepository.save(Project(
            name = "Testing Project 1",
            energiekeRegioId = 458,
        ))
        val lastProject = projectRepository.save(Project(
            name = "Testing Project 2",
            energiekeRegioId = 459,
        ))
        val projects = projectRepository.getProjects()
        assertEquals(2, projects.size)
        assertEquals(lastProject.id, projects.last().id)
        assertEquals(lastProject.name, projects.last().name)
        assertEquals(459, projects.last().energiekeRegioId)
    }

    @Test
    fun testSaveNewProject() {
        val projectName = "New Test Project"
        projectRepository.saveNewProject(projectName)

        transaction(db) {
            val result = ProjectTable.selectAll().where{ ProjectTable.name eq projectName }.singleOrNull()
            assertNotNull(result)
            assertEquals(projectName, result[ProjectTable.name])
        }
    }

    @Test
    fun testGetProjectByEnergiekeRegioId() {
        val project = Project(
            name = "Test Project",
            energiekeRegioId = 123,
        )

        projectRepository.save(project)
        val response = projectRepository.getProjectByEnergiekeRegioId(123)
        assertNotNull(project)
        assertEquals(project.name, response.name)
        assertEquals(project.energiekeRegioId, response.energiekeRegioId)
    }

    @Test
    fun testGetProjectsByUserId() {
        val userId = UUID.randomUUID()
        val projectName1 = "User's Project 1"
        val projectName2 = "User's Project 2"

        val projectId1 = projectRepository.saveNewProject(projectName1)
        val projectId2 = projectRepository.saveNewProject(projectName2)
        userRepository.saveUser(userId, listOf(projectId1, projectId2))

        val projects = projectRepository.getProjectsByUserId(userId)
        assertEquals(2, projects.size)
        assertTrue(projects.any { it.name == projectName1 })
        assertTrue(projects.any { it.name == projectName2 })
    }

    @Test
    fun testUserCantAccessOtherProjects() {
        val ownProjectName = "Middelkaap"
        val otherProjectName = "Bovenkaap"
        val projectId = projectRepository.saveNewProject(ownProjectName)
        val projectId2 = projectRepository.saveNewProject(otherProjectName)

        val userId = UUID.randomUUID()
        userRepository.saveUser(userId, listOf(projectId))
        val userId2 = UUID.randomUUID()
        userRepository.saveUser(userId2, listOf(projectId2))

        val projects = projectRepository.getProjectsByUserId(userId)
        assertEquals(1, projects.size)
        assertEquals("Middelkaap", projects[0].name)
    }

    @Test
    fun testDeleteProject() {
        val projectName = "Project to Delete"
        val projectId = projectRepository.saveNewProject(projectName)
        val deleteResult = projectRepository.deleteProject(projectId)

        // Verify that the project is successfully deleted
        assertTrue(deleteResult, "Project should be deleted successfully")

        // Verify that the project does not exist in the database anymore
        transaction(db) {
            val result = ProjectTable.selectAll().where { ProjectTable.id eq projectId }.singleOrNull()
            assertNull(result, "Deleted project should not be found in the database")
        }

        // Try to delete a non-existent project
        val nonExistentProjectId = UUID.randomUUID()
        val deleteNonExistentResult = projectRepository.deleteProject(nonExistentProjectId)

        // Verify that deletion of a non-existent project returns false
        assertFalse(deleteNonExistentResult, "Deletion of non-existent project should return false")
    }

    @Test
    fun testUpdateProject() {
        val originalProject = Project(
            name = "Initial Project",
            energiekeRegioId = 789,
            buurtCodes = listOf("B003")
        )
        val savedProject = projectRepository.save(originalProject)

        // Update project details
        val updatedProject = savedProject.copy(
            name = "Updated Project Name",
            energiekeRegioId = 111,
            buurtCodes = listOf("B003", "B004")
        )
        var response = projectRepository.save(updatedProject)
        var retrievedProject = projectRepository.getProjectById(savedProject.id)

        assertNotNull(updatedProject)
        assertEquals(originalProject.id, response.id)
        assertEquals("Updated Project Name", response.name)
        assertEquals("Updated Project Name", retrievedProject?.name)
        assertEquals("Updated Project Name", updatedProject.name)
        assertEquals(111, updatedProject.energiekeRegioId)
        assertEquals(listOf("B003", "B004"), updatedProject.buurtCodes)
    }
}
