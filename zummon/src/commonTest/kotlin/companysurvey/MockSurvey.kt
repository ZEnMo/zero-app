package companysurvey

import com.benasher44.uuid.uuid4
import com.zenmo.zummon.companysurvey.*
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

val mockSurvey = createMockSurvey()

fun createMockSurvey(projectName: String = "Project") = Survey(
    companyName = "Zenmo",
    zenmoProject = projectName,
    personName = "John Doe",
    email = "john@example.com",
    dataSharingAgreed = true,
    addresses = listOf(
        Address(
            id = uuid4(),
            street = "Kerkstraat",
            houseNumber = 35,
            houseLetter = "A",
            houseNumberSuffix = "100",
            postalCode = "1234AB",
            city = "Amsterdam",
            gridConnections = listOf(
                GridConnection(
                    electricity = Electricity(
                        hasConnection = true,
                        annualElectricityDelivery_kWh = 1000,
                        annualElectricityFeedIn_kWh = 2000,
                        ean = "123456789012345678",
                        quarterHourlyValuesFiles = listOf(
                            File(
                                blobName = "qwerty-kwartierwaarden-2021.csv",
                                originalName = "kwartierwaarden-2021.csv",
                                contentType = "text/csv",
                                size = 1000,
                            ),
                            File(
                                blobName = "qwerty-kwartierwaarden-2022.csv",
                                originalName = "kwartierwaarden-2022.csv",
                                contentType = "text/csv",
                                size = 1000,
                            ),
                        ),
                        kleinverbruikOrGrootverbruik = KleinverbruikOrGrootverbruik.GROOTVERBRUIK,
                        grootverbruik = CompanyGrootverbruik(
                            contractedConnectionDeliveryCapacity_kW = 150,
                            contractedConnectionFeedInCapacity_kW = 200,
                            physicalCapacityKw = 300,
                        ),
                        kleinverbruik = CompanyKleinverbruik(
                            connectionCapacity = KleinverbruikElectricityConnectionCapacity._3x63A,
                            consumptionProfile = KleinverbruikElectricityConsumptionProfile.TWO,
                        ),
                        authorizationFile = File(
                            blobName = "authorization.pdf",
                            originalName = "Authorization.pdf",
                            contentType = "application/pdf",
                            size = 10000,
                        ),
                        gridExpansion = GridExpansion(
                            hasRequestAtGridOperator = true,
                            requestedKW = 200,
                            reason = "Mooaar power!",
                        ),
                        quarterHourlyDelivery_kWh = TimeSeries(
                            type = TimeSeriesType.ELECTRICITY_DELIVERY,
                            start = Instant.parse("2022-01-01T00:00:00+01"),
                            values = floatArrayOf(1.2f, 2.2f, 3.2f, 4.2f),
                        ),
                        quarterHourlyFeedIn_kWh = TimeSeries(
                            type = TimeSeriesType.ELECTRICITY_FEED_IN,
                            start = Instant.parse("2022-01-01T00:00:00+01"),
                            values = floatArrayOf(1.2f, 2.2f, 3.2f, 4.2f),
                        ),
                        quarterHourlyProduction_kWh = TimeSeries(
                            type = TimeSeriesType.ELECTRICITY_PRODUCTION,
                            start = Instant.parse("2022-01-01T00:00:00+01"),
                            values = floatArrayOf(1.2f, 2.2f, 3.2f, 4.2f),
                        ),
                        annualElectricityProduction_kWh = 3000,
                    ),
                    supply = Supply(
                        hasSupply = true,
                        pvInstalledKwp = 100,
                        pvOrientation = PVOrientation.SOUTH,
                        pvPlanned = true,
                        pvPlannedKwp = 200,
                        pvPlannedOrientation = PVOrientation.EAST_WEST,
                        pvPlannedYear = 2022,
                        windInstalledKw = 300f,
                        windPlannedKw = 400f,
                        otherSupply = "Other supply",
                        missingPvReason = MissingPvReason.OTHER,
                    ),
                    naturalGas = NaturalGas(
                        ean = "123456789012345678",
                        hasConnection = true,
                        annualDelivery_m3 = 3500,
                        hourlyValuesFiles = listOf(
                            File(
                                blobName = "qwerty-uurwaarden-2022.csv",
                                originalName = "uurwaarden-2022.csv",
                                contentType = "text/csv",
                                size = 1000,
                            ),
                        ),
                        percentageUsedForHeating = 50,
                        hourlyDelivery_m3 = TimeSeries(
                            type = TimeSeriesType.GAS_DELIVERY,
                            start = Instant.parse("2022-01-01T00:00:00+01"),
                            timeStep = 2.hours,
                            values = floatArrayOf(1.2f, 2.2f, 3.2f, 4.2f),
                        )
                    ),
                    heat = Heat(
                        heatingTypes = listOf(HeatingType.GAS_BOILER, HeatingType.DISTRICT_HEATING),
                        sumGasBoilerKw = 28.8f,
                        sumHeatPumpKw = 0f,
                        sumHybridHeatPumpElectricKw = 0f,
                        annualDistrictHeatingDelivery_GJ = 300f,
                        localHeatExchangeDescription = "Local heat exchange description",
                        hasUnusedResidualHeat = false,
                    ),
                    storage = Storage(
                        hasBattery = false,
                        batteryCapacityKwh = 0f,
                        batteryPowerKw = 0f,
                        batterySchedule = "Battery schedule",
                        hasPlannedBattery = true,
                        plannedBatteryCapacityKwh = 100f,
                        plannedBatteryPowerKw = 10f,
                        plannedBatterySchedule = "Planned battery schedule",
                        hasThermalStorage = true,
                    ),
                    mainConsumptionProcess = "Main consumption process",
                    electrificationPlans = "Electrification plans",
                    consumptionFlexibility = "Consumption flexibility",
                    energyOrBuildingManagementSystemSupplier = "EnergyBrothers",
                    surveyFeedback = "Survey feedback",
                    transport = Transport(
                        hasVehicles = false,
                        numDailyCarAndVanCommuters = 14,
                        numDailyCarVisitors = 5,
                        numCommuterAndVisitorChargePoints = 2,
                        cars = Cars(
                            numCars = 2,
                            numElectricCars = 0,
                            numChargePoints = 0,
                            powerPerChargePointKw = 55f,
                            annualTravelDistancePerCarKm = 5500,
                            numPlannedElectricCars = 0,
                            numPlannedHydrogenCars = 2,
                        ),
                        trucks = Trucks(
                            numTrucks = 5,
                            numElectricTrucks = 0,
                            numChargePoints = 0,
                            powerPerChargePointKw = 70f,
                            annualTravelDistancePerTruckKm = 15000,
                            numPlannedElectricTrucks = 0,
                            numPlannedHydrogenTrucks = 2,
                        ),
                        vans = Vans(
                            numVans = 2,
                            numElectricVans = 3,
                            numChargePoints = 0,
                            powerPerChargePointKw = 23f,
                            annualTravelDistancePerVanKm = 23000,
                            numPlannedElectricVans = 0,
                            numPlannedHydrogenVans = 2,
                        ),
                        otherVehicles = OtherVehicles(
                            hasOtherVehicles = true,
                            description = "Other vehicles description",
                        )
                    ),
                )
            ),
        )
    )
)

