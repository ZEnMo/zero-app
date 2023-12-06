import {UseFormReturn} from 'react-hook-form'
import {Address} from './address'
import {Electricity} from './electricity'
import {Heat} from './heat'
import {LabelRow} from './generic/label-row'
import {NaturalGas} from './natural-gas'
import {OpenQuestions} from './open-questions'
import {Storage} from './storage'
import {Supply} from './supply'
import {ElectricityData} from "./electricity-data";
import {Transport} from './transport'

export const GridConnection = ({form, prefix}: { form: UseFormReturn, prefix: string }) => {
    const supplyPrefix = `${prefix}.supply`
    const hasSupplyName = `${supplyPrefix}.hasSupply`

    return (
        <>
            <Electricity form={form} prefix={`${prefix}.electricity`}  hasSupplyName={hasSupplyName} />
            <Supply form={form} prefix={supplyPrefix} hasSupplyName={hasSupplyName} />
            <ElectricityData form={form} prefix={`${prefix}.electricity`}  hasSupplyName={hasSupplyName} />
            <Heat form={form} prefix={`${prefix}.heat`} />
            <NaturalGas form={form} prefix={`${prefix}.naturalGas`} />
            <Storage form={form} prefix={`${prefix}.storage`} />
            <Transport form={form} prefix={`${prefix}.transport`} />
            <OpenQuestions form={form} prefix={prefix} />
        </>
    )
}