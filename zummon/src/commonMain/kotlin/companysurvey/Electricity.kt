package com.zenmo.zummon.companysurvey
import com.zenmo.zummon.companysurvey.KleinverbruikElectricityConnectionCapacity.valueOf
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class Electricity (
    val hasConnection: Boolean? = null,

    val ean: String = "",

    val authorizationFile: File? = null,
    val quarterHourlyValuesFiles: List<File> = emptyList(),
    val quarterHourlyDelivery_kWh: TimeSeries? = null,
    val quarterHourlyFeedIn_kWh: TimeSeries? = null,
    val quarterHourlyProduction_kWh: TimeSeries? = null,

    // If no kwartierwaarden, ask for annual values.
    val annualElectricityDelivery_kWh: Int? = null,
    val annualElectricityFeedIn_kWh: Int? = null,
    val annualElectricityProduction_kWh: Int? = null,

    // Better name "customerType"?
    val kleinverbruikOrGrootverbruik: KleinverbruikOrGrootverbruik? = null,
    val kleinverbruik: CompanyKleinverbruik? = null,
    val grootverbruik: CompanyGrootverbruik? = null,
    val gridExpansion: GridExpansion = GridExpansion(),
) {
    @Deprecated("Renamed to annualElectricityDelivery_kWh", ReplaceWith("annualElectricityDelivery_kWh"))
    val annualElectricityDemandKwh
        get() = annualElectricityDelivery_kWh

    fun getHasConnection(): Boolean {
        return hasConnection ?: false
    }

    /**
     * Contracted capacity for delivery of electricity from grid to company.
     */
    fun getContractedConnectionCapacityKw(): Double? {
        return when (kleinverbruikOrGrootverbruik) {
            KleinverbruikOrGrootverbruik.GROOTVERBRUIK -> grootverbruik?.contractedConnectionDeliveryCapacity_kW?.toDouble()
            KleinverbruikOrGrootverbruik.KLEINVERBRUIK -> kleinverbruik?.connectionCapacity?.toKw()
            else -> kleinverbruik?.connectionCapacity?.toKw() ?: grootverbruik?.contractedConnectionDeliveryCapacity_kW?.toDouble()
        }
    }

    fun getPhysicalConnectionCapacityKw(): Double? {
        return when (kleinverbruikOrGrootverbruik) {
            KleinverbruikOrGrootverbruik.GROOTVERBRUIK -> grootverbruik?.physicalCapacityKw?.toDouble()
            KleinverbruikOrGrootverbruik.KLEINVERBRUIK -> kleinverbruik?.connectionCapacity?.toKw()
            else -> kleinverbruik?.connectionCapacity?.toKw() ?: grootverbruik?.physicalCapacityKw?.toDouble()
        }
    }

    /**
     * Contracted capacity for feed-in of electricity from company to grid.
     */
    fun getContractedFeedInCapacityKw(): Double? {
        when (kleinverbruikOrGrootverbruik) {
            KleinverbruikOrGrootverbruik.GROOTVERBRUIK -> return grootverbruik?.contractedConnectionFeedInCapacity_kW?.toDouble()
            KleinverbruikOrGrootverbruik.KLEINVERBRUIK -> return kleinverbruik?.connectionCapacity?.toKw()
            else -> return kleinverbruik?.connectionCapacity?.toKw() ?: grootverbruik?.contractedConnectionFeedInCapacity_kW?.toDouble()
        }
    }
}

@Serializable
data class GridExpansion (
    val hasRequestAtGridOperator: Boolean? = null,
    val requestedKW: Int? = null,
    val reason: String = "",
)

@Serializable
data class CompanyKleinverbruik (
    val connectionCapacity: KleinverbruikElectricityConnectionCapacity? = null,
    // If no kwartierwaarden
    val consumptionProfile: KleinverbruikElectricityConsumptionProfile? = null,
)

@Serializable
data class CompanyGrootverbruik (
    val contractedConnectionDeliveryCapacity_kW: Int? = null,
    val contractedConnectionFeedInCapacity_kW: Int? = null,
    val physicalCapacityKw: Int? = null,
) {
    @Deprecated("Renamed to contractedConnectionDeliveryCapacity_kW", ReplaceWith("contractedConnectionDeliveryCapacity_kW"))
    val contractedConnectionDemandCapacityKw: Int?
        get() = contractedConnectionDeliveryCapacity_kW

    @Deprecated("Renamed to contractedConnectionFeedInCapacity_kW", ReplaceWith("contractedConnectionFeedInCapacity_kW"))
    val contractedConnectionSupplyCapacityKw: Int?
        get() = contractedConnectionFeedInCapacity_kW
}

@JsExport
enum class KleinverbruikOrGrootverbruik {
    KLEINVERBRUIK,
    GROOTVERBRUIK,
}

enum class KleinverbruikElectricityConnectionCapacity {
    // The total number of kleinverbruik postal code regions is 359.299.
    // This statistic includes residential connections.

    //_1x6A, // majority of connections in 89 postalcodes
    //_1x10A, // majority of connections in 36 postalcodes
    //_1x20A, // majority of connections in 188 postalcodes
    _1x25A, // majority of connections in 52.042 postalcodes
    _1x35A, // majority of connections in 170.736 postalcodes
    _1x40A, // majority of connections in 6.491 postalcodes
    _1x50A, // majority of connections in 2.226 postalcodes
    //_1x63A, // majority of connections in 1 postalcode
    _3x25A, // majority of connections in 125.261 postalcodes
    _3x35A, // majority of connections in 1.049 postalcodes
    //_3x40A, // majority of connections in 87 postalcodes
    _3x50A, // majority of connections in 180 postalcodes
    _3x63A, // majority of connections in 261 postalcodes
    _3x80A, // majority of connections in 613 postalcodes
    ;// Unknown in 39 postalcodes

    fun toKw(): Double {
        return when (this) {
            _1x25A -> (1 * 25 * 230 * 0.001)
            _1x35A -> (1 * 35 * 230 * 0.001)
            _1x40A -> (1 * 40 * 230 * 0.001)
            _1x50A -> (1 * 50 * 230 * 0.001)
            _3x25A -> (3 * 25 * 230 * 0.001)
            _3x35A -> (3 * 35 * 230 * 0.001)
            _3x50A -> (3 * 50 * 230 * 0.001)
            _3x63A -> (3 * 63 * 230 * 0.001)
            _3x80A -> (3 * 80 * 230 * 0.001)
        }
    }

    fun toDisplayName(): String =
        this.name.substring(1)
}

fun kleinverbruikEnumFromDisplayName(displayName: String): KleinverbruikElectricityConnectionCapacity =
    valueOf("_$displayName")

// TODO: create profile names
enum class KleinverbruikElectricityConsumptionProfile {
    ONE,
    TWO,
    THREE,
    FOUR,
}

