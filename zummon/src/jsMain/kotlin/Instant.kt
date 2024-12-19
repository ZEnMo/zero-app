import kotlinx.datetime.Instant
import kotlinx.datetime.internal.JSJoda.Instant as jtInstant

@JsModule("@js-joda/timezone")
@JsNonModule
external object JsJodaTimeZoneModule

private val jsJodaTz = JsJodaTimeZoneModule

@JsExport
fun toJsJodaInstant(instant: Instant) = jtInstant.ofEpochSecond(instant.epochSeconds.toDouble(), 0)
