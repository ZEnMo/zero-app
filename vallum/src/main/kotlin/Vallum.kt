package com.zenmo.vallum

import com.zenmo.zummon.companysurvey.Survey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class Vallum
@JvmOverloads
constructor(
    private val clientId: String,
    private val clientSecret: String,
    private val baseUrl: String = "https://ztor.zero.zenmo.com",
    private val tokenUrl: String = "https://keycloak.zenmo.com/realms/zenmo/protocol/openid-connect/token",
) {
    fun getHessenpoortSurveys(): List<Survey> =
        getSurveysByProject("Hessenpoort")

    @JvmOverloads
    fun getSurveysByProject(project: String, includeInSimulation: Boolean? = true): List<Survey> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        return runBlocking {
            val accessToken = getAccessToken(client)
            client.get(baseUrl.trimEnd('/') + "/company-surveys") {
                parameter("project", project)
                parameter("includeInSimulation", includeInSimulation)
                headers {
                    append("Authorization", "Bearer $accessToken")
                }
            }.body<List<Survey>>()
        }
    }

    private suspend fun getAccessToken(client: HttpClient): String {
         val response = client.submitForm(
            url = tokenUrl,
            formParameters = parameters {
                append("grant_type", "client_credentials")
                append("client_id", clientId)
                append("client_secret", clientSecret)
            }
        )

        val tokenInfo = response.body<TokenInfo>()
        return tokenInfo.accessToken
    }
}

@Serializable
data class TokenInfo(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
)
