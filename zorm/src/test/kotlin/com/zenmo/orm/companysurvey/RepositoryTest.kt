package com.zenmo.orm.companysurvey

import com.zenmo.orm.companysurvey.table.CompanySurveyTable
import com.zenmo.orm.createSchema
import com.zenmo.orm.connectToPostgres
import com.zenmo.orm.user.saveUser
import com.zenmo.zummon.companysurvey.Survey
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.BeforeClass
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class RepositoryTest {
    companion object {
        /**
         * Drop and create database before running tests.
         */
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            val db = connectToPostgres()
            val schema = Schema(db.connector().schema)
            transaction(db) {
                SchemaUtils.dropSchema(schema, cascade = true)
                SchemaUtils.createSchema(schema)
            }

            createSchema(db)
        }
    }

    @Test
    fun testSaveMinimalSurvey() {
        val db = connectToPostgres()
        val projectRepository = ProjectRepository(db)
        projectRepository.saveNewProject("Project2")
        val repo = SurveyRepository(db)
        val survey = Survey(
            companyName = "Zenmo",
            zenmoProject = "Project2",
            personName = "John Doe",
            email = "john@example.com",
            addresses = emptyList(),
        )
        repo.save(survey)
        val storedSurveys = repo.getSurveys(CompanySurveyTable.id eq survey.id)
        assertEquals(1, storedSurveys.size)
        assertEquals(survey, storedSurveys.first())
    }

    @Test
    fun testSaveWithGridConnections() {
        val db = connectToPostgres()
        val projectName = "WessenHoort"
        ProjectRepository(db).saveNewProject(projectName)
        val repo = SurveyRepository(db)
        val survey = createMockSurvey(projectName)

        repo.save(survey)
        val storedSurveys = repo.getSurveys(CompanySurveyTable.id eq survey.id)
        assertEquals(1, storedSurveys.size)
        val storedSurvey = wipeSequence(storedSurveys.first())

        assertEquals(survey, storedSurvey)
        val gasTimeStep = storedSurvey.addresses.single().gridConnections.single().naturalGas.hourlyDelivery_m3?.timeStep
        assertNotNull(gasTimeStep)
        assertEquals(2, gasTimeStep.inWholeHours)
    }

    @Test
    fun testUserCantAccessSurveyInOtherProject() {
        val db = connectToPostgres()

        val ownProjectName = "Middelkaap"
        val projectId = ProjectRepository(db).saveNewProject(ownProjectName)

        val userId = UUID.randomUUID()
        saveUser(db, userId, listOf(projectId))

        val otherProjectName = "Bovenkaap"
        ProjectRepository(db).saveNewProject(otherProjectName)

        val repo = SurveyRepository(db)
        repo.save(Survey(
            companyName = "Zenmo",
            zenmoProject = ownProjectName,
            personName = "John Doe",
            email = "john@example.com",
            addresses = emptyList(),
        ))
        repo.save(Survey(
            companyName = "Zenmo",
            zenmoProject = otherProjectName,
            personName = "John Doe",
            email = "john@example.com",
            addresses = emptyList(),
        ))

        val surveys = repo.getSurveysByUser(userId)
        assertEquals(1, surveys.size)
        assertEquals("Middelkaap", surveys[0].zenmoProject)
    }

    @Test
    fun testSequenceIsSameAfterUpsert() {
        val db = connectToPostgres()

        val projectRepository = ProjectRepository(db)
        projectRepository.saveNewProject("Project")

        val repo = SurveyRepository(db)
        val survey = createMockSurvey("Project")
        repo.save(survey)

        val storedSurvey1 = repo.getSurveyById(survey.id)!!

        repo.save(storedSurvey1)

        val storedSurvey2 = repo.getSurveyById(storedSurvey1.id)!!

        assertEquals(
            storedSurvey1.addresses.first().gridConnections.first().sequence,
            storedSurvey2.addresses.first().gridConnections.first().sequence,
        )
    }

    @Test
    @OptIn(ExperimentalUuidApi::class)
    fun testCreatorIsSetOnCreateButNotOnEdit() {
        val db = connectToPostgres()
        val jaapId = UUID.fromString("bc0ea106-3bac-452e-ae39-5c1b29782001")
        saveUser(db = db, userId = jaapId, note = "Jaap")
        val pietId = UUID.fromString("bc0ea106-3bac-452e-ae39-5c1b29782002")
        saveUser(db = db, userId = pietId, note = "Piet")

        val repo = SurveyRepository(db)
        // create survey
        val surveyId = repo.save(createMockSurvey(), jaapId)

        val surveyLoadedAfterCreate = repo.getSurveyById(surveyId)
        val createdBy = surveyLoadedAfterCreate?.createdBy
        assertNotNull(createdBy)
        assertEquals("Jaap", createdBy.note)
        assertEquals(jaapId.toKotlinUuid(), createdBy.id)

        // edit survey
        repo.save(surveyLoadedAfterCreate, pietId)
        val surveyLoadedAfterEdit = repo.getSurveyById(surveyId)
        val createdBy2 = surveyLoadedAfterEdit?.createdBy
        assertNotNull(createdBy2)
        assertEquals("Jaap", createdBy2.note)
        assertEquals(jaapId.toKotlinUuid(), createdBy2.id)
    }

    private fun wipeSequence(survey: Survey)
    = survey.copy(
        addresses = survey.addresses.map {
            it.copy(
                gridConnections = it.gridConnections.map {
                    it.copy(
                        sequence = null,
                    )
                }
            )
        },
    )
}
