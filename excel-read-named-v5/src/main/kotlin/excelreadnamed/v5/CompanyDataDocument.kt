package com.zenmo.excelreadnamed.v5

import com.zenmo.zummon.companysurvey.*
import com.zenmo.zummon.companysurvey.TimeSeriesUnit
import kotlinx.datetime.Instant
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class CompanyDataDocument(
    private val workbook: XSSFWorkbook,
    private val projectProvider: ProjectProvider = ProjectProvider.default,
) {
    val errors: MutableList<String> = mutableListOf()

    constructor(inputStream: java.io.InputStream, projectProvider: ProjectProvider = ProjectProvider.default)
            : this(XSSFWorkbook(inputStream), projectProvider)

    companion object {
        fun fromFile(fileName: String): CompanyDataDocument {
            val workbook = XSSFWorkbook(fileName)
            return CompanyDataDocument(workbook)
        }

        fun fromResource(resourceName: String): CompanyDataDocument {
            val inputStream = ClassLoader.getSystemResourceAsStream(resourceName)
            if (inputStream == null) {
                throw Exception("Resource not found: $resourceName")
            }
            return CompanyDataDocument(inputStream)
        }
    }

    fun getSurveyObject(): Survey {
        var companyName = getStringField("companyName")
        val project = projectProvider.getProjectByEnergiekeRegioId(
            getIntegerField("projectId")
        )
        val realSurvey =
            Survey(
                companyName = companyName,
                zenmoProject = project.name ?: "Energieke Regio project ${project.energiekeRegioId}",
                personName = "Contactpersoon",
                project = project,
                includeInSimulation = readCompletenessField(),
                addresses = listOf(
                    Address(
                        id = UUID.randomUUID(),
                        street = getStringField("street"),
                        houseNumber = getHouseNumber(),
                        city = getStringField("city"),
                        gridConnections =
                        listOf(
                            GridConnection(
                                electricity =
                                Electricity(
                                    hasConnection = true,
                                    annualElectricityDelivery_kWh = getNumericField("annualElectricityDeliveryKwh").toInt(),
                                    annualElectricityFeedIn_kWh =
                                    getNumericField("annualElectricityFeedinKwh").toInt(),
                                    // ean = "123456789012345678",
                                    quarterHourlyDelivery_kWh = getElectricityDeliveryTimeSeries(),
                                    quarterHourlyFeedIn_kWh = getElectricityFeedIn(),
                                    quarterHourlyProduction_kWh = getElectricityProduction(),
                                    grootverbruik = CompanyGrootverbruik(
                                        contractedConnectionDeliveryCapacity_kW =
                                        getNumericField(
                                            "contractedConnectionDeliveryCapacityKw"
                                        )
                                            .toInt(),
                                        contractedConnectionFeedInCapacity_kW =
                                        getNumericField(
                                            "contractedConnectionFeedinCapacityKw"
                                        )
                                            .toInt(),
                                        // physicalCapacityKw = 300,
                                    ),
                                ),
                                supply = Supply(
                                    hasSupply = true,
                                    pvInstalledKwp =
                                    getNumericField(
                                        "pvInstalledKwp"
                                    )
                                        .toInt(),
                                    pvOrientation =
                                    PVOrientation
                                        .SOUTH,
                                    /*pvPlanned = true,
                                    pvPlannedKwp = 200,
                                    pvPlannedOrientation = PVOrientation.EAST_WEST,
                                    pvPlannedYear = 2022,
                                    windInstalledKw = 300f,
                                    windPlannedKw = 400f,
                                    otherSupply = "Other supply",
                                    missingPvReason = MissingPvReason.OTHER,*/
                                ),
                                naturalGas =
                                NaturalGas(
                                    ean =
                                    "123456789012345678",
                                    hasConnection =
                                    true,
                                    annualDelivery_m3 =
                                    getNumericField(
                                        "naturalGasAnnualDeliveryM3"
                                    )
                                        .toInt(),
                                    /*hourlyValuesFiles = listOf(
                                        File(
                                            blobName = "qwerty-uurwaarden-2022.csv",
                                            originalName = "uurwaarden-2022.csv",
                                            contentType = "text/csv",
                                            size = 1000,
                                        ),
                                    ),*/
                                    percentageUsedForHeating =
                                    100,
                                ),
                                /*heat = Heat(
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
                                surveyFeedback = "Survey feedback",*/
                                transport =
                                Transport(
                                    hasVehicles =
                                    true,
                                    // numDailyCarAndVanCommuters = 14,
                                    // numDailyCarVisitors = 5,
                                    // numCommuterAndVisitorChargePoints = 2,
                                    cars =
                                    Cars(
                                        numCars =
                                        getNumericField(
                                            "numCars"
                                        )
                                            .toInt(),
                                        numElectricCars =
                                        getNumericField(
                                            "numElectricCars"
                                        )
                                            .toInt(),
                                        numChargePoints =
                                        getNumericField(
                                            "numChargePoints"
                                        )
                                            .toInt(),
                                        powerPerChargePointKw =
                                        (getNumericField(
                                            "chargePointsTotalPowerKw"
                                        ) /
                                                getNumericField(
                                                    "numChargePoints"
                                                ))
                                            .toFloat(),
                                        annualTravelDistancePerCarKm =
                                        getNumericField(
                                            "annualTravelDistancePerCarKm"
                                        )
                                            .toInt(),
                                        // numPlannedElectricCars = 0,
                                        // numPlannedHydrogenCars = 2,
                                    ),
                                    trucks =
                                    Trucks(
                                        numTrucks =
                                        getNumericField(
                                            "numTrucks"
                                        )
                                            .toInt(),
                                        numElectricTrucks =
                                        getNumericField(
                                            "numElectricTrucks"
                                        )
                                            .toInt(),
                                        numChargePoints =
                                        0,
                                        powerPerChargePointKw =
                                        0f,
                                        annualTravelDistancePerTruckKm =
                                        getNumericField(
                                            "annualTravelDistancePerTruckKm"
                                        )
                                            .toInt(),
                                        // numPlannedElectricTrucks = 0,
                                        // numPlannedHydrogenTrucks = 2,
                                    ),
                                    vans =
                                    Vans(
                                        numVans =
                                        getNumericField(
                                            "numVans"
                                        )
                                            .toInt(),
                                        numElectricVans =
                                        getNumericField(
                                            "numElectricVans"
                                        )
                                            .toInt(),
                                        numChargePoints =
                                        0,
                                        powerPerChargePointKw =
                                        0f,
                                        annualTravelDistancePerVanKm =
                                        getNumericField(
                                            "annualTravelDistancePerVanKm"
                                        )
                                            .toInt(),
                                        // numPlannedElectricVans = 0,
                                        // numPlannedHydrogenVans = 2,
                                    ),
                                    /*otherVehicles = OtherVehicles(
                                        hasOtherVehicles = true,
                                        description = "Other vehicles description",
                                    )*/
                                )
                            )
                        )
                    )
                )
            )

        return realSurvey
    }

    private fun getProject(): String =
        "EnergiekeRegio_" + getStringField("projectId")

    private fun getHouseNumber(): Int {
        // TODO support houseletters
        val value = getStringField("houseNumberCombined")
        val numberPart = "\\d+".toRegex().find(value)
        if (numberPart == null) {
            errors.add("Could not parse house number from $value")
            return 0
        }
        return numberPart.value.toInt()
    }

    private fun getSingleCell(field: String): XSSFCell {
        val name = workbook.getName(field)
        if (name == null) {
            throw FieldNotPresentException(field)
        }

        val ref = AreaReference(name.refersToFormula, workbook.spreadsheetVersion)
        check(ref.isSingleCell) { "Named range $field should be a single cell" }

        return ref.firstCell.dereference()
    }

    private fun CellReference.dereference() =
        workbook.getSheet(this.sheetName)
            .getRow(this.row)
            .getCell(this.col.toInt())

    private fun getNumericField(field: String): Double {
        val cell = getSingleCell(field)
        return cell.numericCellValue
    }

    fun getIntegerField(field: String): Int {
        val cell = getSingleCell(field)
        return cell.rawValue.toInt()
    }

    fun getStringField(field: String): String {
        val cell = getSingleCell(field)

        return try {
            cell.stringCellValue
        } catch (e: IllegalStateException) {
            return cell.numericCellValue.toString()
        }
    }

    private val controleSheetName = "Controle"

    fun getControleSheet(): XSSFSheet {
        return workbook.getSheet(controleSheetName) ?: throw Exception("""Sheet "$controleSheetName" not found""")
    }

    /**
     * Check field A1 which indicates completeness.
     * TODO: make this a named field
     */
    fun readCompletenessField(): Boolean {
        val stringValue = getControleSheet()
            .getRow(0)
            .getCell(0)
            .stringCellValue

        val falseMatch = "QuickScan niet mogelijk"
        val trueMatch = "QuickScan mogelijk"

        return when (stringValue) {
            falseMatch -> false
            trueMatch -> true
            else -> throw Exception("""Found "$stringValue" in field $controleSheetName.A1, expected "$falseMatch" or "$trueMatch"""")
        }
    }

    fun getUsageTable(field: String): FloatArray {
        val numericValueName = workbook.getName(field)
        val ref = AreaReference(numericValueName.refersToFormula, workbook.spreadsheetVersion)
        val cellReference = ref.firstCell
        // val sheet = workbook.getSheet(cellReference.sheetName)
        // val row = sheet.getRow(cellReference.row)
        // val cell = row.getCell(cellReference.col.toInt())
        // val numericValue =
        // workbook.getSheet(cellReference.sheetName).getRow(cellReference.row).getCell(cellReference.col.toInt())
        // return numericValue.numericCellValue

        // Check if table is complete
        val tableComplete =
            workbook.getSheet(cellReference.sheetName)
                .getRow(cellReference.row - 6)
                .getCell(cellReference.col.toInt() + 1)
                .booleanCellValue

        println("Table complete? $tableComplete")

        if (tableComplete) {
            val numCols = ref.lastCell.col - ref.firstCell.col + 1
            if (numCols != 2) {
                throw IllegalArgumentException("Number of columns in arrayField should be 2!")
            }

            val numRows = ref.lastCell.row - ref.firstCell.row + 1
            var result = FloatArray(numRows)

            for (i in 0 until numRows) {
                val usage_kWh =
                    workbook.getSheet(cellReference.sheetName)
                        .getRow(cellReference.row + i)
                        .getCell(cellReference.col.toInt() + 1)
                        .numericCellValue
                        .toFloat()
                // println("cell value: ${cell.numericCellValue}")

                val timeStamp = ref.firstCell
                    .dereference()
                    .dateCellValue
                    .toInstant()
                val kotlinTimeStamp = Instant.fromEpochMilliseconds(timeStamp.toEpochMilli())
                val currentUsage = TimeSeriesDataPoint(kotlinTimeStamp, usage_kWh)
                result[i] = currentUsage.value
            }
        }

        return floatArrayOf()
    }

    fun getArrayField(field: String): FloatArray {
        val numericValueName = workbook.getName(field)

        val ref = AreaReference(numericValueName.refersToFormula, workbook.spreadsheetVersion)

        val cellReference = ref.firstCell
        // val sheet = workbook.getSheet(cellReference.sheetName)
        // val row = sheet.getRow(cellReference.row)
        // val cell = row.getCell(cellReference.col.toInt())
        // val numericValue =
        // workbook.getSheet(cellReference.sheetName).getRow(cellReference.row).getCell(cellReference.col.toInt())
        // return numericValue.numericCellValue

        val numCols = ref.lastCell.col - ref.firstCell.col + 1
        if (numCols != 2) {
            throw IllegalArgumentException("Number of columns in arrayField should be 2!")
        }

        val numRows = ref.lastCell.row - ref.firstCell.row + 1
        println("numRows: $numRows")
        // var tableArray = emptyArray<Double>()
        var tableArray = FloatArray(numRows)
        // var tableArray = Array<Double>(numRows)

        for (i in 0 until numRows) {
            val cell =
                workbook.getSheet(cellReference.sheetName)
                    .getRow(cellReference.row + i)
                    .getCell(cellReference.col.toInt() + 1)
            // println("cell value: ${cell.numericCellValue}")
            tableArray[i] = cell.numericCellValue.toFloat()
        }
        return tableArray
    }

    /**
     * This does not take into account multiple production meters
     */
    fun getElectricityProduction(): TimeSeries? =
        try {
            getElectricityProductionV2()
        } catch (e: FieldNotPresentException) {
            getElectricityProductionV1()
        }

    fun getElectricityProductionV2(): TimeSeries? =
        getTimeSeriesV2(SoortProfiel.zon)

    fun getElectricityProductionV1(): TimeSeries? =
        getTimeSeriesV1("quarterHourlyPvProductionKwh", TimeSeriesType.ELECTRICITY_PRODUCTION)

    fun getElectricityFeedIn(): TimeSeries? =
        try {
            getElectricityFeedInV2()
        } catch (e: FieldNotPresentException) {
            getElectricityFeedInV1()
        }

    fun getElectricityFeedInV1(): TimeSeries? =
        getTimeSeriesV1("quarterHourlyElectricityFeedInKwh", TimeSeriesType.ELECTRICITY_FEED_IN)

    fun getElectricityFeedInV2(): TimeSeries? = getTimeSeriesV2(SoortProfiel.teruglevering)

    fun getElectricityDeliveryTimeSeries(): TimeSeries? =
        try {
            getElectricityDeliveryTimeSeriesV2()
        } catch (e: FieldNotPresentException) {
            getElectricityDeliveryTimeSeriesV1()
        }

    fun getElectricityDeliveryTimeSeriesV1(): TimeSeries? =
        getTimeSeriesV1("quarterHourlyElectricityDeliveryKwh", TimeSeriesType.ELECTRICITY_DELIVERY)

    fun getTimeSeriesV1(fieldName: String, type: TimeSeriesType): TimeSeries? {
        val firstCell = getFirstCellOfNamedRange(fieldName)
        if (!isTimeSeriesTableComplete(firstCell)) {
            return null
        }

        val year = getYearOfTimeSeries(firstCell)
        val start = yearToFirstOfJanuary(year)
        val values = getArrayField(fieldName)

        return TimeSeries(
            type = type,
            start = start,
            timeStep = when (type) {
                TimeSeriesType.GAS_DELIVERY -> 1.hours
                else -> 15.minutes
            },
            unit = TimeSeriesUnit.KWH,
            values = values
        )
    }

    fun getElectricityDeliveryTimeSeriesV2(): TimeSeries? = getTimeSeriesV2(SoortProfiel.levering)

    fun getTimeSeriesV2(soortProfiel: SoortProfiel): TimeSeries? {
        val metadata = getTimeSeriesMetaDataList().find { it.soortProfiel == soortProfiel }
        if (metadata == null) {
            return null
        }

        return TimeSeries(
            type = soortProfiel.timeSeriesType(),
            start = yearToFirstOfJanuary(metadata.jaar),
            timeStep = metadata.resolutieMinuten.minutes,
            unit = metadata.eenheid,
            values = getArrayField("profileData${metadata.index}")
        )
    }

    fun isTimeSeriesTableComplete(firstCell: CellReference): Boolean {
        // Find the cell that indicates if the table is complete
        return workbook.getSheet(firstCell.sheetName)
            .getRow(firstCell.row - 6)
            .getCell(firstCell.col.toInt() + 1)
            .booleanCellValue
    }

    fun getYearOfTimeSeries(firstCell: CellReference): Int {
        val year = workbook.getSheet(firstCell.sheetName)
            .getRow(1)
            .getCell(firstCell.col.toInt() + 1)
            .numericCellValue
            .toInt()

        if (year < 2000 || year > 2100) {
            throw IllegalArgumentException("Year of time series should be between 2000 and 2100")
        }

        return year
    }

    fun getFirstCellOfNamedRange(rangeName: String): CellReference {
        val numericValueName = workbook.getName(rangeName)
        val ref = AreaReference(numericValueName.refersToFormula, workbook.spreadsheetVersion)
        return ref.firstCell
    }

    fun getTimeSeriesMetaDataList(): List<TimeSeriesMetadata> =
        (1..6)
            .map { getTimeSeriesMataData(it) }
            .filterNotNull()
            .filter { it.profielCompleet }

    fun getTimeSeriesMataData(i: Int): TimeSeriesMetadata? {
        val name = workbook.getName("profileMetadata$i")
        // older versions of the sheet don't have this field
        if (name == null) {
            throw FieldNotPresentException("profileMetadata$i")
        }
        val ref = AreaReference(name.refersToFormula, workbook.spreadsheetVersion)

        val lastCellInFirstColumn = CellReference(ref.lastCell.row, ref.firstCell.col)
        val firstColumnRef = AreaReference(ref.firstCell, lastCellInFirstColumn, workbook.spreadsheetVersion)

        val eenheidString = findCellRefWithStringValue(firstColumnRef, "eenheid").oneToTheRight().dereference().stringCellValue.uppercase()
            .filter { it.isLetter() or it.isDigit() }

        if (eenheidString == "") {
            return null
        }

        val soortProfielString = findCellRefWithStringValue(firstColumnRef, "soort profiel").oneToTheRight().dereference().stringCellValue
        if (soortProfielString == "") {
            return null
        }

        return TimeSeriesMetadata(
            index = i,
            jaar = findCellRefWithStringValue(firstColumnRef, "jaar").oneToTheRight().dereference().numericCellValue.toInt(),
            tijdzone = findCellRefWithStringValue(firstColumnRef, "tijdzone").oneToTheRight().dereference().stringCellValue,
            resolutieMinuten = findCellRefWithStringValue(firstColumnRef, "resolutie in minuten").oneToTheRight().dereference().numericCellValue.toInt(),
            eenheid = TimeSeriesUnit.valueOf(eenheidString),
            soortProfiel = SoortProfiel.valueOf(soortProfielString),
            profielCompleet = findCellRefWithStringValue(firstColumnRef, "Profiel compleet").oneToTheRight().dereference().booleanCellValue
        )
    }

    fun findCellRefWithStringValue(area: AreaReference, target: String): CellReference {
        val result = area.allReferencedCells.find { cell ->
            val value = cell.dereference().stringCellValue
            value.lowercase() == target.lowercase()
        }

        if (result == null) {
            throw Exception("Could not find cell with value $target in area $area")
        }

        return result
    }
}
