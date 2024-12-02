package com.zenmo.zummon.companysurvey
import kotlin.js.JsExport

@JsExport
fun interface Validator<T> {
    fun validate(item: T): List<ValidationResult>
}

@JsExport
data class ValidationResult(
    val status: Status,
    val message: String,
)

@JsExport
enum class Status {
    VALID,
    INVALID,
    MISSING_DATA,
    NOT_APPLICABLE,
}

@JsExport
val surveyValidator = Validator<Survey> { survey: Survey ->
    survey.addresses.flatMap {
        addressValidator.validate(it)
    }
}

val addressValidator = Validator<Address> { address: Address ->
    address.gridConnections.flatMap {
        GridConnectionValidator().validate(it)
    }
}

class GridConnectionValidator : Validator<GridConnection> {
    override fun validate(gridConnection: GridConnection): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()

        results.addAll(ElectricityValidator().validate(gridConnection.electricity))
        results.addAll(StorageValidator().validate(gridConnection.storage))
        results.addAll(NaturalGasValidator().validate(gridConnection.naturalGas))
        results.addAll(TransportValidator().validate(gridConnection.transport))
        results.addAll(validateTotalPowerChargePoints(gridConnection))
        results.add(validateQuarterHourlyFeedIn(gridConnection))
        results.add(validateAnnualElectricityProduction(gridConnection))
        results.add(quarterHourlyFeedInLowProductionBatteryPower(gridConnection))
        results.add(validatePvInstalled(gridConnection))

        return results
    }
    // Validator for total charge point power < contracted capacity + battery power
    fun validateTotalPowerChargePoints(gridConnection: GridConnection): List<ValidationResult> {
        val totalPowerChargePoints = listOf(
            gridConnection.transport.cars.powerPerChargePointKw,
            gridConnection.transport.trucks.powerPerChargePointKw,
            gridConnection.transport.vans.powerPerChargePointKw
        ).map { (it ?: 0).toFloat() }.sum()

        val contractedCapacity = (gridConnection.electricity.getContractedConnectionCapacityKw() ?: 0.0).toFloat()
        val batteryPower = (gridConnection.storage.batteryPowerKw ?: 0.0).toFloat()

        return if (totalPowerChargePoints < (contractedCapacity + batteryPower)) {
            listOf(ValidationResult(Status.VALID, translate("gridConnection.totalPowerChargePoints")))
        } else {
            listOf(ValidationResult(Status.INVALID, translate("gridConnection.totalPowerChargePointsInvalid", totalPowerChargePoints, contractedCapacity + batteryPower)))
        }
    }

    //quarter-hourly production should not be all zeroes if hasPv is true
    fun validateQuarterHourlyFeedIn(gridConnection: GridConnection): ValidationResult {
        return when { gridConnection.supply.hasSupply == true && gridConnection.electricity.quarterHourlyProduction_kWh?.values?.all { it == 0f } == true ->
                ValidationResult(Status.INVALID, translate("electricity.quarterHourlyProductionCannotBeAllZero"))
            else ->
                ValidationResult(Status.VALID, translate("electricity.quarterHourlyProductionValid"))
        }
    }

    //hasPV true -> should have annual production
    fun validateAnnualElectricityProduction(gridConnection: GridConnection): ValidationResult {
        return when {
            gridConnection.supply.hasSupply == true && gridConnection.electricity.annualElectricityProduction_kWh == null ->
                ValidationResult(Status.MISSING_DATA, translate("electricity.annualElectricityProductionNotProvided"))
            else ->
                ValidationResult(Status.NOT_APPLICABLE, translate("electricity.withoutConnection"))
        }
    }

    //every time step in quarter-hourly feed-in should be less than or equal to production + battery power
    fun quarterHourlyFeedInLowProductionBatteryPower(gridConnection: GridConnection): ValidationResult {
        val feedIn = gridConnection.electricity.quarterHourlyFeedIn_kWh
        val production = gridConnection.electricity.quarterHourlyProduction_kWh
        val batteryPower = (gridConnection.storage.batteryPowerKw ?: 0.0).toFloat()

        // Ensure both time series starts at the same time
        if (feedIn?.start != production?.start) {
            return ValidationResult(
                Status.INVALID,
                translate("gridConnection.incompatibleStartTimeQuarterHourly")
            )
        }

        // Ensure both time series have the same length for comparison
        if (feedIn?.values?.size != production?.values?.size) {
            return ValidationResult(
                Status.INVALID,
                translate("gridConnection.incompatibleQuarterHourly")
            )
        }

        // Check each value in the feed-in time series is <= the corresponding production value
        feedIn?.values?.forEachIndexed { index, value ->
            production?.values?.let { productionValues ->
                val maxProduction = (productionValues.getOrNull(index) ?: Float.MIN_VALUE) + batteryPower
                if (value > maxProduction) {
                    return ValidationResult( Status.INVALID, translate("gridConnection.quarterHourlyFeedInHighProductionBatteryPower", value, maxProduction))
                }
            }
        }

        return ValidationResult(Status.VALID, translate("gridConnection.quarterHourlyFeedInLowProductionBatteryPower"))
    }

    // PV installed power should not be larger dan 5000kW
    fun validatePvInstalled(gridConnection: GridConnection): ValidationResult {
        return when {
            gridConnection.supply.hasSupply == true && ((gridConnection.supply.pvInstalledKwp ?: 0) < 5000) ->
                ValidationResult(Status.VALID, translate("gridConnection.pvInstalledHigh"))
            else ->
                ValidationResult(Status.INVALID, translate("gridConnection.pvInstalledLow"))
        }
    }
}

