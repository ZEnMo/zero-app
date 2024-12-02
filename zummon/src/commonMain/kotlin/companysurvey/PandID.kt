package com.zenmo.zummon.companysurvey

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.zenmo.zummon.BenasherUuidSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport

val pandIdRegex = Regex("^\\d{16}\$")

/**
 * Would prefer it if it was an inline value class but JS export does not support it
 */
@JsExport
@Serializable(with = PandIdSerializer::class)
data class PandID(val value: String) {
    init {
        if (!value.matches(pandIdRegex)) {
            throw IllegalArgumentException("Pand ID should have 16 digits. Got: $value")
        }
    }
}

// Unnest object to string so the JSON looks nicer
object PandIdSerializer : KSerializer<PandID> {
    override val descriptor = PrimitiveSerialDescriptor("PandId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PandID {
        return PandID(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: PandID) {
        encoder.encodeString(value.value)
    }
}
