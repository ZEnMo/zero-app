package com.zenmo.fudura

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class GetChannelMetadataResult(
    val channelId: String,
    val channelType: ChannelType,
    val channelDataType: ChannelDataType,
    val productType: ProductType,
    // for cumulative gas values, this will contain the word "standen"
    val description: String,
    val longDescription: String,
    // "00:15:00" for quarter-hourly data.
    // not present when channelType == ChannelType.Register.
    val interval: String? = null,
    val direction: Direction,
    val unitOfMeasurement: UnitOfMeasurement,
    val firstReadingTimestamp: Instant,
    val lastReadingTimestamp: Instant,
    val telemetryUpdatedTimestamp: Instant,
    val lastChangeFeedCheckpoint: String,
)

enum class ChannelType {
    Profiel,
    Register, // Used for maandmaximum, maandtotaal
    Onbekend,
}

enum class ChannelDataType {
    Decimal,
    Text,
}

enum class UnitOfMeasurement {
    kW,
    kWh,
    kVA,
    kVarh,
    kVAr,
    m3,
    K, // Hoogcalorisch gas?
    Onbekend,
}

enum class Direction {
    Consumption,
    Production,
    Unknown, // is often Consumption - Production
}

enum class ProductType {
    Electricity,
    Gas,
}
