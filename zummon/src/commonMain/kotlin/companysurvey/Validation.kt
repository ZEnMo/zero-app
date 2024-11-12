package com.zenmo.zummon.companysurvey
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun interface Validator<T> {
    fun validate(item: T): List<ValidationResult>
}

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ValidationResult(
    val status: Status,
    val message: String,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class Status {
    VALID,
    INVALID,
    MISSING_DATA,
    NOT_APPLICABLE,
}

@OptIn(ExperimentalJsExport::class)
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
        results.addAll(TransportValidator().validate(gridConnection.transport))
        results.addAll(validateTotalPowerChargePoints(gridConnection))

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
}

class ElectricityValidator : Validator<Electricity> {
    override fun validate(electricity: Electricity): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()

        results.addAll(validateKleinOrGroot(electricity))
        results.add(validateContractedCapacity(electricity))
        results.add(validateAnnualProductionFeedIn(electricity))
        results.add(validateContractedFeedInCapacity(electricity))

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
            "annualElectricityFeedInNotProvided" to "Annual electricity Feed In is not provided",
            "annualProductionFeedInValid" to "Annual pv production %d is valid, it is more than annual electricity feed-in %d",
            "annualProductionFeedInInvalid" to "Annual PV production %d is less than feed-in %d",

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
        ),
    ),
    Language.nl to mapOf(
        "gridConnection" to mapOf(
            "totalPowerChargePointsValid" to "Totale laadvermogen is geldig",
            "totalPowerChargePointsInvalid" to "Totale laadvermogen %d overschrijdt de toegestane capaciteit %d",
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
