package com.zenmo.orm.user

import com.zenmo.zummon.companysurvey.Project
import java.util.UUID

data class User(
    // Keycloak id
    val id: UUID,
    val projects: List<Project> = emptyList(),
    val note: String,
)