class ElectricityValidator : Validator<Electricity> {
    override fun validate(electricity: Electricity): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()

        results.addAll(validateKleinOrGroot(electricity))
        results.add(validateContractedCapacity(electricity))
        results.add(validateAnnualProductionFeedIn(electricity))
        results.add(validateContractedFeedInCapacity(electricity))

        results.add(validateAnnualFeedInMatchesQuarterHourlyFeedIn(electricity))
        results.add(validateQuarterHourlyDeliveryData(electricity))
        results.add(validateQuarterHourlyProductionData(electricity))
        results.add(quarterHourlyDeliveryLowContractedCapacity(electricity))
        results.add(quarterFeedInLowContractedCapacity(electricity))
        return results
    }

    fun validateKleinOrGroot(electricity: Electricity): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()
        val kleinOrGroot = electricity.kleinverbruikOrGrootverbruik

        if (kleinOrGroot == null) {
            results.add(ValidationResult(
                Status.MISSING_DATA,
                translate("electricity.kleinverbruikOrGrootverbruikNoDefined")
            ))
        } else {
            // Validate grootverbruik data
            if (kleinOrGroot == KleinverbruikOrGrootverbruik.GROOTVERBRUIK) {
                results.addAll(validateGrootverbruik(electricity.grootverbruik))
                results.add(validateGrootverbruikPhysicalCapacity(electricity))
            }

            // Validate kleinverbruik data
            if (kleinOrGroot == KleinverbruikOrGrootverbruik.KLEINVERBRUIK) {
                results.add(validateKleinverbruik(electricity))
            }
        }

        return results
    }

    fun validateGrootverbruik(grootverbruik: CompanyGrootverbruik?): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()

        if (grootverbruik == null) {
            results.add(ValidationResult(Status.INVALID, translate("grootverbruik.notProvided")))
        } else {
            grootverbruik.contractedConnectionDeliveryCapacity_kW ?: results.add(ValidationResult(Status.MISSING_DATA, translate("grootverbruik.connectionCapacityNotProvide")))
            grootverbruik.physicalCapacityKw ?: results.add(ValidationResult(Status.MISSING_DATA, translate("grootverbruik.physicalCapacityNotProvide")))
            grootverbruik.contractedConnectionFeedInCapacity_kW ?: results.add(ValidationResult(Status.MISSING_DATA, translate("grootverbruik.connectionFeedInCapacityNotProvide")))
        }

        return results
    }

    // Validator for grootverbruik physical connection > 3x80A (55.2 kW)
    fun validateGrootverbruikPhysicalCapacity(electricity: Electricity): ValidationResult {
        val connectionCapacity = electricity.getPhysicalConnectionCapacityKw()

        return if (connectionCapacity == null) {
            ValidationResult(Status.MISSING_DATA, translate("grootverbruik.physicalCapacityNotProvide")) // Same validation in multiple places
        } else if (connectionCapacity > KleinverbruikElectricityConnectionCapacity._3x80A.toKw()) {
            ValidationResult(Status.VALID, translate("grootverbruik.valid"))
        } else {
            ValidationResult(Status.INVALID, translate("grootverbruik.invalid", connectionCapacity))
        }
    }

    fun validateKleinverbruik(electricity: Electricity): ValidationResult {
        // Check if kleinverbruikOrGrootverbruik is KLEINVERBRUIK
        return if (electricity.kleinverbruikOrGrootverbruik == KleinverbruikOrGrootverbruik.KLEINVERBRUIK) {
            val kleinverbruik = electricity.kleinverbruik

            // If kleinverbruik is null, return invalid
            if (kleinverbruik == null) {
                ValidationResult(Status.MISSING_DATA, translate("kleinverbruik.notProvided"))
            }
            // If kleinverbruik.connectionCapacity is not null, perform the validation
            else if (kleinverbruik.connectionCapacity != null) {
                // Compare kleinverbruik connection capacity with 3x80A using enum comparison
                if (kleinverbruik.connectionCapacity <= KleinverbruikElectricityConnectionCapacity._3x80A) {
                    ValidationResult(Status.VALID, translate("kleinverbruik.valid"))
                } else {
                    ValidationResult(
                        Status.INVALID,
                        translate("kleinverbruik.exceedsLimit", kleinverbruik.connectionCapacity)
                    )
                }
            }
            // If connection capacity is null, return invalid
            else {
                ValidationResult(Status.INVALID, translate("kleinverbruik.invalid"))
            }
        }
        // If kleinverbruikOrGrootverbruik is not KLEINVERBRUIK, return not applicable
        else {
            ValidationResult(Status.NOT_APPLICABLE, translate("kleinverbruik.notApplicable"))
        }
    }

    // Validator for contracted delivery capacity <= physical capacity
    fun validateContractedCapacity(electricity: Electricity): ValidationResult {
        val contractedCapacity = electricity.getContractedConnectionCapacityKw()
        val physicalCapacity = electricity.getPhysicalConnectionCapacityKw()

        return when {
            contractedCapacity == null -> ValidationResult(Status.MISSING_DATA, translate("electricity.contractedCapacityNotProvided"))
            physicalCapacity == null -> ValidationResult(Status.MISSING_DATA, translate("electricity.physicalCapacityNotProvide"))
            contractedCapacity <= physicalCapacity -> ValidationResult(Status.VALID, translate("electricity.contractedDeliveryCapacityValid", contractedCapacity))
            else -> ValidationResult(Status.INVALID, translate("electricity.contractedDeliveryCapacityExceeds", contractedCapacity, physicalCapacity))
        }
    }

    // Validator for contracted feed-in capacity <= physical capacity
    fun validateContractedFeedInCapacity(electricity: Electricity): ValidationResult {
        val feedInCapacity = electricity.getContractedFeedInCapacityKw()
        val physicalCapacity = electricity.getPhysicalConnectionCapacityKw()

        return when {
            feedInCapacity == null -> ValidationResult(Status.MISSING_DATA, translate("electricity.connectionFeedInCapacityNotProvide"))
            physicalCapacity == null -> ValidationResult(Status.MISSING_DATA, translate("electricity.physicalCapacityNotProvide"))
            feedInCapacity <= physicalCapacity -> ValidationResult(Status.VALID, translate("electricity.feedInLowerPhysicalCapacity", feedInCapacity, physicalCapacity ))
            else -> ValidationResult(Status.INVALID, translate("electricity.feedInExceedPhysicalCapacity", feedInCapacity, physicalCapacity ))
        }
    }



    // Annual pv production should be more than annual feed-in
    fun validateAnnualProductionFeedIn(electricity: Electricity): ValidationResult {
        val annualProduction = electricity.annualElectricityProduction_kWh
        val feedIn = electricity.annualElectricityFeedIn_kWh

        return when {
            annualProduction == null -> ValidationResult(Status.MISSING_DATA, translate("electricity.annualElectricityProductionNotProvided"))
            feedIn == null -> ValidationResult(Status.MISSING_DATA, translate("electricity.annualElectricityFeedInNotProvided"))
            annualProduction >= feedIn -> ValidationResult(Status.VALID, translate("electricity.annualProductionFeedInValid", annualProduction, feedIn))
            else -> ValidationResult(Status.INVALID, translate("electricity.annualProductionFeedInInvalid", annualProduction, feedIn))
        }
    }

    //annual feed-in should match total of quarter-hourly feed-in
    fun validateAnnualFeedInMatchesQuarterHourlyFeedIn(electricity: Electricity): ValidationResult {
        if (electricity.quarterHourlyFeedIn_kWh == null) {
            return ValidationResult(Status.MISSING_DATA, translate("electricity.quarterHourlyFeedInNotProvided"))
        }

        return if (electricity.quarterHourlyFeedIn_kWh.hasFullYear() == true) {
            val totalQuarterHourlyFeedIn = electricity.quarterHourlyFeedIn_kWh.values.sum()

            val isCloseEnough = electricity.annualElectricityFeedIn_kWh?.toFloat()?.let { annualFeedIn ->
                val difference = kotlin.math.abs(annualFeedIn - totalQuarterHourlyFeedIn)
                difference <= 0.01f * annualFeedIn // 1% tolerance
            } ?: false

            if (isCloseEnough) {
                ValidationResult(Status.VALID, translate("electricity.annualFeedInValid", electricity.annualElectricityFeedIn_kWh, totalQuarterHourlyFeedIn))
            } else {
                ValidationResult(Status.INVALID, translate("electricity.annualFeedInMismatch", electricity.annualElectricityFeedIn_kWh, totalQuarterHourlyFeedIn))
            }
        } else {
            ValidationResult(Status.INVALID, translate("electricity.notEnoughValues",
                electricity.annualElectricityFeedIn_kWh,
                electricity.quarterHourlyFeedIn_kWh.values.size
            ))
        }
    }

    // quarter-hourly delivery data should not have "holes" longer than 4 days. Display duration of hole in message.
    fun validateQuarterHourlyDeliveryData(electricity: Electricity): ValidationResult {
        electricity.quarterHourlyDelivery_kWh?.values ?: return ValidationResult(
            Status.MISSING_DATA,
            translate("electricity.quarterHourlyDeliveryDataNotProvided")
        )

        var maxNullSequence = electricity.quarterHourlyDelivery_kWh.lengthEmptyGapsData()

        return if (maxNullSequence > 384) { // 384 nulls is 4 days of missing data in quarter-hour intervals
            ValidationResult(
                Status.INVALID,
                translate("electricity.quarterHourlyDeliveryDataHolesExceed", maxNullSequence / 4) // Display in hours
            )
        } else {
            ValidationResult(Status.VALID, translate("electricity.quarterHourlyDeliveryDataValid"))
        }
    }

    //quarter-hourly production data should not have "holes" longer than 4 days. Display duration of hole in message.
    fun validateQuarterHourlyProductionData(electricity: Electricity): ValidationResult {
        electricity.quarterHourlyProduction_kWh?.values ?: return ValidationResult(
            Status.MISSING_DATA,
            translate("electricity.quarterHourlyProductionDataNotProvided")
        )

        var maxNullSequence = electricity.quarterHourlyProduction_kWh.lengthEmptyGapsData()

        return if (maxNullSequence > 384) { // 384 nulls is 4 days of missing data in quarter-hour intervals
            ValidationResult(
                Status.INVALID,
                translate("electricity.quarterHourlyProductionDataHolesExceed", maxNullSequence / 4) // Display in hours
            )
        } else {
            ValidationResult(Status.VALID, translate("electricity.quarterHourlyProductionDataValid"))
        }
    }

    //peak of delivery should be less than contracted capacity
    fun quarterHourlyDeliveryLowContractedCapacity(electricity: Electricity): ValidationResult {
        val contractedCapacity = (electricity.getContractedConnectionCapacityKw() ?: 0.0).toFloat()
        val pickDelivery = electricity.quarterHourlyDelivery_kWh?.values?.maxOrNull() ?: Float.MIN_VALUE
        return if ( pickDelivery <= contractedCapacity) {
            ValidationResult(Status.VALID, translate("electricity.quarterHourlyDeliveryLowContractedCapacityKw", contractedCapacity))
        } else {
            ValidationResult(Status.INVALID, translate("electricity.quarterHourlyDeliveryHighContractedCapacityKw", contractedCapacity))
        }
    }

    //peak of feed-in should be less than contracted capacity
    fun quarterFeedInLowContractedCapacity(electricity: Electricity): ValidationResult {
        val contractedFeedInCapacity = (electricity.getContractedFeedInCapacityKw() ?: 0.0).toFloat()
        val pickFeedIn = electricity.quarterHourlyFeedIn_kWh?.values?.maxOrNull() ?: Float.MIN_VALUE
        return if (pickFeedIn < contractedFeedInCapacity) {
            ValidationResult(Status.VALID, translate("electricity.quarterHourlyDeliveryLowContractedCapacityKw", contractedFeedInCapacity))
        } else {
            ValidationResult(Status.INVALID, translate("electricity.quarterHourlyDeliveryHighContractedCapacityKw", contractedFeedInCapacity))
        }
    }


}

