package com.zenmo.ztor

import com.zenmo.fudura.DetailedMeteringPoint
import com.zenmo.fudura.Direction
import com.zenmo.orm.companysurvey.SurveyRepository
import com.zenmo.fudura.FuduraClient
import com.zenmo.fudura.GetChannelMetadataResult
import com.zenmo.fudura.ProductType
import com.zenmo.fudura.Telemetry
import com.zenmo.fudura.UnitOfMeasurement
import com.zenmo.orm.companysurvey.TimeSeriesRepository
import com.zenmo.zummon.companysurvey.Survey
import com.zenmo.zummon.companysurvey.TimeSeries
import com.zenmo.zummon.companysurvey.TimeSeriesType
import com.zenmo.zummon.companysurvey.TimeSeriesUnit
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class FuduraImport (
    val surveyRepository: SurveyRepository,
    val timeSeriesRepository: TimeSeriesRepository,
    val fuduraClient: FuduraClient = FuduraClient(),
) {
    fun importHessenPoort() {
        val detailsMeteringPoints = fuduraClient.getDetailedMeteringPoints(perCustomer = true, customerId = "*")
        val relevantMeteringPoints = detailsMeteringPoints.meteringPoints.filter {
            isRelevantMeter(it.meteringPointId)
        }
        val now: Instant = Clock.System.now() - 1.days
        val inRangeEanMeteringPoints = relevantMeteringPoints.filter {
            val period = it.authorizations.single().periods.single()
            // We want to only retrieve meters of which there is at least one year of data.
            // Preferably a full calendar year.
            period.from < now.minus(366.days)
                    && period.to > now
        }

        val surveys = surveyRepository.getHessenpoortSurveys()

        val perCustomerData: Collection<FuduraCustomer> = inRangeEanMeteringPoints.groupingBy { it.authorizations.single().customerId }
            .aggregate { key, accumulator: FuduraCustomer?, element, first ->
                val companyName = findCompanyNameByEan(surveys, element.ean)
                if (companyName == null) {
                    println("No survey found for ean ${element.ean}, Fudura customer ${key}")
                }
                if (first) {
                    FuduraCustomer(key, companyName, listOf(element))
                } else {
                    accumulator!!.copy(
                        meteringPoints = accumulator.meteringPoints + element,
                        companyName = if (companyName == accumulator.companyName) {
                            companyName
                        } else {
                            "${accumulator.companyName}+${companyName}"
                        }
                    )
                }
            }.values

        val perKnownCustomerData = perCustomerData.filter {
            if (it.companyName == null) {
                println("No survey found for all EANs of Fudura customer ${it.id}, don't know which company this is")
            }

            it.companyName != null
        }

        for (customer in perKnownCustomerData) {
            for (meteringPoint in customer.meteringPoints) {
                val channels = fuduraClient.getChannels(meteringPoint.meteringPointId).channels
                val channelMetadataCollection = channels.map { channel ->
                    fuduraClient.getChannelMetadata(meteringPoint.meteringPointId, channel)
                }.filter { isChannelRelevant(it, meteringPoint.meteringPointId) }

                if (channelMetadataCollection.isEmpty()) {
                    println("No relevant channels for ean ${meteringPoint.ean}, company ${customer.companyName}")
                }

                val authorizationStart = meteringPoint.authorizations.single().periods.single().from
                val desiredStart = Instant.parse("2022-12-31T00:00:00+01:00")
                val desiredEnd = Instant.parse("2024-01-02T00:00:00+01:00")

                for (channel in channelMetadataCollection) {
                    val channelStart = channel.firstReadingTimestamp

                    // Try to import the whole of 2023, else import most recent values.
                    val from = listOf(desiredStart, authorizationStart, channelStart).max()
                    val to = if (from == desiredStart) desiredEnd else now

                    val telemetry = fuduraClient.getTelemetryRecursive(
                        meteringPointId = meteringPoint.meteringPointId,
                        channelId = channel.channelId,
                        from = from,
                        to = to,
                    )

//                    val numValues = 365 * 24 * 4
//                    if (telemetry.size < numValues) {
//                        throw Exception("Not enough values")
//                    }
//
//                    if (telemetry.size > numValues) {
//                        throw Exception("Too many values")
//                    }

                    val timeSeries = createTimeSeries(channel, telemetry.reversed(), meteringPoint.meteringPointId)
                    timeSeriesRepository.insertByEan(meteringPoint.ean, timeSeries)
                    println("Saved timeSeries ${channel.channelId}")
                    println("    metering point ${meteringPoint.meteringPointId}")
                    println("    direction ${channel.direction}")
                    println("    company ${customer.companyName}")
                    println("    start ${timeSeries.start}")
                    println("    end ${timeSeries.calculateEnd()}")
                }
            }
        }
    }
}

fun isRelevantMeter(meteringPointId: String) = isMainMeter(meteringPointId) || isSummedProductionMeter(meteringPointId)

fun isMainMeter(meteringPointId: String) =
    meteringPointId.matches(Regex("^\\d{18}\$"))

fun isSummedProductionMeter(meteringPointId: String) =
    // Meters with this suffix seem to have the sum of all production meters behind one EAN.
    meteringPointId.matches(Regex("^\\d{18}(GENS|GENA)$"))

data class FuduraCustomer (
    val id: String,
    val companyName: String?,
    val meteringPoints: List<DetailedMeteringPoint>,
)

fun findCompanyNameByEan(surveys: List<Survey>, ean: String): String? {
    val survey = surveys.find {  survey ->
        for (gc in survey.flattenedGridConnections()) {
            if (gc.electricity.ean == ean || gc.naturalGas.ean == ean) {
                return@find true
            }
        }

        false
    }

    return survey?.companyName
}

fun isChannelRelevant(channelMetadata: GetChannelMetadataResult, meteringPointId: String) =
    channelMetadata.unitOfMeasurement == UnitOfMeasurement.kWh
            && channelMetadata.productType == ProductType.Electricity
            && channelMetadata.interval == "00:15:00"
            && channelMetadata.lastReadingTimestamp - channelMetadata.firstReadingTimestamp > 366.days
            && getTimeSeriesType(channelMetadata, meteringPointId) != null

fun createTimeSeries(
    channelMetadata: GetChannelMetadataResult,
    telemetry: List<Telemetry>,
    meteringPointId: String,
) = TimeSeries(
    type = getTimeSeriesType(channelMetadata, meteringPointId) ?: throw Exception("Unmapped time series type"),
    // Measurement start time
    start = Instant.parse(telemetry.first().readingTimestamp),
    timeStep = DateTimeUnit.MINUTE * 15,
    unit = TimeSeriesUnit.KWH,
    values = telemetry.map {
        it.value.toFloat()
    }.toFloatArray()
)

fun getTimeSeriesType(channelMetadata: GetChannelMetadataResult, meteringPointId: String): TimeSeriesType? =
    when {
        isMainMeter(meteringPointId) && channelMetadata.direction == Direction.Production
            -> TimeSeriesType.ELECTRICITY_FEED_IN
        isMainMeter(meteringPointId) && channelMetadata.direction == Direction.Consumption
            -> TimeSeriesType.ELECTRICITY_DELIVERY
        isSummedProductionMeter(meteringPointId) && channelMetadata.direction == Direction.Production
            -> TimeSeriesType.ELECTRICITY_PRODUCTION
        // ignore net measurement
        // and ignore consumption behind production meter
        else -> null
    }
