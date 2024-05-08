package com.zenmo.ztor.plugins

import com.zenmo.orm.companysurvey.SurveyRepository
import com.zenmo.orm.connectToPostgres
import com.zenmo.orm.deeplink.DeeplinkRepository
import com.zenmo.ztor.deeplink.DeeplinkService
import com.zenmo.ztor.errorMessageToJson
import com.zenmo.ztor.user.UserSession
import com.zenmo.zummon.companysurvey.Survey
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.Database
import java.util.*

fun Application.configureDatabases(): Database {
    val db: Database = connectToPostgres()
    val surveyRepository = SurveyRepository(db)
    val deeplinkService = DeeplinkService(DeeplinkRepository(db))

    routing {
        // Create
        post("/company-survey") {
            val survey: Survey?
            try {
                survey = call.receive<Survey>()
            } catch (e: BadRequestException) {
                if (e.cause is JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest, errorMessageToJson(e.cause?.message))
                    return@post
                }
                call.respond(HttpStatusCode.BadRequest,  errorMessageToJson(e.message))
                return@post
            }

            val surveyId = surveyRepository.save(survey)

            val deeplink = deeplinkService.generateDeeplink(surveyId)

            call.respond(HttpStatusCode.Created, deeplink)
        }

        // fetch all
        get("/company-survey") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val userId = userSession.getUserId()

            val repository = SurveyRepository(db)
            val surveys = repository.getSurveysByUser(userId)

            call.respond(HttpStatusCode.OK, surveys)
        }

        // fetch single
        get("/company-survey/{surveyId}") {
            val surveyId = UUID.fromString(call.parameters["surveyId"])

            val deeplink = call.request.queryParameters.get("deeplink")
            val secret = call.request.queryParameters.get("secret")

            if (deeplink != null && secret != null) {
                val deeplinkId = UUID.fromString(deeplink)
                deeplinkService.assertValidDeeplink(surveyId, deeplinkId, secret)
                val survey = surveyRepository.getSurveyById(surveyId)
                if (survey == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respond(HttpStatusCode.OK, survey)
            }

            val userSession = call.sessions.get<UserSession>()
            if (userSession == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val userId = userSession.getUserId()

            val survey = surveyRepository.getSurveyByIdWithUserAccessCheck(surveyId, userId)
            if (survey == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(HttpStatusCode.OK, survey)
        }

        delete("/company-survey/{surveyId}") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }

            val surveyId = UUID.fromString(call.parameters["surveyId"])
            val userId = userSession.getUserId()

            surveyRepository.deleteSurveyById(surveyId, userId)

            call.respond(HttpStatusCode.OK)
        }

        // generate deeplink
        post("/company-survey/{surveyId}/deeplink") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val surveyId = UUID.fromString(call.parameters["surveyId"])

            val survey = surveyRepository.getSurveyByIdWithUserAccessCheck(surveyId, userSession.getUserId())
            if (survey == null) {
                // User may not have access to this project
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val deeplink = deeplinkService.generateDeeplink(surveyId)

            call.respond(HttpStatusCode.Created, deeplink)
        }
    }

    return db
}
