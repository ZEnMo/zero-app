import {FunctionComponent} from "react"
import {SurveyWithErrors, surveyValidator, ValidationResult, Status, KtList} from "zero-zummon"
import {Button} from "primereact/button"
import {MessageDisplay} from "./message-display"
import { Panel } from 'primereact/panel';

import {Card} from "primereact/card";
import {Tooltip} from 'primereact/tooltip';

import {useToggle} from "../hooks/use-toggle"
import {Message} from "primereact/message";

export const Feedback: FunctionComponent<{
    surveyWithErrors: SurveyWithErrors
    navigateNext: () => void
}> = ({surveyWithErrors, navigateNext}) => {
    const results = surveyValidator.get().validate(surveyWithErrors.survey)

    return (
        <>
            <MessageDisplay validationResults={results}/>
            <div style={{margin: "1rem"}}>
                <Panel header="Data Verbergen" toggleable>
                    <pre style={{
                        backgroundColor: '#1e1e1e',
                        color: '#dcdcdc',
                        padding: '1rem',
                        borderRadius: '5px',
                        fontFamily: 'monospace',
                        overflow: 'auto',
                        maxHeight: '400px'
                    }}>
                        {surveyWithErrors?.survey.toPrettyJson()}
                    </pre>
                </Panel>
            </div>

            <div style={{
                display: "flex",
                gap: "1rem",
                margin: "1rem"
            }}>
                <Button label="Panden selecteren" icon="pi pi-arrow-right" onClick={navigateNext}/>
            </div>
        </>
    )
}

