package com.zenmo.orm.companysurvey

import com.zenmo.orm.cleanDb
import com.zenmo.orm.companysurvey.table.CompanySurveyTable
import com.zenmo.orm.connectToPostgres
import com.zenmo.orm.user.UserRepository
import com.zenmo.zummon.companysurvey.Survey
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class SurveyRepositoryTest {
    val db: Database = connectToPostgres()
    val surveyRepository = SurveyRepository(db)
    val projectRepository = ProjectRepository(db)
    val userRepository = UserRepository(db)

    @BeforeTest
    fun cleanUp() {
        cleanDb(db)
        projectRepository.saveNewProject("Project")
    }

    @Test
    fun testSaveMinimalSurvey() {
        projectRepository.saveNewProject("Project2")
        val survey = Survey(
            companyName = "Zenmo",
            zenmoProject = "Project2",
            personName = "John Doe",
            email = "john@example.com",
            addresses = emptyList(),
        )
        surveyRepository.save(survey)
        val storedSurveys = surveyRepository.getSurveys(CompanySurveyTable.id eq survey.id)
        assertEquals(1, storedSurveys.size)
        assertEquals(survey, storedSurveys.first())
    }

    @Test
    fun testSaveWithGridConnections() {
        val projectName = "WessenHoort"
        projectRepository.saveNewProject(projectName)
        val survey = createMockSurvey(projectName)

        surveyRepository.save(survey)
        val storedSurveys = surveyRepository.getSurveys(CompanySurveyTable.id eq survey.id)
        assertEquals(1, storedSurveys.size)
        val storedSurvey = wipeSequence(storedSurveys.first())

        assertEquals(survey, storedSurvey)
        val gasTimeStep = storedSurvey.addresses.single().gridConnections.single().naturalGas.hourlyDelivery_m3?.timeStep
        assertNotNull(gasTimeStep)
        assertEquals(2, gasTimeStep.inWholeHours)
    }

    @Test
    fun testUserCantAccessSurveyInOtherProject() {
        val ownProjectName = "Middelkaap"
        val projectId = projectRepository.saveNewProject(ownProjectName)

        val userId = UUID.randomUUID()
        userRepository.saveUser(userId, listOf(projectId))

        val otherProjectName = "Bovenkaap"
        projectRepository.saveNewProject(otherProjectName)

        surveyRepository.save(Survey(
            companyName = "Zenmo",
            zenmoProject = ownProjectName,
            personName = "John Doe",
            email = "john@example.com",
            addresses = emptyList(),
        ))
        surveyRepository.save(Survey(
            companyName = "Zenmo",
            zenmoProject = otherProjectName,
            personName = "John Doe",
            email = "john@example.com",
            addresses = emptyList(),
        ))

        val surveys = surveyRepository.getSurveysByUser(userId)
        assertEquals(1, surveys.size)
        assertEquals("Middelkaap", surveys[0].zenmoProject)
    }

    @Test
    fun testSequenceIsSameAfterUpsert() {
        val survey = createMockSurvey("Project")
        surveyRepository.save(survey)

        val storedSurvey1 = surveyRepository.getSurveyById(survey.id)!!

        surveyRepository.save(storedSurvey1)

        val storedSurvey2 = surveyRepository.getSurveyById(storedSurvey1.id)!!

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
        userRepository.saveUser(userId = jaapId, note = "Jaap")
        val pietId = UUID.fromString("bc0ea106-3bac-452e-ae39-5c1b29782002")
        userRepository.saveUser(userId = pietId, note = "Piet")

        // create survey
        val surveyId = surveyRepository.save(createMockSurvey(), jaapId)

        val surveyLoadedAfterCreate = surveyRepository.getSurveyById(surveyId)
        val createdBy = surveyLoadedAfterCreate?.createdBy
        assertNotNull(createdBy)
        assertEquals("Jaap", createdBy.note)
        assertEquals(jaapId.toKotlinUuid(), createdBy.id)

        // edit survey
        surveyRepository.save(surveyLoadedAfterCreate, pietId)
        val surveyLoadedAfterEdit = surveyRepository.getSurveyById(surveyId)
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
