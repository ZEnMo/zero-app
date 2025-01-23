import {LocalDateTime, ZonedDateTime, ZoneId} from "@js-joda/core"
import "@js-joda/timezone/dist/js-joda-timezone-2017-2027.esm.js"
import {FunctionComponent, useState} from "react"
import {TimeSeries} from "zero-zummon"
import {InputTextarea} from "primereact/inputtextarea"
import {LabelRow} from "../generic/label-row"
import {displayTimeZone, kotlinInstantToJsJodaInstant, prettyPrint} from "./time-series-util"
import {Dropdown} from "primereact/dropdown"
import {InputText} from "primereact/inputtext"
import {IntervalDropdown} from "./interval-dropdown"

const placeholder = `
bijvoorbeeld:
0.23
0,23
.23
23
2.3
`.trim()

const kwhNumberFormatter = new Intl.NumberFormat("nl-NL", {maximumFractionDigits: 1})

export const TimeSeriesTextarea: FunctionComponent<{timeSeries: TimeSeries, setTimeSeries: (t: TimeSeries) => void}> = ({timeSeries, setTimeSeries}) => {
    const [internalTimeSeries, setInternalTimeSeries] = useState(timeSeries)

    const setTimeSeriesImpl = (timeSeries: TimeSeries) => {
        setInternalTimeSeries(timeSeries)
        if (timeSeries.isValid()) {
            setTimeSeries(timeSeries)
        }
    }

    const localStart = kotlinInstantToJsJodaInstant(internalTimeSeries.start)
        .atZone(ZoneId.of(displayTimeZone))
        .toLocalDateTime()

    const end = kotlinInstantToJsJodaInstant(internalTimeSeries.calculateEnd())

    return (
        <>
            <LabelRow label="Datum en tijd begin kwartierwaarden">
                <InputText type="datetime-local" id="start" name="start" defaultValue={localStart.toString()}
                    onChange={e => {
                        const local = LocalDateTime.parse(e.target.value)
                        const zoned = ZonedDateTime.of(local, ZoneId.of(displayTimeZone))
                        setTimeSeriesImpl(
                            internalTimeSeries.withStartEpochSeconds(zoned.toEpochSecond()),
                        )
                    }} />
            </LabelRow>
            <LabelRow label="Tijdzone">
                <Dropdown options={[{ label: 'Nederlandse tijd', value: displayTimeZone }]} value={displayTimeZone}/>
            </LabelRow>
            <LabelRow label="Meetinterval">
                <IntervalDropdown timeStep={internalTimeSeries.timeStep} setTimeStep={timeStep => setTimeSeriesImpl(internalTimeSeries.withTimeStep(timeStep))} />
            </LabelRow>
            <LabelRow label="Plak hier de waarden in kWh">
                <InputTextarea
                    id="values"
                    name="values"
                    defaultValue={internalTimeSeries.values.values().toArray().join("\n")}
                    onInput={e => {setTimeSeriesImpl(internalTimeSeries.withValues(parseTextArea((e.target as HTMLTextAreaElement).value)))}}
                    // onChange={e => setTimeSeriesImpl(internalTimeSeries.withValues(parseTextArea((e.target as HTMLTextAreaElement).value)))}
                    style={{display: "block", height: "10rem"}}
                    placeholder={placeholder} />
            </LabelRow>

            <p>Eind kwartierwaarden: {prettyPrint(end)}</p>
            <p>Totaal: <span id="total">{kwhNumberFormatter.format(internalTimeSeries.sum())}</span> kWh</p>
        </>
    )
}

function parseTextArea(content: String): Float32Array {
    const lines = content.split("\n")

    const numberArray =  lines.map(line => line
        .trim()
        .replace(",", ".")
    ).filter(line => line).map(parseFloat)

    return new Float32Array(numberArray)
}

