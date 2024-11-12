package companysurvey

import com.benasher44.uuid.uuid4
import com.zenmo.zummon.companysurvey.*
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class ValidationTest {
    @Test
    fun loadSurvey() {
        assertEquals(mockSurvey.companyName, "Zenmo")
        assertTrue(mockSurvey.dataSharingAgreed)
    }

    @Test
    fun testAllValidations() {
        val mockSurvey = createMockSurvey()
        val validationResults = surveyValidator.validate(mockSurvey)

        assertEquals(14, validationResults.size)
        // Check sample validation results
        val sampleResult = validationResults.last()
        assertEquals(Status.VALID, sampleResult.status)
    }

    @Test
    fun validateDutchTranslation() {
        setLanguage(Language.nl)
        val electricityValidator = ElectricityValidator()
        val mockElectric = mockSurvey.getSingleGridConnection().electricity
        var result = electricityValidator.validateContractedCapacity(mockElectric)
        assertEquals(result.status, Status.VALID)
        assertContains(result.message, "geldig")
    }

    @Test
    fun validateContractedCapacity() {
        // Test for contracted delivery capacity
        val electricityValidator = ElectricityValidator()
        val mockElectric = mockSurvey.getSingleGridConnection().electricity
        var result = electricityValidator.validateContractedCapacity(mockElectric)
        assertEquals(result.status, Status.VALID)
        assertContains(result.message, "valid")

        val wipeGrootverbruik = CompanyGrootverbruik(
            contractedConnectionDeliveryCapacity_kW = null,
            physicalCapacityKw = null,
        )
        var mockSurveySample = updateCapacity(wipeGrootverbruik)
        var mockElectricitySample = mockSurveySample.getSingleGridConnection().electricity
        result = electricityValidator.validateContractedCapacity(mockElectricitySample)
        assertEquals(result.status, Status.MISSING_DATA)
        assertContains(result.message, "is not provided")

        val invalidGrootverbruik = CompanyGrootverbruik(
            contractedConnectionDeliveryCapacity_kW = 300,
            physicalCapacityKw = 100,
        )
        mockSurveySample = updateCapacity(invalidGrootverbruik)
        mockElectricitySample = mockSurveySample.getSingleGridConnection().electricity
        result = electricityValidator.validateContractedCapacity(mockElectricitySample)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "exceeds")

        mockSurveySample = updateElectricity()
        mockElectricitySample = mockSurveySample.getSingleGridConnection().electricity
        result = electricityValidator.validateContractedCapacity(mockElectricitySample)
        assertEquals(result.status, Status.VALID)
        assertContains(result.message, KleinverbruikElectricityConnectionCapacity._3x63A.toKw().toString())
    }

    @Test
    fun validValidations() {
        val mockSurvey = createMockSurvey()
        val gridConnectionValidator = GridConnectionValidator()
        val electricityValidator = ElectricityValidator()
        val transportValidator = TransportValidator()

        val mockElectric = mockSurvey.getSingleGridConnection().electricity
        val mockTransport = mockSurvey.getSingleGridConnection().transport

        // Test for contracted delivery capacity
        var result = electricityValidator.validateContractedCapacity(mockElectric)
        assertEquals(result.status, Status.VALID)

        // Test for contracted feed-in capacity
        result = electricityValidator.validateContractedFeedInCapacity(mockElectric)
        assertEquals(result.status, Status.VALID)

        // Test for PV production
        result = electricityValidator.validateAnnualProductionFeedIn(mockElectric)
        assertEquals(result.status, Status.VALID)

        // Test for grootverbruik physical capacity
        result = electricityValidator.validateGrootverbruikPhysicalCapacity(mockElectric)
        assertEquals(result.status, Status.VALID)

        // Test for kleinverbruik physical capacity
        result = electricityValidator.validateKleinverbruik(mockElectric)
        assertEquals(result.status, Status.NOT_APPLICABLE)

        // Test for power per charge point
        result = transportValidator.validatePowerPerChargeCars(mockTransport)
        assertEquals(result.status, Status.VALID)

        // Test for vehicle travel distance
        result = transportValidator.validateTravelDistanceCar(mockTransport)
        assertEquals(result.status, Status.VALID)

        // Test for number of electric vehicles
        result = transportValidator.validateTotalElectricCars(mockTransport)
        assertEquals(result.status, Status.VALID)

        // Test for total power of charge points
        val results = gridConnectionValidator.validateTotalPowerChargePoints(mockSurvey.getSingleGridConnection())
        assertEquals(results.last().status, Status.VALID)
    }

    @Test
    fun testInvalidValidations() {
        val gridConnectionValidator = GridConnectionValidator()
        val electricityValidator = ElectricityValidator()
        val transportValidator = TransportValidator()

        val invalidSurvey = updateMockSurveyWithInvalidData()
        val mockElectric = invalidSurvey.getSingleGridConnection().electricity
        val mockTransport = invalidSurvey.getSingleGridConnection().transport

        // Test for contracted delivery capacity (should fail)
        var result = electricityValidator.validateContractedCapacity(mockElectric)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "exceeds physical capacity")

        // Test for contracted feed-in capacity (should fail)
        result = electricityValidator.validateContractedFeedInCapacity(mockElectric)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "exceeds physical capacity")

        // Test for PV production (should fail)
        result = electricityValidator.validateAnnualProductionFeedIn(mockElectric)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "is less than feed-in")

        // Test for grootverbruik physical capacity (should fail)
        result = electricityValidator.validateGrootverbruikPhysicalCapacity(mockElectric)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "below (3x80A)")

        // Test for kleinverbruik physical capacity (should fail)
        result = electricityValidator.validateKleinverbruik(mockElectric)
        assertEquals(result.status, Status.NOT_APPLICABLE)

        // Test for power per charge point (should fail)
        result = transportValidator.validatePowerPerChargeCars(mockTransport)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "outside the valid range")

        // Test for power per charge point (should fail)
        result = transportValidator.validatePowerPerChargeTrucks(mockTransport)
        assertEquals(result.status, Status.NOT_APPLICABLE)
        assertContains(result.message, "is not provided")

        // Test for vehicle travel distance (should fail)
        result = transportValidator.validateTravelDistanceCar(mockTransport)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "outside the valid range")

        // Test for number of electric vehicles
        result = transportValidator.validateTotalElectricVans(mockTransport)
        assertEquals(result.status, Status.INVALID)
        assertContains(result.message, "exceeds the total")

        // Test for total power of charge points (should fail, contracted capacity + battery is too low)
        val results = gridConnectionValidator.validateTotalPowerChargePoints(invalidSurvey.getSingleGridConnection())
        assertEquals(results.last().status, Status.INVALID)
        assertContains(results.last().message, "exceeds allowed capacity")

    }


}