class StorageValidator : Validator<Storage> {
    override fun validate(storage: Storage): List<ValidationResult> {
        return listOf(validateAnnualElectricityProduction(storage))
    }

    //hasBattery true -> should have power and capacity
    fun validateAnnualElectricityProduction(storage: Storage): ValidationResult {
        return if (storage.hasBattery == true) {
            if ((storage.batteryCapacityKwh ?: 0) == 0) {
                ValidationResult(Status.MISSING_DATA, translate("storage.batteryCapacityNotProvided"))
            } else if ((storage.batteryPowerKw ?: 0) == 0) {
                ValidationResult(Status.MISSING_DATA, translate("storage.batteryPowerNotProvided"))
            } else {
                ValidationResult(Status.VALID, translate("storage.batteryWithPowerAndCapacity"))
            }
        } else {
            ValidationResult(Status.NOT_APPLICABLE, translate("storage.withoutConnection"))
        }
    }
}

class NaturalGasValidator : Validator<NaturalGas> {
    override fun validate(naturalGas: NaturalGas): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()

        results.add(validateAnnualElectricityProduction(naturalGas))
        results.add(validateAnnualGasDelivery(naturalGas))

        return results
    }

    //hasNaturalGasConnection true -> should have annual delivery
    fun validateAnnualElectricityProduction(naturalGas: NaturalGas): ValidationResult {
        return when {
            naturalGas.hasConnection == true && (naturalGas.annualDelivery_m3 ?: 0) == 0 ->
                ValidationResult(Status.MISSING_DATA, translate("naturalGas.annualDeliveryNotProvided"))
            else ->
                ValidationResult(Status.NOT_APPLICABLE, translate("naturalGas.withoutConnection"))
        }
    }

    //annual gas delivery should match total of hourly delivery
    fun validateAnnualGasDelivery(naturalGas: NaturalGas): ValidationResult {
        return if (naturalGas.hourlyDelivery_m3?.values == null) {
            ValidationResult(Status.MISSING_DATA, translate("naturalGas.hourlyDeliveryNotProvided"))
        } else {
            val totalHourlyDelivery = naturalGas.hourlyDelivery_m3.values.sum()

            val isCloseEnough = naturalGas.annualDelivery_m3?.toFloat()?.let { annualDelivery ->
                val difference = kotlin.math.abs(annualDelivery - totalHourlyDelivery)
                difference <= 0.01f * annualDelivery // 1% tolerance
            } ?: false


            if (isCloseEnough) {
                ValidationResult(Status.VALID, translate("naturalGas.annualGasDeliveryValid", naturalGas.annualDelivery_m3))
            } else {
                ValidationResult(Status.INVALID, translate("naturalGas.annualGasDeliveryMismatch", naturalGas.annualDelivery_m3, totalHourlyDelivery))
            }
        }
    }


}

