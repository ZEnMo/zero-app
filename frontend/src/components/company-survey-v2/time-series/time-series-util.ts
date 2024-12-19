import {convert, Instant} from "@js-joda/core"
import "@js-joda/timezone/dist/js-joda-timezone-2017-2027.esm.js"
import {instantToEpochSeconds, toJsJodaInstant} from "zero-zummon"

export const targetYear = 2023
export const displayTimeZone = "Europe/Amsterdam"

// This seems dubious because it mixes different versions of js-joda.
// If this leads to issues we can do it with a conversion through epoch seconds
// export const kotlinInstantToJsJodaInstant = (kotlinInstant: any): Instant => toJsJodaInstant(kotlinInstant)
export const kotlinInstantToJsJodaInstant = (kotlinInstant: any): Instant => Instant.ofEpochSecond(instantToEpochSeconds(kotlinInstant))

// We use native date formatting because js-joda does not support Dutch locale
const dateFormatter = new Intl.DateTimeFormat("nl-NL", {
    dateStyle: "full",
    timeStyle: "long",
    timeZone: displayTimeZone,
})

export function prettyPrint(instant: Instant): string {
    const jsDate = convert(instant).toDate()
    return dateFormatter.format(jsDate)
}
