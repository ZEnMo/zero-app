package com.zenmo.zummon.companysurvey

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zenmo.zummon.BenasherUuidSerializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class Address(
    @Serializable(with = BenasherUuidSerializer::class)
    val id: Uuid = uuid4(),

    val street: String,
    val houseNumber: Int,
    val houseLetter: String = "", // A-Z allowed
    val houseNumberSuffix: String = "",
    val postalCode: String = "",
    val city: String,

    val gridConnections: List<GridConnection> = emptyList(),
) {
    /**
     * For JavaScript
     */
    public val gridConnectionArray: Array<GridConnection>
        get() = gridConnections.toTypedArray()
}
