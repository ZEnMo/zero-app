package com.zenmo.zummon.companysurvey

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.zenmo.zummon.BenasherUuidSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class GridConnection(
    @Serializable(with = BenasherUuidSerializer::class)
    val id: Uuid = uuid4(),

    // Is always set when object comes from the database.
    val sequence: Int? = null,

    val electricity: Electricity = Electricity(),
    val supply: Supply = Supply(),
    val naturalGas: NaturalGas = NaturalGas(),
    val heat: Heat = Heat(),
    val storage: Storage = Storage(),
    val transport: Transport = Transport(),
    val pandIds: Set<PandID> = emptySet(),

    // open questions
    val energyOrBuildingManagementSystemSupplier: String = "",
    val mainConsumptionProcess: String = "",
    val consumptionFlexibility: String = "",
    val expansionPlans: String = "",
    val electrificationPlans: String = "",
    val surveyFeedback: String = "",
)
