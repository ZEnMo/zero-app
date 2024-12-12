package com.zenmo.excelreadnamed.v5

import com.zenmo.zummon.companysurvey.TimeSeriesType
import com.zenmo.zummon.companysurvey.TimeSeriesUnit
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.minutes

class ReadExcelTest {
    @Test
    fun testReadExcel() {
        val companyDocumented = CompanyDataDocument.fromResource("Dealnr.bedrijfsnaam.data_aanpassingenZenmo27aug_filled_out.xlsx")
        val survey = companyDocumented.getSurveyObject()

        assertEquals(false, survey.includeInSimulation)

        val deliveryTimeSeries = survey.getSingleGridConnection().electricity.quarterHourlyDelivery_kWh
        assertNotNull(deliveryTimeSeries)

        assertEquals(15.minutes, deliveryTimeSeries.timeStep)
        assertEquals(Instant.parse("2023-01-01T00:00:00+01"), deliveryTimeSeries.start)
        assertEquals(365 * 24 * 4, deliveryTimeSeries.values.size)
        assertEquals(listOf(1.2f, 2.2f, 3.2f), deliveryTimeSeries.values.sliceArray(0..2).toList())
        assertEquals(1.7, deliveryTimeSeries.values.average(), 0.0001)
        assertEquals(TimeSeriesType.ELECTRICITY_DELIVERY, deliveryTimeSeries.type)
        assertEquals(TimeSeriesUnit.KWH, deliveryTimeSeries.unit)

        assertEquals(42, survey.project?.energiekeRegioId)
    }
}
