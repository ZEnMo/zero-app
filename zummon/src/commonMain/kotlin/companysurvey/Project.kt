package com.zenmo.zummon.companysurvey

import com.zenmo.zummon.KotlinUuidSerializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Serializable
@JsExport
data class Project
constructor(
//    @Contextual
    @Serializable(with = KotlinUuidSerializer::class)
    val id: Uuid? = null,
    val name: String = "",
    // Project ID aka Energy Hub ID of Energieke Regio.
    val energiekeRegioId: Int?,
    val buurtCodes: List<String> = emptyList(),
)
