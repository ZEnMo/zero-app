package com.zenmo.zummon.companysurvey

import kotlinx.serialization.Serializable

/**
 * Mobiliteit
 */
@Serializable
data class Transport (
    val hasVehicles: Boolean? = null,
    val numDailyCarAndVanCommuters: Int? = null,
    val numDailyCarVisitors: Int? = null,
    val numCommuterAndVisitorChargePoints: Int? = null,
    val trucks: Trucks = Trucks(),
    val vans: Vans = Vans(),
    val cars: Cars = Cars(),
    val agriculture: Agriculture = Agriculture(),
    val otherVehicles: OtherVehicles = OtherVehicles(),
)

@Serializable
data class Trucks (
    // Current situation
    val numTrucks: Int? = null, // Total number including electric
    val numElectricTrucks: Int? = null,
    val numChargePoints: Int? = null,
    val powerPerChargePointKw: Float? = null,
    val annualTravelDistancePerTruckKm: Int? = null,
    // Nice to have: ask possible charge times.

    // Disagreement on whether we should ask specifics about planned charge points
    val numPlannedElectricTrucks: Int? = null,
    // Only for bedrijventerrein De Wieken
    val numPlannedHydrogenTrucks: Int? = null,
)

@Serializable
data class Vans (
    val numVans: Int? = null,
    val numElectricVans: Int? = null,
    val numChargePoints: Int? = null,
    val powerPerChargePointKw: Float? = null,
    val annualTravelDistancePerVanKm: Int? = null,

    val numPlannedElectricVans: Int? = null,
    // Only for bedrijventerrein De Wieken
    val numPlannedHydrogenVans: Int? = null,
)

@Serializable
data class Cars (
    val numCars: Int? = null,
    val numElectricCars: Int? = null,
    val numChargePoints: Int? = null,
    val powerPerChargePointKw: Float? = null,
    val annualTravelDistancePerCarKm: Int? = null,

    val numPlannedElectricCars: Int? = null,
    // Only for bedrijventerrein De Wieken
    val numPlannedHydrogenCars: Int? = null,
)

@Serializable
data class OtherVehicles (
    val hasOtherVehicles: Boolean? = null,
    // which vehicles, usage patterns, how many, electrification.
    val description: String = "",
)

@Serializable
data class Agriculture (
    val numTractors: Int? = null,
    val annualDieselUsage_L: Double? = null,
)
