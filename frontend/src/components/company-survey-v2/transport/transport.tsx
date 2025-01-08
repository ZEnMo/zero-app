import {UseFormReturn} from 'react-hook-form'
import {BooleanInput} from '../generic/boolean-input'
import {FormRow} from '../generic/form-row'
import {ProjectName} from '../project'
import {Cars} from './cars'
import {CommutersVisitors} from './commuters-visitors'
import {Trucks} from './trucks'
import {Vans} from './vans'
import {OtherVehicles} from "./other-vehicles";
import {Agriculture} from "./agriculture"

export const Transport = ({form, prefix, project}: {
    form: UseFormReturn,
    prefix: string,
    project: ProjectName
}) => {
    const {register, watch} = form

    const hasVehicles = watch(`${prefix}.hasVehicles`)

    return (
        <>
            <h2>6. Mobiliteit</h2>

            <FormRow
                label="Heeft u op dit adres bedrijfsauto's, busjes, vrachtwagens, of andere voertuigen?"
                WrappedInput={BooleanInput}
                name={`${prefix}.hasVehicles`}
                form={form}/>
            {hasVehicles && (
                <>
                    <Trucks form={form} prefix={`${prefix}.trucks`} project={project}/>
                    <Vans form={form} prefix={`${prefix}.vans`} project={project}/>
                    <Cars form={form} prefix={`${prefix}.cars`} project={project}/>
                    <Agriculture form={form} prefix={`${prefix}.agriculture`} />
                    <OtherVehicles form={form} prefix={`${prefix}.otherVehicles`} />
                </>
            )}
            <CommutersVisitors  form={form} prefix={prefix} />
        </>
    )
}
