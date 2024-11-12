import {FunctionComponent} from "react";
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import {useSurveys} from "./use-surveys";
import {PrimeReactProvider} from "primereact/api";
import {Survey, formatByteSize} from "zero-zummon"

import "primereact/resources/themes/lara-light-cyan/theme.css"
import 'primeicons/primeicons.css'
import {DeleteButton} from "./delete-button";
import {EditButton} from "./edit-button";
import {JsonButton} from "./json-button";
import {DeeplinkButton} from "./deeplink-button"
import {ZeroLayout} from "../components/zero-layout"

import {ImportExcelButton} from "./import-excel-button"
import {NewSurveyButton} from "./new-survey-button"
import {AdminButtonRow} from "./admin-button-row"

export const Admin: FunctionComponent = () => {
    const {loading, surveys, removeSurvey} = useSurveys()

    const multipleProjects = surveys.map(survey => survey.zenmoProject)
        .filter((value, index, self) => self.indexOf(value) === index).length > 1

    return (
        <PrimeReactProvider>
            <ZeroLayout subtitle="Beheer uitvraag bedrijven">
                <div css={{margin: '1rem'}}>
                    <AdminButtonRow/>
                </div>
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
                                <JsonButton surveyId={survey.id}/>
                                <DeleteButton surveyId={survey.id} onDelete={removeSurvey}/>
                                <EditButton surveyId={survey.id}/>
                                <DeeplinkButton surveyId={survey.id}/>
                            </div>
                        )}/>
                    </DataTable>
            </ZeroLayout>
        </PrimeReactProvider>
    )
}

const downloadUrl = (blobName: string) =>
    import.meta.env.VITE_ZTOR_URL + "/download?blobName=" + encodeURIComponent(blobName)

// Doing it in JavaScript because no timezone available in Kotlin.
const formatDatetime = (date: string) => {
    const dateTime = new Date(date)

    return dateTime.getFullYear() + "-" +
        (dateTime.getMonth() + 1).toString().padStart(2, "0") + "-" +
        dateTime.getDate().toString().padStart(2, "0") +
        " " + dateTime.getHours().toString().padStart(2, "0") +
        ":" + dateTime.getMinutes().toString().padStart(2, "0")
}