class TransportValidator {
    fun validate(transport: Transport): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()

        results.add(validatePowerPerChargeCars(transport))
        results.add(validatePowerPerChargeTrucks(transport))
        results.add(validatePowerPerChargeVans(transport))

        results.add(validateTravelDistanceCar(transport))
        results.add(validateTravelDistanceTruck(transport))
        results.add(validateTravelDistanceVan(transport))

        results.add(validateTotalElectricCars(transport))
        results.add(validateTotalElectricTrucks(transport))
        results.add(validateTotalElectricVans(transport))

        return results
    }

    // Validator for power per charge point in range 3..150 kW
    fun validatePowerPerChargeCars(transport: Transport): ValidationResult {
        val powerPerChargePointCars = transport.cars.powerPerChargePointKw

        return when {
            powerPerChargePointCars == null -> ValidationResult(Status.NOT_APPLICABLE, translate("transport.carsPowerNotProvided"))
            powerPerChargePointCars in 3.0..150.0 -> ValidationResult(Status.VALID, translate("transport.carsPowerValid", powerPerChargePointCars))
            else -> ValidationResult(Status.INVALID, translate("transport.carsPowerInvalid", powerPerChargePointCars))
        }
    }

    fun validatePowerPerChargeTrucks(transport: Transport): ValidationResult {
        val powerPerChargePointTrucks = transport.trucks.powerPerChargePointKw

        return when {
            powerPerChargePointTrucks == null -> ValidationResult(Status.NOT_APPLICABLE, translate("transport.trucksPowerNotProvided"))
            powerPerChargePointTrucks in 3.0..150.0 -> ValidationResult(Status.VALID, translate("transport.trucksPowerValid", powerPerChargePointTrucks))
            else -> ValidationResult(Status.INVALID, translate("transport.trucksPowerInvalid", powerPerChargePointTrucks))
        }
    }

    fun validatePowerPerChargeVans(transport: Transport): ValidationResult {
        val powerPerChargePointVans = transport.vans.powerPerChargePointKw

        return when {
            powerPerChargePointVans == null -> ValidationResult(Status.NOT_APPLICABLE, translate("transport.vansPowerNotProvided"))
            powerPerChargePointVans in 3.0..150.0 -> ValidationResult(Status.VALID, translate("transport.vansPowerValid", powerPerChargePointVans))
            else -> ValidationResult(Status.INVALID, translate("transport.vansPowerInvalid", powerPerChargePointVans))
        }
    }

    // Validator for vehicle travel distance in range 5k..100k km
    fun validateTravelDistanceCar(transport: Transport): ValidationResult {
        val travelDistanceCars = transport.cars.annualTravelDistancePerCarKm

        return when {
            travelDistanceCars == null -> ValidationResult(Status.MISSING_DATA, translate("transport.distanceCarsNotProvided"))
            travelDistanceCars in 5000..100000 -> ValidationResult(Status.VALID, translate("transport.distanceCarsValid"))
            else -> ValidationResult(Status.INVALID, translate("transport.distanceCarsInvalid", travelDistanceCars))
        }
    }

    fun validateTravelDistanceTruck(transport: Transport): ValidationResult {
        val travelDistanceTrucks = transport.trucks.annualTravelDistancePerTruckKm

        return when {
            travelDistanceTrucks == null -> ValidationResult(Status.MISSING_DATA, translate("transport.distanceTrucksNotProvided"))
            travelDistanceTrucks in 5000..100000 -> ValidationResult(Status.VALID, translate("transport.distanceTrucksValid"))
            else -> ValidationResult(Status.INVALID, translate("transport.distanceTrucksInvalid", travelDistanceTrucks))
        }
    }

    fun validateTravelDistanceVan(transport: Transport): ValidationResult {
        val travelDistanceVans = transport.vans.annualTravelDistancePerVanKm

        return when {
            travelDistanceVans == null -> ValidationResult(Status.MISSING_DATA, translate("transport.distanceVansNotProvided"))
            travelDistanceVans in 5000..100000 -> ValidationResult(Status.VALID, translate("transport.distanceVansValid"))
            else -> ValidationResult(Status.INVALID, translate("transport.distanceVansInvalid", travelDistanceVans))
        }
    }

    // Validator for number of electric vehicles should be less than or equal to total number of vehicles
    fun validateTotalElectricCars(transport: Transport): ValidationResult {
        return when {
            (transport.cars.numElectricCars ?: 0) > (transport.cars.numCars ?: 0) -> ValidationResult(Status.INVALID, translate("transport.electricCarsInvalid", transport.cars.numElectricCars, transport.cars.numCars))
            else -> ValidationResult(Status.VALID, translate("transport.electricCarsValid"))
        }
    }

    fun validateTotalElectricTrucks(transport: Transport): ValidationResult {
        return when {
            (transport.trucks.numElectricTrucks ?: 0) > (transport.trucks.numTrucks ?: 0) -> ValidationResult(Status.INVALID, translate("transport.electricCTrucksInvalid", transport.trucks.numTrucks, transport.trucks.numTrucks))
            else -> ValidationResult(Status.VALID, translate("transport.electricTrucksValid"))
        }
    }

    fun validateTotalElectricVans(transport: Transport): ValidationResult {
        return when {
            ((transport.vans.numElectricVans ?: 0) > (transport.vans.numVans ?: 0)) -> ValidationResult(Status.INVALID, translate("transport.electricVansInvalid", transport.vans.numElectricVans, transport.vans.numVans))
            else -> ValidationResult(Status.VALID, translate("transport.electricVansValid"))
        }
    }
}

