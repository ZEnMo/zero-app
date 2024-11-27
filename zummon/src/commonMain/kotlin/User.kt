package com.zenmo.zummon

import com.zenmo.zummon.companysurvey.Project
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

/**
 * This object is intended to be enriched with Keycloak data.
 * It is not purely an ORM object.
 */
@Serializable
@JsExport
data class User(
    @Serializable(with = KotlinUuidSerializer::class)
    val id: Uuid,
    val note: String,
    val projects: List<Project> = emptyList()
)