fun createMockGridConnectionWithInvalidData() = GridConnection(
    electricity = Electricity(
        hasConnection = true,
        annualElectricityDelivery_kWh = 500,
        annualElectricityFeedIn_kWh = 2000,  // Invalid: Feed-in is higher than production
        annualElectricityProduction_kWh = 1000,
        ean = "999999999999999999",
        kleinverbruikOrGrootverbruik = KleinverbruikOrGrootverbruik.GROOTVERBRUIK,
        grootverbruik = CompanyGrootverbruik(
            contractedConnectionDeliveryCapacity_kW = 100,  // Invalid: Exceeds physical capacity
            contractedConnectionFeedInCapacity_kW = 70,   // Invalid: Exceeds physical capacity
            physicalCapacityKw = 50   // Invalid: Less than 55.2 for grootverbruik
        ),
        kleinverbruik = CompanyKleinverbruik(
            connectionCapacity = null
        ),

        ),
    storage = Storage(
        batteryPowerKw = 0f,
    ),
    transport = Transport(
        cars = Cars(
            powerPerChargePointKw = 170f,  // Invalid: Greater than 150 kW
            annualTravelDistancePerCarKm = 200000,  // Invalid: Greater than 100k km
        ),
        trucks = Trucks(
            annualTravelDistancePerTruckKm = 100000,  // Valid
        ),
        vans = Vans(
            numVans = 8,
            numElectricVans = 10,
            powerPerChargePointKw = 200f,  // Invalid: Greater than 150 kW
            annualTravelDistancePerVanKm = 50000,  // Valid
        )
    )
)

fun Survey.changeGridConnection(function: (gridConnection: GridConnection) -> GridConnection): Survey =
    copy(
        addresses = addresses.map {
            it.copy(
                gridConnections = it.gridConnections.map { gridConnection ->
                    function(gridConnection)
                }
            )
        },
    )

fun updateElectricity(): Survey {
    return mockSurvey.changeGridConnection {
        it.copy(
            electricity = it.electricity.copy(kleinverbruikOrGrootverbruik = null)
        )
    }
}
fun updateCapacity(capacity: CompanyGrootverbruik): Survey {
    return mockSurvey.changeGridConnection {
        it.copy(
            electricity = it.electricity.copy(
                grootverbruik = it.electricity.grootverbruik?.copy(
                    contractedConnectionDeliveryCapacity_kW = capacity.contractedConnectionDeliveryCapacity_kW,
                    physicalCapacityKw = capacity.physicalCapacityKw
                )
            )
        )
    }
}

fun updateMockSurveyWithInvalidData(): Survey {
    return mockSurvey.changeGridConnection {
        createMockGridConnectionWithInvalidData()
    }
}