fun setLanguage(language: Language) {
    currentLanguage = language
}

fun getLanguage(): Language {
    return currentLanguage
}

enum class Language {
    en,
    nl
}

private
var currentLanguage: Language = Language.en // Set default language

val translations: Map<Language, Map<String, Map<String, String>>> = mapOf(
    Language.en to mapOf(
        "gridConnection" to mapOf(
            "totalPowerChargePointsValid" to "Total power of charge points is valid",
            "totalPowerChargePointsInvalid" to "Total power of charge points %d exceeds allowed capacity %d",
            "quarterHourlyFeedInLowProductionBatteryPower" to "Every time step in quarter-hourly feed-in is less than or equal to production + battery power",
            "quarterHourlyFeedInHighProductionBatteryPower" to "Time step in quarter-hourly feed-in (%d) is higher than the production and battery power (%d)",
            "incompatibleQuarterHourly" to "Quarter hourly feed-in is not the same length for Quarter hourly production",
            "incompatibleStartTimeQuarterHourly" to "Quarter hourly feed-in start time is not the same for Quarter hourly production",
            "pvInstalledLow" to "PV installed power stays lower than 5000kW",
            "pvInstalledHigh" to "PV installed power should not be larger dan 5000kW",
        ),
        "electricity" to mapOf(
            "kleinverbruikOrGrootverbruikNoDefined" to "Small or large consumption type is not defined",
            "contractedCapacityNotProvided" to "Connection delivery capacity is not provided",
            "physicalCapacityNotProvide" to "Physical connection capacity is not provided",
            "contractedDeliveryCapacityValid" to "Contracted delivery capacity is valid %d",
            "contractedDeliveryCapacityExceeds" to "Contracted delivery capacity %d kW exceeds physical capacity %d kW",
            "connectionFeedInCapacityNotProvide" to "Connection feed in capacity is not provided",
            "feedInLowerPhysicalCapacity" to "Feed-in capacity %d is lower than the physical capacity %d kW",
            "feedInExceedPhysicalCapacity" to "Feed-in capacity %d exceeds physical capacity %d kW",
            "annualElectricityProductionNotProvided" to "Annual electricity production is not provided",
            "annualElectricityFeedInNotProvided" to "Annual electricity feed in is not provided",
            "annualProductionFeedInValid" to "Annual pv production %d is valid, it is more than annual electricity feed-in %d",
            "annualProductionFeedInInvalid" to "Annual PV production %d is less than feed-in %d",

            "quarterHourlyFeedIn_kWh" to "Quarter hourly feed in is not provided",
            "withConnection" to "Electricity with connection",
            "withoutConnection" to "Electricity without connection",
            "quarterHourlyDeliveryDataNotProvided" to "Quarter-hourly delivery data is not provided",
            "quarterHourlyDeliveryDataHolesExceed" to "Quarter-hourly delivery data has a gap of %d hours, exceeding the allowed limit",
            "quarterHourlyDeliveryDataValid" to "Quarter-hourly delivery data has no gaps exceeding the limit",

            "quarterHourlyProductionDataNotProvided" to "Quarter-hourly production data is not provided",
            "quarterHourlyProductionDataHolesExceed" to "Quarter-hourly production data has a gap of %d hours, exceeding the allowed limit",
            "quarterHourlyProductionDataValid" to "Quarter-hourly production data has no gaps exceeding the limit",
            "quarterHourlyProductionCannotBeAllZero" to "Quarter-hourly production data cannot be all zeroes when supply is present",
            "quarterHourlyProductionValid" to "Quarter-hourly production data is valid",


            // quarter
            "notEnoughValues" to "Not enough values for year: needed %d got %d",
            "annualFeedInMismatch" to "Annual feed in (%d) mismatch the total quarter hourly feed in (%d)",
            "annualFeedInMismatch" to "Annual feed in (%d) matches the total quarter hourly feed in (%d)",

            "quarterHourlyDeliveryLowContractedCapacityKw" to "Quarter-hourly stays lower than the Contracted CapacityKw (%d)",
            "quarterHourlyDeliveryHighContractedCapacityKw" to "Quarter-hourly shouldn't go higher than the Contracted CapacityKw (%d)"

            ),
        "grootverbruik" to mapOf(
            "notProvided" to "Large consumption data is not provided",
            "contractedCapacityNotProvided" to "Connection delivery capacity is not provided for large consumption",
            "physicalCapacityNotProvide" to "Physical connection capacity is not provided for large consumption",
            "connectionFeedInCapacityNotProvide" to "Connection feed in capacity is not provided for large consumption",
            "valid" to "Physical connection capacity is within limits (3x80A) for large consumption ",
            "invalid" to "Physical connection capacity %d is below (3x80A) for large consumption",
            "notApplicable" to "Large consumption validations are not applicable",
        ),
        "kleinverbruik" to mapOf(
            "notProvided" to "Small consumption data is not provided",
            "valid" to "Small consumption connection capacity is within limits",
            "exceedsLimit" to "Small consumption connection capacity %d exceeds limit (3x80A)",
            "invalid" to "Small consumption connection capacity is invalid",
            "notApplicable" to "Small consumption validations are not applicable",
        ),
        "storage" to mapOf(
            "batteryCapacityNotProvided" to "Battery Capacity is not provided",
            "batteryPowerNotProvided" to "Battery Power is not provided",
            "batteryWithPowerAndCapacity" to "Battery has Power and Capacity",
            "withoutConnection" to "Without Battery",
        ),
        "naturalGas" to mapOf(
            "annualDeliveryNotProvided" to "Natural gas annual delivery is not provided",
            "withoutConnection" to "Without Natural gas connection",
            "hourlyDeliveryNotProvided" to "Hourly delivery data is not provided",
            "annualGasDeliveryValid" to "Annual gas delivery matches total hourly delivery (%d m³)",
            "annualGasDeliveryMismatch" to "Annual gas delivery %d m³ does not match total hourly delivery %d m³"
        ),

        "transport" to mapOf(
            "carsPowerNotProvided" to "Cars power per charge point is not provided",
            "carsPowerValid" to "Cars power per charge point is valid %d",
            "carsPowerInvalid" to "Cars power per charge point %d is outside the valid range (3..150 kW)",

            "trucksPowerNotProvided" to "Trucks power per charge point is not provided",
            "trucksPowerValid" to "Trucks power per charge point is valid %d",
            "trucksPowerInvalid" to "Trucks power per charge point %d is outside the valid range (3..150 kW)",

            "vansPowerNotProvided" to "Vans power per charge point is not provided",
            "vansPowerValid" to "Vans power per charge point is valid %d",
            "vansPowerInvalid" to "Vans power per charge point %d is outside the valid range (3..150 kW)",

            "distanceCarsNotProvided" to "Cars travel distances is not provided",
            "distanceCarsValid" to "Cars travel distances are valid",
            "distanceCarsInvalid" to "Cars travel distance %d is outside the valid range (5k..100k km)",

            "distanceTrucksNotProvided" to "Trucks travel distances is not provided",
            "distanceTrucksValid" to "Trucks travel distances are valid",
            "distanceTrucksInvalid" to "Trucks travel distance %d is outside the valid range (5k..100k km)",

            "distanceVansNotProvided" to "Vans travel distances is not provided",
            "distanceVansValid" to "Vans travel distances are valid",
            "distanceVansInvalid" to "Vans travel distance %d is outside the valid range (5k..100k km)",

            "electricCarsValid" to "Number of Electric Cars is lower than the total of Cars",
            "electricCarsInvalid" to "Number of electric cars %d exceeds the total number of cars %d",

            "electricTrucksValid" to "Number of Electric Trucks is lower than the total of Trucks",
            "electricTrucksInvalid" to "Number of electric trucks %d exceeds the total number of trucks %d",

            "electricVansValid" to "Number of Electric Vans is lower than the total of Vans",
            "electricVansInvalid" to "Number of electric vans %d exceeds the total number of vans %d",

            "quarterHourlyDeliveryLowContractedCapacityKw" to "Kwartuur levering blijft lager dan de Contractuele CapaciteitKw (%d)",
            "quarterHourlyDeliveryHighContractedCapacityKw" to "Kwartuur levering mag niet hoger zijn dan de Contractuele CapaciteitKw (%d)",
        ),
    ),
    Language.nl to mapOf(
        "gridConnection" to mapOf(
            "totalPowerChargePointsValid" to "Totale laadvermogen is geldig",
            "totalPowerChargePointsInvalid" to "Totale laadvermogen %d overschrijdt de toegestane capaciteit %d",
            "quarterHourlyFeedInLowProductionBatteryPower" to "Elke kwartierwaarde in feed-in is minder dan of gelijk aan de productie plus de batterijcapaciteit.",
            "quarterHourlyFeedInHighProductionBatteryPower" to "De waarde in dit kwartier van de feed-in (%d) is hoger dan de productie en batterijcapaciteit samen (%d).",
            "incompatibleQuarterHourly" to "De kwartierwaarden van de feed-in en productie hebben niet dezelfde lengte.",
            "incompatibleStartTimeQuarterHourly" to "De starttijd van de kwartierwaarden voor feed-in komt niet overeen met die van de productie.",
            "pvInstalledLow" to "PV geïnstalleerd vermogen blijft lager dan 5000 kW",
            "pvInstalledHigh" to "PV geïnstalleerd vermogen mag niet groter zijn dan 5000 kW",
        ),
        "electricity" to mapOf(
            "kleinverbruikOrGrootverbruikNoDefined" to "Klein- of grootverbruikstype is niet gedefinieerd",
            "contractedCapacityNotProvided" to "Gecontracteerd vermogen ontbreekt",
            "physicalCapacityNotProvide" to "Fysieke aansluitcapaciteit ontbreekt",
            "contractedDeliveryCapacityValid" to "Gecontracteerde aansluitcapaciteit %d is geldig",
            "contractedDeliveryCapacityExceeds" to "Gecontracteerde aansluitcapaciteit %d kW overschrijdt fysieke capaciteit %d kW",
            "connectionFeedInCapacityNotProvide" to "Gecontracteerd transportvermogen voor teruglevering ontbreekt",
            "feedInLowerPhysicalCapacity" to "Gecontracteerd transportvermogen teruglevering %d valt binnen de fysieke capaciteit %d kW",
            "feedInExceedPhysicalCapacity" to "Gecontracteerd transportvermogen teruglevering %d overschrijdt fysieke capaciteit %d kW",

            "annualElectricityProductionNotProvided" to "Jaartotaal elektriciteitsproductie ontbreekt",
            "annualElectricityFeedInNotProvided" to "Jaartotaal teruglevering van elektriciteit ontbreekt",
            "annualProductionFeedInValid" to "Jaartotaal PV-productie %d is geldig, het is meer dan de jaarlijkse teruglevering %d",
            "annualProductionFeedInInvalid" to "Jaartotaal PV-productie %d is minder dan teruglevering %d",
            "quarterHourlyDeliveryDataNotProvided" to "Kwartiergegevens voor levering zijn niet opgegeven",
            "quarterHourlyDeliveryDataHolesExceed" to "Kwartiergegevens voor levering bevatten een gat van %d uur, boven de toegestane limiet",
            "quarterHourlyDeliveryDataValid" to "Kwartiergegevens voor levering hebben geen gaten boven de limiet",

            "quarterHourlyProductionDataNotProvided" to "Kwartiergegevens van de productie zijn niet opgegeven",
            "quarterHourlyProductionDataHolesExceed" to "Kwartiergegevens van de productie hebben een gat van %d uur, dat de toegestane limiet overschrijdt",
            "quarterHourlyProductionDataValid" to "Kwartiergegevens van de productie hebben geen gaten die de limiet overschrijden",

            "quarterHourlyProductionCannotBeAllZero" to "Kwartierwaarden voor productie mogen niet allemaal nul zijn als er opwek is",
            "quarterHourlyProductionValid" to "Kwartierwaarden voor productie zijn geldig",
        ),
        "grootverbruik" to mapOf(
            "notProvided" to "Data voor grootverbruik ontbreekt",
            "contractedCapacityNotProvided" to "Gecontracteerd vermogen voor grootverbruik ontbreekt",
            "physicalCapacityNotProvide" to "Fysieke aansluitcapaciteit voor grootverbruik ontbreekt",
            "connectionFeedInCapacityNotProvide" to "Gecontracteerd transportvermogen teruglevering voor grootverbruik ontbreekt",
            "valid" to "Fysieke aansluitcapaciteit voor grootverbruik voldoet aan het minimum (3x80A)",
            "invalid" to "Fysieke aansluitcapaciteit %d is lager dan het minimum (3x80A) voor grootverbruik",
            "notApplicable" to "Validaties voor grootverbruik zijn niet van toepassing",
        ),
        "kleinverbruik" to mapOf(
            "notProvided" to "Data voor kleinverbruik ontbreekt",
            "valid" to "Kleinverbruik-aansluitcapaciteit is binnen de limiet",
            "exceedsLimit" to "Kleinverbruik-aansluitcapaciteit %d overschrijdt de limiet (3x80A)",
            "invalid" to "Kleinverbruik-aansluitcapaciteit is ongeldig",
            "notApplicable" to "Validaties voor kleinverbruik zijn niet van toepassing",
        ),
        "storage" to mapOf(
            "batteryCapacityNotProvided" to "Batterijcapaciteit is niet opgegeven",
            "batteryPowerNotProvided" to "Batterijvermogen is niet opgegeven",
            "batteryWithPowerAndCapacity" to "Batterij heeft Vermogen en Capaciteit",
            "withoutConnection" to "Zonder Batterij"
        ),
        "naturalGas" to mapOf(
            "annualDeliveryNotProvided" to "Jaarlijkse gaslevering is niet opgegeven",
            "hourlyDeliveryNotProvided" to "Uurleveringsgegevens zijn niet opgegeven",
            "annualGasDeliveryValid" to "Jaarlijkse gaslevering komt overeen met de totale uurlevering",
            "annualGasDeliveryMismatch" to "Jaarlijkse gaslevering %d m³ komt niet overeen met de totale uurlevering %d m³",
            "withoutConnection" to "Zonder aardgasaansluiting"
        ),
        "transport" to mapOf(
            "carsPowerNotProvided" to "Vermogen per laadpunt voor auto's ontbreekt",
            "carsPowerValid" to "Vermogen per laadpunt voor auto's is geldig %d",
            "carsPowerInvalid" to "Vermogen per laadpunt voor auto's %d ligt buiten het toegestane bereik (3..150 kW)",

            "trucksPowerNotProvided" to "Vermogen per laadpunt voor vrachtwagens ontbreekt",
            "trucksPowerValid" to "Vermogen per laadpunt voor vrachtwagens is geldig %d",
            "trucksPowerInvalid" to "Vermogen per laadpunt voor vrachtwagens %d ligt buiten het toegestane bereik (3..150 kW)",

            "vansPowerNotProvided" to "Vermogen per laadpunt voor bestelwagens ontbreekt",
            "vansPowerValid" to "Vermogen per laadpunt voor bestelwagens is geldig %d",
            "vansPowerInvalid" to "Vermogen per laadpunt voor bestelwagens %d ligt buiten het toegestane bereik (3..150 kW)",

            "distanceCarsNotProvided" to "Afstand afgelegd door auto's ontbreekt",
            "distanceCarsValid" to "Afstand afgelegd door auto's is geldig",
            "distanceCarsInvalid" to "Afstand afgelegd door auto's %d ligt buiten het toegestane bereik (5k..100k km)",

            "distanceTrucksNotProvided" to "Afstand afgelegd door vrachtwagens ontbreekt",
            "distanceTrucksValid" to "Afstand afgelegd door vrachtwagens is geldig",
            "distanceTrucksInvalid" to "Afstand afgelegd door vrachtwagens %d ligt buiten het toegestane bereik (5k..100k km)",

            "distanceVansNotProvided" to "Afstand afgelegd door bestelwagens ontbreekt",
            "distanceVansValid" to "Afstand afgelegd door bestelwagens is geldig",
            "distanceVansInvalid" to "Afstand afgelegd door bestelwagens %d ligt buiten het toegestane bereik (5k..100k km)",

            "electricCarsValid" to "Aantal elektrische auto's valt binnen het totale aantal auto's",
            "electricCarsInvalid" to "Aantal elektrische auto's %d overschrijdt het totale aantal auto's %d",

            "electricTrucksValid" to "Aantal elektrische vrachtwagens valt binnen het totale aantal vrachtwagens",
            "electricTrucksInvalid" to "Aantal elektrische vrachtwagens %d overschrijdt het totale aantal vrachtwagens %d",

            "electricVansValid" to "Aantal elektrische bestelwagens valt binnen het totale aantal bestelwagens",
            "electricVansInvalid" to "Aantal elektrische bestelwagens %d overschrijdt het totale aantal bestelwagens %d",
        ),
    )
)

// Translation function with fallback to English
fun translate(key: String, vararg args: Any?): String {
    val (module, translationKey) = key.split(".").let { it[0] to it[1] }

    val translation = translations[getLanguage()]?.get(module)?.get(translationKey)
        ?: translations[Language.en]?.get(module)?.get(translationKey)
        ?: key // Fallback to key itself if translation is missing
    return if (args.isEmpty()) {
        translation
    } else {
        replacePlaceholders(translation, *args)
    }
}

fun replacePlaceholders(template: String, vararg args: Any?): String {
    var result = template
    args.forEach { arg ->
        result = result.replaceFirst("%d", arg.toString())
    }
    return result
}
