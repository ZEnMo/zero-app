import {FunctionComponent} from "react"
import {UseFormReturn} from "react-hook-form"
import {NumberRow} from "../generic/number-row"

export const Agriculture: FunctionComponent<{
    form: UseFormReturn,
    prefix: string
}> = ({form, prefix}) => {
    const {watch} = form

    return (
        <>
            <h3>Landbouwvoertuigen</h3>
            <NumberRow
                label="Aantal trekkers?"
                name={`${prefix}.numTractors`}
                form={form} />

            {watch(`${prefix}.numTractors`) && (
                <NumberRow
                    label="Gezamelijk dieselverbruik per jaar?"
                    name={`${prefix}.annualDieselUsage_L`}
                    form={form}
                    suffix="liter" />
            )}
        </>
    )
}
