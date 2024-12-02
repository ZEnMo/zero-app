package com.zenmo.fudura

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

class FuduraClientTest {
    @Test
    fun testGetWalkTreeToLeaf() {
        val client = FuduraClient()
        val meteringPoints = client.getMeteringPoints()
        assertTrue(meteringPoints.eans.isNotEmpty())

        val meteringPointId = meteringPoints.eans.first().meteringPoints.first()

        val channels = client.getChannels(meteringPointId)
        assertTrue(channels.channels.isNotEmpty())

        val kwhQuarterHourlyChannels = channels.channels.map {
            client.getChannelMetadata(meteringPointId, it)
        }.filter {
            it.unitOfMeasurement == UnitOfMeasurement.kWh
                    && it.interval != null
                    && it.firstReadingTimestamp < Instant.parse("2024-06-01T00:00:00+01:00")
                    && it.lastReadingTimestamp > Instant.parse("2024-07-01T00:00:00+01:00")
        }

        val channelMetadata = kwhQuarterHourlyChannels.first()

        val telemetryBatch = client.getTelemetry(
            meteringPointId,
            channelMetadata.channelId,
            from = Instant.parse("2024-01-01T00:00:00+01:00"),
            to = channelMetadata.lastReadingTimestamp,
        )
        assertTrue(telemetryBatch.telemetry.size > 10)
        assertTrue(telemetryBatch.telemetry.size < 1001)

        val fullTelemetry = client.getTelemetryRecursive(
            meteringPointId,
            channelMetadata.channelId,
            from = Instant.parse("2024-08-01T00:00:00+01:00"),
            to = channelMetadata.lastReadingTimestamp,
        )

        assertTrue(fullTelemetry.size > 1001)
    }

    @Test
    fun testGetDetailedMeteringPoints() {
        val client = FuduraClient()
        val meteringPoints = client.getDetailedMeteringPoints()
        assertTrue(meteringPoints.meteringPoints.isNotEmpty())
    }
}
