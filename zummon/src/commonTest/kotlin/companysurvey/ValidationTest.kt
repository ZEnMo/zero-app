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
    fun validateContractedCapacity() {
        var results = validateContractedCapacity.validate(mockSurvey)
        assertEquals(results.status, Status.VALID)
        assertContains(results.message, "fits")

        val mockSurveySample = wipeCapacity()
        results = validateContractedCapacity.validate(mockSurveySample)
        assertEquals(results.status, Status.MISSING_DATA)
        assertContains(results.message, "No")
    }
}
