import {UseFormReturn} from 'react-hook-form'
import {Electricity} from './electricity'
import {ElectricityData} from './electricity-data'
import {Heat} from './heat'
import {NaturalGas} from './natural-gas'
import {OpenQuestions} from './open-questions'
import {ProjectConfiguration} from './project'
import {Storage} from './storage'
import {Supply} from './supply'
import {Transport} from './transport/transport'
import {GridExpansion} from "./grid-expansion";
import {PandenSelect} from "./panden-select"

export const GridConnection = ({form, prefix, project}: { form: UseFormReturn, prefix: string, project: ProjectConfiguration }) => {
    const supplyPrefix = `${prefix}.supply`
    const hasSupplyName = `${supplyPrefix}.hasSupply`
    const hasElectricityConnection = form.watch(`${prefix}.electricity.hasConnection`)

    return (
        <>
            <PandenSelect buurtcodes={project.buurtcodes} form={form} prefix={prefix} />
            <Electricity form={form} prefix={`${prefix}.electricity`} />
            <GridExpansion form={form} prefix={`${prefix}.electricity.gridExpansion`} />
            {hasElectricityConnection &&
                <>
                    <Supply form={form} prefix={supplyPrefix} hasSupplyName={hasSupplyName} />
                    <ElectricityData form={form} prefix={`${prefix}.electricity`}  project={project} />
                </>
            }
            <Heat form={form} prefix={`${prefix}.heat`} />
            <NaturalGas form={form} prefix={`${prefix}.naturalGas`} project={project.name} />
            <Storage form={form} prefix={`${prefix}.storage`} />
            <Transport form={form} prefix={`${prefix}.transport`} project={project.name}/>
            <OpenQuestions form={form} prefix={prefix} />
        </>
    )
}
