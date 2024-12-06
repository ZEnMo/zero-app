package com.zenmo.zummon.companysurvey

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zenmo.zummon.BenasherUuidSerializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class Project(
//    @Contextual
    @Serializable(with = BenasherUuidSerializer::class)
    val id: Uuid = uuid4(),
    val name: String = "",
    // Project ID aka Energy Hub ID of Energieke Regio.
    val energiekeRegioId: Int?,
    val buurtCodes: List<String> = emptyList(),
)
