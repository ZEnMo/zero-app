package com.zenmo.orm.companysurvey

import com.zenmo.zummon.companysurvey.CompanyGrootverbruik
import com.zenmo.zummon.companysurvey.CompanyKleinverbruik
import com.zenmo.zummon.companysurvey.Electricity
import com.zenmo.zummon.companysurvey.KleinverbruikElectricityConnectionCapacity
import kotlin.test.Test
import kotlin.test.assertEquals

class ElectricityTest {
    @Test
    fun testKleinverbruikConnectionCapacity() {
        val electricity = Electricity(
            kleinverbruik = CompanyKleinverbruik(
                connectionCapacity = KleinverbruikElectricityConnectionCapacity._3x80A,
            )
        )

        assertEquals(electricity.getContractedConnectionCapacityKw(), 3 * 80.0 * 230.0 * 0.001)
    }

    @Test
    fun testGrootverbruikConnectionCapacity() {
        val electricity = Electricity(
            grootverbruik = CompanyGrootverbruik(
                contractedConnectionDeliveryCapacity_kW = 400
            )
        )

        assertEquals(electricity.getContractedConnectionCapacityKw(), 400.0)
    }
}
