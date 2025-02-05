package com.zenmo.ztor.plugins

import com.zenmo.ztor.user.UserSession
import com.zenmo.ztor.user.UserInfo
import com.zenmo.ztor.user.decodeAccessToken
import io.ktor.client.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.html.*
import io.ktor.server.plugins.forwardedheaders.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.p
import com.zenmo.orm.connectToPostgres
import org.jetbrains.exposed.sql.Database
import com.zenmo.orm.user.UserRepository

val applicationHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

/**
 * After https://ktor.io/docs/server-oauth.html
 */
fun Application.configureAuthentication() {
    val db: Database = connectToPostgres()
    val userRepository = UserRepository(db)

    // This reads the X-Forwarded-Proto header.
    // This allows us to set the secure cookie below.
    install(XForwardedHeaders)
    install(Sessions) {
        cookie<UserSession>("user_session", SessionStorageMemory()) {
            if (System.getenv("BASE_URL").startsWith("https")) {
                // The frontend and backend may be hosted on different domains
                cookie.extensions["SameSite"] = "None"
                cookie.secure = true
            }
        }
    }
    val redirects = mutableMapOf<String, String>()
    install(Authentication) {
        oauth("keycloak") {
            // We could make a call here to register the base url in Ory Hydra
            // for the test environment which has a dynamic domain.
            urlProvider = { System.getenv("BASE_URL") + "/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "keycloak",
                    // https://keycloak.zenmo.com/realms/zenmo/.well-known/openid-configuration
                    authorizeUrl = "https://keycloak.zenmo.com/realms/zenmo/protocol/openid-connect/auth",
                    accessTokenUrl = "https://keycloak.zenmo.com/realms/zenmo/protocol/openid-connect/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("OAUTH_CLIENT_ID"),
                    clientSecret = System.getenv("OAUTH_CLIENT_SECRET"),
                    defaultScopes = listOf("openid", "profile", "email"),
                    onStateCreated = { call, state ->
                        //saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            client = applicationHttpClient
        }
    }
    routing {
        authenticate("keycloak") {
            get("/login") {
                // Redirects to 'authorizeUrl' automatically
            }

            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                if (currentPrincipal == null) {
                    call.respondText("No principal")
                    return@get
                }
                // redirects home if the url is not found before authorization
                currentPrincipal.let { principal ->
                    principal.state?.let { state ->
                        call.sessions.set(UserSession(state, principal.accessToken))
                        redirects[state]?.let { redirect ->
                            call.respondRedirect(redirect)
                            return@get
                        }
                    }
                }
                call.respondRedirect("/home")
            }
        }
        get("/user-info") {
            val userSession: UserSession? = call.sessions.get()

            if (userSession == null) {
                call.respondText("Not logged in", status=HttpStatusCode.Unauthorized)
            } else {
                val userId = userSession.getUserId()
                val isAdmin = userRepository.isAdmin(userId)

                val response = UserInfo(
                    isAdmin = isAdmin,
                    decodedAccessToken = userSession.getDecodedAccessToken()
                )
                call.respond(response)
            }
        }
        get("/home") {
            val userSession: UserSession? = call.sessions.get()
            if (userSession != null) {
                val token = decodeAccessToken(userSession.token)
                call.respondText("Hello, ${token.preferred_username}! Welcome home!")
            } else {
                call.respondHtml {
                    body {
                        p {
                            a("/login") { +"Login with Keycloak" }
                        }
                    }
                }
            }
        }
    }
}
