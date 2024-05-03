import {FunctionComponent} from "react";
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import {useSurveys} from "./use-surveys";
import {PrimeReactProvider} from "primereact/api";
import { Button } from 'primereact/button';
import {com} from "zero-zummon"

import "primereact/resources/themes/lara-light-cyan/theme.css"
import 'primeicons/primeicons.css'
import {ButtonLink} from "./button-link";
import {DeleteButton} from "./delete-button";

type Survey = com.zenmo.zummon.companysurvey.Survey
type Address = com.zenmo.zummon.companysurvey.Address

const formatByteSize = com.zenmo.zummon.companysurvey.formatByteSize

export const Admin: FunctionComponent = () => {
    const {loading, surveys, removeSurvey} = useSurveys()

    const multipleProjects = surveys.map(survey => survey.zenmoProject)
        .filter((value, index, self) => self.indexOf(value) === index).length > 1

    return (
        <PrimeReactProvider>
            <div style={{margin: '1rem'}}>
                <h1>Uitvraag bedrijven</h1>
                <DataTable
                    value={surveys}
                    loading={loading}
                    sortField="created"
                    sortOrder={-1}
                    filterDisplay="row"
                >
                    {multipleProjects && <Column field="zenmoProject" header="Project" sortable filter />}
                    <Column field="companyName" header="Bedrijf" sortable filter />
                    <Column field="personName" header="Contactpersoon" sortable filter />
                    <Column field="email" header="E-mail" sortable filter />
                    <Column header="Aansluitingen" sortable field="numGridConnections" />
                    {/* TODO: bestanden */}
                    <Column header="Bestanden" body={(survey: Survey ) => (
                        <>
                            {survey.filesArray.map(file => (
                                <div key={file.blobName}>
                                    <a href={downloadUrl(file.blobName)}>{file.originalName}</a>
                                    &nbsp;
                                    ({formatByteSize(file.size)})
                                </div>
                            ))}
                        </>
                    )}/>
                    <Column field="createdToString" body={survey => formatDatetime(survey.created.toString())} header="Opgestuurd op" sortable/>
                    <Column body={(survey: Survey) => (
                        <div css={{
                            display: 'flex',
                            '> *': {
                                margin: `${1/6}rem`
                            },
                        }}>
                            <ButtonLink href={`${process.env.ZTOR_URL}/company-survey/${survey.id}`}>
                                {"{}"}
                            </ButtonLink>
                            <DeleteButton surveyId={survey.id} onDelete={removeSurvey}/>
                        </div>
                    )}/>
                </DataTable>
            </div>
        </PrimeReactProvider>
    )
}

const downloadUrl = (blobName: string) =>
    process.env.ZTOR_URL + "/download?blobName=" + encodeURIComponent(blobName)

// Doing it in JavaScript because no timezone available in Kotlin.
const formatDatetime = (date: string) => {
    const dateTime = new Date(date)

    return dateTime.getFullYear() + "-" +
        (dateTime.getMonth() + 1).toString().padStart(2, "0") + "-" +
        dateTime.getDate().toString().padStart(2, "0") +
        " " + dateTime.getHours().toString().padStart(2, "0") +
        ":" + dateTime.getMinutes().toString().padStart(2, "0")
}
