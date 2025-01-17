package com.zenmo.vallum

import com.zenmo.orm.companysurvey.ProjectRepository
import com.zenmo.orm.companysurvey.SurveyRepository
import com.zenmo.orm.companysurvey.createMockSurvey
import com.zenmo.orm.connectToPostgres
import com.zenmo.orm.createSchema
import com.zenmo.orm.getenv
import com.zenmo.orm.user.UserRepository
import com.zenmo.ztor.StopZtor
import com.zenmo.ztor.startTestServer
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@JvmOverloads
fun initZtor(port: Int = 8082): StopZtor {
    val db = connectToPostgres()
    val schema = Schema(db.connector().schema)
    transaction(db) {
        SchemaUtils.dropSchema(schema, cascade = true)
        SchemaUtils.createSchema(schema)
    }
    createSchema(db)

    val projectRepository = ProjectRepository(db)
    val surveyRepository = SurveyRepository(db)

    val projectName1 = "Waardkwartier"
    val projectName2 = "Hessenwiek"
    val projectId1 = projectRepository.saveNewProject(projectName1)
    val projectId2 = projectRepository.saveNewProject(projectName2)
    val userId = UUID.fromString(getenv("USER_ID"))

    // give user access to one project but not the other
    UserRepository(db).saveUser(userId, listOf(projectId1), "Service account test user")

    surveyRepository.save(createMockSurvey(projectName1))
    surveyRepository.save(createMockSurvey(projectName2))

    return startTestServer(port)
}


