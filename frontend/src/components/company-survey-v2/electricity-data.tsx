import {useState} from "react"
import {UseFormReturn} from "react-hook-form"
import {ConsumptionSpec, ElectricityConsumptionRadios} from "./electricity-consumption-radios"
import {LabelRow} from "./generic/label-row"
import {Purpose, Upload} from "./generic/upload"
import {ProjectConfiguration} from "./project"
import {TimeSeriesElectricity} from "./time-series/time-series-electricity"

export const ElectricityData = ({form, prefix, project}: {
    form: UseFormReturn,
    prefix: string,
    project: ProjectConfiguration,
}) => {
    const [consumptionSpec, setConsumptionSpec] = useState<ConsumptionSpec | null | undefined>()

    return (
        <>
            <h3>Data</h3>
            <LabelRow label="Hoe wilt u het elektriciteitsprofiel van deze netaansluiting doorgeven?">
                <ElectricityConsumptionRadios onChange={setConsumptionSpec} consumptionSpec={consumptionSpec} project={project}/>
            </LabelRow>
            {consumptionSpec === ConsumptionSpec.UPLOAD_QUARTER_HOURLY_VALUES && (
                <LabelRow label="Kwartierwaarden (upload een heel kalenderjaar)">
                    <Upload
                        multiple={true}
                        setFormValue={files => form.setValue(`${prefix}.quarterHourlyValuesFiles`, files)}
                        company={form.watch('companyName')}
                        project={project.name}
                        purpose={Purpose.ELECTRICITY_VALUES} />
                </LabelRow>
            )}
            {consumptionSpec === ConsumptionSpec.PDF_AUTHORIZATION && (
                <LabelRow label={
                    <>
                        <div>
                            Download <a href={project.authorizationPdf} target="_blank"> dit formulier</a> en scan het
                            in.
                        </div>
                        <div>&nbsp;</div>
                        <div>
                            Een handtekening tekenen kan met <a
                            href="https://www.mozilla.org/nl/firefox/new/">Firefox</a> of <a
                            href="https://www.adobe.com/acrobat/pdf-reader.html">Adobe Reader</a>, of print uit en scan
                            in.
                        </div>
                    </>
                }>
                    <Upload
                        multiple={false}
                        setFormValue={file => form.setValue(`${prefix}.authorizationFile`, file)}
                        company={form.watch('companyName')}
                        project={project.name}
                        purpose={Purpose.ELECTRICITY_AUTHORIZATION}/>
                </LabelRow>
            )}
            {consumptionSpec === ConsumptionSpec.PLACEHOLDER_AUTHORIZATION && (
                <LabelRow label={<div>Download <a href="/placeholder-machtiging.pdf" target="_blank">dit formulier</a> en scan het in</div>}>
                    <Upload
                        multiple={false}
                        setFormValue={file => form.setValue(`${prefix}.authorizationFile`, file)}
                        company={form.watch('companyName')}
                        project={project.name}
                        purpose={Purpose.ELECTRICITY_AUTHORIZATION} />
                </LabelRow>
            )}
            {consumptionSpec === ConsumptionSpec.TEXTAREA && (
                <TimeSeriesElectricity form={form} prefix={prefix} />
            )}
            {/*{consumptionSpec === ConsumptionSpec.ANNUAL_VALUES && (*/}
            {/*    <>*/}
            {/*        <NumberRow*/}
            {/*            label="Jaarverbruik"*/}
            {/*            name={`${prefix}.annualElectricityDelivery_kWh`}*/}
            {/*            form={form}*/}
            {/*            suffix="kWh" />*/}
            {/*        {hasSupply && (*/}
            {/*            <NumberRow*/}
            {/*                label="Jaaropwek"*/}
            {/*                name={`${prefix}.annualElectricityFeedIn_kWh`}*/}
            {/*                form={form}*/}
            {/*                suffix="kWh" />*/}
            {/*        )}*/}
            {/*    </>*/}
            {/*)}*/}
        </>
    )
}
