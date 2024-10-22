
package com.zenmo.orm.companysurvey.table

import com.zenmo.orm.dbutil.KleinverbruikPGEnum
import com.zenmo.orm.dbutil.PGEnum
import com.zenmo.orm.dbutil.ZenmoUUIDTable
import com.zenmo.orm.dbutil.enumArray
import com.zenmo.zummon.companysurvey.*
import org.jetbrains.exposed.sql.Sequence
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.nextIntVal

/**
 * This is to have a human-readable id for grid connections.
 */
val gridConnectionSequence = Sequence("grid_connection_sequence")

object GridConnectionTable: ZenmoUUIDTable("grid_connection") {
    val addressId = uuid("address_id").references(AddressTable.id)

    val sequence = integer("sequence").defaultExpression(gridConnectionSequence.nextIntVal())

    /**
     * Open questions
     */
    val energyOrBuildingManagementSystemSupplier = varchar("energy_or_building_management_system_supplier", 1000)
    val mainConsumptionProcess = varchar("main_consumption_process", 1000)
    val consumptionFlexibility = varchar("consumption_flexibility", 1000)
    val expansionPlans = varchar("expansion_plans", 1000)
    val electrificationPlans = varchar("electrification_plans", 1000)
    val surveyFeedback = varchar("survey_feedback", 1000)

    /**
     * [com.zenmo.zummon.companysurvey.Electricity]
     */
    val hasElectricityConnection = bool("has_electricity_connection").nullable()
    val electricityEan = varchar("electricity_ean", 18)
    val annualElectricityDelivery_kWh = uinteger("annual_electricity_delivery_kwh").nullable()
    val annualElectricityFeedIn_kWh = uinteger("annual_electricity_feed_in_kwh").nullable()
    val annualElectricityProduction_kWh = integer("annual_electricity_production_kwh").nullable().default(null)

    val kleinverbruikOrGrootverbruik = customEnumeration(
        "kleinverbruik_or_grootverbruik",
        KleinverbruikOrGrootverbruik::class.simpleName,
        fromDb = { KleinverbruikOrGrootverbruik.valueOf(it as String) },
        toDb = { PGEnum(KleinverbruikOrGrootverbruik::class.simpleName!!, it) }).nullable()
    val kleinverbruikElectricityConnectionCapacity = customEnumeration(
        "kleinverbuik_electricity_connection_capacity",
        KleinverbruikElectricityConnectionCapacity::class.simpleName,
        fromDb = { kleinverbruikEnumFromDisplayName(it as String) },
        toDb = { KleinverbruikPGEnum(it) }).nullable()

    val kleinverbuikElectricityConsumptionProfile = customEnumeration(
        "kleinverbuik_electricity_consumption_profile",
        KleinverbruikElectricityConsumptionProfile::class.simpleName,
        fromDb = { KleinverbruikElectricityConsumptionProfile.valueOf(it as String) },
        toDb = { PGEnum(KleinverbruikElectricityConsumptionProfile::class.simpleName!!, it) }).nullable()

    val grootverbruikContractedDeliveryCapacityKw = uinteger("grootverbruik_contracted_delivery_capacity_kw").nullable()
    val grootverbruikContractedFeedInCapacityKw = uinteger("grootverbruik_contracted_feed_in_capacity_kw").nullable()
    val grootverbruikPhysicalCapacityKw = uinteger("grootverbruik_physical_capacity_kw").nullable()
    val hasExpansionRequestAtGridOperator = bool("has_expansion_request_at_grid_operator").nullable()
    val expansionRequestKW = uinteger("expansion_request_kw").nullable()
    val expansionRequestReason = varchar("expansion_request_reason", 1000)
    val pandIds = array<String>("pand_ids", VarCharColumnType(16)).default(emptyList())

    /**
     * [com.zenmo.zummon.companysurvey.Supply]
     */
    val hasSupply = bool("has_supply").nullable()
    val pvInstalledKwp = uinteger("pv_installed_kwp").nullable()
    val pvOrientation = customEnumeration(
        "pv_orientation",
        PVOrientation::class.simpleName,
        fromDb = { PVOrientation.valueOf(it as String) },
        toDb = { PGEnum(PVOrientation::class.simpleName!!, it) }).nullable()
    val pvPlanned = bool("pv_planned").nullable()
    val pvPlannedKwp = uinteger("pv_planned_kwp").nullable()
    val pvPlannedOrientation = customEnumeration(
        "pv_planned_orientation",
        PVOrientation::class.simpleName,
        fromDb = { PVOrientation.valueOf(it as String) },
        toDb = { PGEnum(PVOrientation::class.simpleName!!, it) }).nullable()
    val pvPlannedYear = uinteger("pv_planned_year").nullable()
    val missingPvReason = customEnumeration(
        "missing_pv_reason",
        MissingPvReason::class.simpleName,
        fromDb = { MissingPvReason.valueOf(it as String) },
        toDb = { PGEnum(MissingPvReason::class.simpleName!!, it) }).nullable()
    val windInstalledKw = float("wind_installed_kw").nullable()
    val windPlannedKw = float("wind_planned_kw").nullable()
    val otherSupply = varchar("other_supply", 1000)

