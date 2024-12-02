package companysurvey

import com.zenmo.zummon.companysurvey.*
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class QuarterValidationTest {
    val electricityValidator = ElectricityValidator()
    val gridConnectionValidator = GridConnectionValidator()

    @Test
    fun validateQuarterHourlyDeliveryNoProvide() {
        val electricity = Electricity(
            quarterHourlyDelivery_kWh = null
        )
        val result = electricityValidator.validateQuarterHourlyDeliveryData(electricity)
        assertEquals(Status.MISSING_DATA, result.status)
        assertEquals("Quarter-hourly delivery data is not provided", result.message)
    }

    @Test
    fun validateQuarterHourlyDeliveryDataValidNoHoles() {
        val electricity = Electricity(
            quarterHourlyDelivery_kWh = TimeSeries(
                type = TimeSeriesType.ELECTRICITY_DELIVERY,
                start = Instant.parse("2022-01-01T00:00:00Z"),
                values = floatArrayOf(1.2f, 2.2f, 3.2f, 4.2f)
            )
        )
        val result = electricityValidator.validateQuarterHourlyDeliveryData(electricity)
        assertEquals(Status.VALID, result.status)
        assertEquals("Quarter-hourly delivery data has no gaps exceeding the limit", result.message)
    }

    @Test
    fun validateQuarterHourlyDeliveryDataSmallGaps() {
        val electricity = Electricity(
            quarterHourlyDelivery_kWh = TimeSeries(
                type = TimeSeriesType.ELECTRICITY_DELIVERY,
                start = Instant.parse("2022-01-01T00:00:00Z"),
                values = floatArrayOf(1.2f, 0.0f, 0.0f, 2.2f, 0.0f, 3.2f, 4.2f) // Only 2 nulls in a row
            )
        )
        val result = electricityValidator.validateQuarterHourlyDeliveryData(electricity)
        assertEquals(Status.VALID, result.status)
        assertEquals("Quarter-hourly delivery data has no gaps exceeding the limit", result.message)
    }

    @Test
    fun validateQuarterHourlyDeliveryDataGapsExceed() {
        val electricity = Electricity(
            quarterHourlyDelivery_kWh = TimeSeries(
                type = TimeSeriesType.ELECTRICITY_DELIVERY,
                start = Instant.parse("2022-01-01T00:00:00Z"),
                values = FloatArray(385) { 0.0f } + floatArrayOf(1.2f, 2.2f) // 385 nulls, exceeding limit
            )
        )
        val result = electricityValidator.validateQuarterHourlyDeliveryData(electricity)
        assertEquals(Status.INVALID, result.status)
        assertEquals("Quarter-hourly delivery data has a gap of 96 hours, exceeding the allowed limit", result.message)
    }

    @Test
    fun validateQuarterHourlyProductionDataInvalidGaps() {
        val electricity = Electricity(
            quarterHourlyProduction_kWh = TimeSeries(
                type = TimeSeriesType.ELECTRICITY_PRODUCTION,
                start = Instant.parse("2022-01-01T00:00:00Z"),
                values = FloatArray(385) { 0.0f } + floatArrayOf(1.2f, 2.2f)// 385 nulls, exceeding limit
            )
        )
        val result = electricityValidator.validateQuarterHourlyProductionData(electricity)
        assertEquals(Status.INVALID, result.status)
        assertEquals("Quarter-hourly production data has a gap of 96 hours, exceeding the allowed limit", result.message)
    }

    @Test
    fun validateQuarterHourlyDeliveryDataExactLimit() {
        val electricity = Electricity(
            quarterHourlyDelivery_kWh = TimeSeries(
                type = TimeSeriesType.ELECTRICITY_DELIVERY,
                start = Instant.parse("2022-01-01T00:00:00Z"),
                values = FloatArray(384) { 0.0f } + floatArrayOf(1.2f, 2.2f) // 384 nulls, at the limit
            )
        )
        val result = electricityValidator.validateQuarterHourlyDeliveryData(electricity)
        assertEquals(Status.VALID, result.status)
        assertEquals("Quarter-hourly delivery data has no gaps exceeding the limit", result.message)
    }

    @Test
    fun testQuarterHourlyFeedInLowProductionBatteryPower_ValidData() {
        // Set up mock data with valid feed-in, production, and battery power
        val gridConnection = GridConnection(
            electricity = Electricity(
                quarterHourlyFeedIn_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(1.0f, 1.5f, 2.0f)),
                quarterHourlyProduction_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(9.0f, 7.5f, 6.0f))
            ),
            storage = Storage(batteryPowerKw = 0.5f)
        )
        
        val result = gridConnectionValidator.quarterHourlyFeedInLowProductionBatteryPower(gridConnection)

        // Assert the result is valid
        assertEquals(Status.VALID, result.status)
        assertEquals(translate("gridConnection.quarterHourlyFeedInLowProductionBatteryPower"), result.message)
    }

    @Test
    fun testQuarterHourlyFeedInLowProductionBatteryPower_InvalidStartTime() {
        // Set up mock data where start times are different
        val gridConnection = GridConnection(
            electricity = Electricity(
                quarterHourlyFeedIn_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(1.0f, 1.5f, 2.0f)),
                quarterHourlyProduction_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T01:00:00Z"), values = floatArrayOf(1.0f, 1.5f, 2.0f))
            ),
            storage = Storage(batteryPowerKw = 0.5f)
        )
        
        val result = gridConnectionValidator.quarterHourlyFeedInLowProductionBatteryPower(gridConnection)

        // Assert the result is invalid and the message matches the expected translation
        assertEquals(Status.INVALID, result.status)
        assertEquals(translate("gridConnection.incompatibleStartTimeQuarterHourly"), result.message)
    }

    @Test
    fun testQuarterHourlyFeedInLowProductionBatteryPower_InvalidLength() {
        // Set up mock data with mismatched lengths of feed-in and production values
        val gridConnection = GridConnection(
            electricity = Electricity(
                quarterHourlyFeedIn_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(1.0f, 1.5f, 2.0f)),
                quarterHourlyProduction_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(1.0f, 1.5f))
            ),
            storage = Storage(batteryPowerKw = 0.5f)
        )
        val result = gridConnectionValidator.quarterHourlyFeedInLowProductionBatteryPower(gridConnection)

        // Assert the result is invalid and the message matches the expected translation
        assertEquals(Status.INVALID, result.status)
        assertEquals(translate("gridConnection.incompatibleQuarterHourly"), result.message)
    }

    @Test
    fun testQuarterHourlyFeedInLowProductionBatteryPower_HighFeedIn() {
        // Set up mock data with a high feed-in value
        val gridConnection = GridConnection(
            electricity = Electricity(
                quarterHourlyFeedIn_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(2.5f, 3.0f, 3.5f)),
                quarterHourlyProduction_kWh = TimeSeries(type= TimeSeriesType.ELECTRICITY_DELIVERY, start = Instant.parse("2024-01-01T00:00:00Z"), values = floatArrayOf(1.0f, 1.5f, 2.0f))
            ),
            storage = Storage(batteryPowerKw = 0.5f)
        )

        val result = gridConnectionValidator.quarterHourlyFeedInLowProductionBatteryPower(gridConnection)

        // Assert the result is invalid and the message matches the expected translation
        assertEquals(Status.INVALID, result.status)
        assertEquals(translate("gridConnection.quarterHourlyFeedInHighProductionBatteryPower", 2.5f, 1.5f), result.message)
    }
}
