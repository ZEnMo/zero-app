package com.zenmo.zummon

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zenmo.zummon.BenasherUuidSerializer
import com.zenmo.zummon.companysurvey.Project
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * This object is intended to be enriched with Keycloak data.
 * It is not purely an ORM object.
 */
@Serializable
@JsExport
data class User
constructor(
    @Serializable(with = BenasherUuidSerializer::class)
    val id: Uuid = uuid4(),
    val note: String,
    val projects: List<Project> = emptyList(),
    val isAdmin: Boolean = false
)

@JsExport
fun usersFromJson(json: String): Array<User> {
    return kotlinx.serialization.json.Json.decodeFromString<Array<User>>(json)
}

@JsExport
fun userFromJson(json: String): User {
    return kotlinx.serialization.json.Json.decodeFromString<User>(json)
}