    /**
     * [com.zenmo.zummon.companysurvey.NaturalGas]
     */
    val hasNaturalGasConnection = bool("has_natural_gas_connection").nullable()
    val naturalGasEan = varchar("natural_gas_ean", 18)
    val naturalGasAnnualDeliveryM3 = uinteger("natural_gas_annual_delivery_m3").nullable()
    val percentageNaturalGasForHeating = uinteger("percentage_natural_gas_for_heating").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.Heat]
     */
    val heatingTypes = enumArray("heating_types", HeatingType::class)
    val sumGasBoilerKw = float("combined_gas_boiler_kw").nullable()
    val sumHeatPumpKw = float("combined_heat_pump_kw").nullable()
    val sumHybridHeatPumpElectricKw = float("combined_hybrid_heat_pump_electric_kw").nullable()
    val annualDistrictHeatingDeliveryGj = float("annual_district_heating_delivery_gj").nullable()
    val localHeatExchangeDescription = varchar("local_heat_exchange_description", 1000)
    val hasUnusedResidualHeat = bool("has_unused_residual_heat").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.Storage]
     */
    val hasBattery = bool("has_battery").nullable()
    val batteryCapacityKwh = float("battery_capacity_kwh").nullable()
    val batteryPowerKw = float("battery_power_kw").nullable()
    val batterySchedule = varchar("battery_schedule", 1000)

    val hasPlannedBattery = bool("has_planned_battery").nullable()
    val plannedBatteryCapacityKwh = float("planned_battery_capacity_kwh").nullable()
    val plannedBatteryPowerKw = float("planned_battery_power_kw").nullable()
    val plannedBatterySchedule = varchar("planned_battery_schedule", 1000)

    val hasThermalStorage = bool("has_thermal_storage").nullable()
    val thermalStorageKw = float("thermal_storage_kw").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.Transport]
     */
    val hasVehicles = bool("has_vehicles").nullable()

    val numDailyCarAndVanCommuters = uinteger("num_daily_car_and_van_commuters").nullable()
    val numDailyCarVisitors = uinteger("num_daily_car_visitors").nullable()
    val numCommuterAndVisitorChargePoints = uinteger("num_commuter_and_visitor_charge_points").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.Trucks]
     */
    val numTrucks = uinteger("num_trucks").nullable()
    val numElectricTrucks = uinteger("num_electric_trucks").nullable()
    val numTruckChargePoints = uinteger("num_truck_charge_points").nullable()
    val powerPerTruckChargePointKw = float("power_per_truck_charge_point_kw").nullable()
    val annualTravelDistancePerTruckKm = uinteger("annual_travel_distance_per_truck_km").nullable()
    val numPlannedElectricTrucks = uinteger("num_planned_electric_trucks").nullable()
    val numPlannedHydgrogenTrucks = uinteger("num_planned_hydrogen_trucks").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.Vans]
     */
    val numVans = uinteger("num_vans").nullable()
    val numElectricVans = uinteger("num_electric_vans").nullable()
    val numVanChargePoints = uinteger("num_electric_van_charge_points").nullable()
    val powerPerVanChargePointKw = float("power_per_van_charge_point_kw").nullable()
    val annualTravelDistancePerVanKm = uinteger("annual_travel_distance_per_van_km").nullable()
    val numPlannedElectricVans = uinteger("num_planned_electric_vans").nullable()
    val numPlannedHydgrogenVans = uinteger("num_planned_hydrogen_vans").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.Cars]
     */
    val numCars = uinteger("num_cars").nullable()
    val numElectricCars = uinteger("num_electric_cars").nullable()
    val numCarChargePoints = uinteger("num_car_charge_points").nullable()
    val powerPerCarChargePointKw = float("power_per_car_charge_point_kw").nullable()
    val annualTravelDistancePerCarKm = uinteger("annual_travel_distance_per_car_km").nullable()
    val numPlannedElectricCars = uinteger("num_planned_electric_cars").nullable()
    val numPlannedHydgrogenCars = uinteger("num_planned_hydrogen_cars").nullable()

    /**
     * [com.zenmo.zummon.companysurvey.OtherVehicles]
     */
    val hasOtherVehicles = bool("has_other_vehicles").nullable()
    val otherVehiclesDescription = varchar("other_vehicles_description", 5000)
}
