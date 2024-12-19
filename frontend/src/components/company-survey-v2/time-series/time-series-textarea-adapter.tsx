import {FunctionComponent} from "react"
import {targetYear} from "./time-series-util"
import {TimeSeries, TimeSeriesType, timeSeriesFromJson, createEmptyTimeSeriesForYear} from "zero-zummon"
import {TimeSeriesTextarea} from "./time-series-textarea"

/**
 * Has a plain JS object as input and output.
 * Converts that objects to a {@link TimeSeries} domain object for use in {@link TimeSeriesTextarea}
 */
export const TimeSeriesTextareaAdapter: FunctionComponent<{
    timeSeries?: any,
    timeSeriesType?: TimeSeriesType,
    setTimeSeries?: (obj: any) => void
}> = ({
    timeSeries,
    timeSeriesType = TimeSeriesType.ELECTRICITY_DELIVERY,
    setTimeSeries = console.log,
}) => {
    const timeSeriesDomainObject = timeSeries ? timeSeriesFromJson(JSON.stringify(timeSeries)) : createEmptyTimeSeriesForYear(timeSeriesType, targetYear)

    return (
        <TimeSeriesTextarea
            timeSeries={timeSeriesDomainObject} setTimeSeries={(timeSeries: TimeSeries) => {
            setTimeSeries(JSON.parse(timeSeries.toJson()))
        }} />
    )
}
