package com.zenmo.zummon.companysurvey

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class NaturalGas (
    val hasConnection: Boolean? = null,
    val ean: String = "",
    val annualDelivery_m3: Int? = null,
    val hourlyValuesFiles: List<File> = emptyList(),
    val hourlyDelivery_m3: TimeSeries? = null,
    val percentageUsedForHeating: Int? = null,
) {
    fun getHasConnection(): Boolean {
        return hasConnection ?: false
    }

    /**
     * For JavaScript
     */
    val hourlyValuesFileArray: Array<File>
        get() = hourlyValuesFiles.toTypedArray()

    @Deprecated("Renamed to annualDelivery_m3", ReplaceWith("annualDelivery_m3"))
    val annualDemandM3
        get() = annualDelivery_m3
}
