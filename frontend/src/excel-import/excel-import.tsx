import {FunctionComponent, useState} from "react"
import {SurveyWithErrors, PandID} from "zero-zummon"
import {ZeroLayout} from "../components/zero-layout"
import {PrimeReactProvider} from "primereact/api"
import {Steps} from "primereact/steps"
import {Message} from "primereact/message";
import {ExcelUpload} from "./excel-upload"
import {Feedback} from "./feedback"
import {PandenSelectLoader} from "./panden-select-loader"
import {Save} from "./save"

export const ExcelImport: FunctionComponent = () => {
    const [activeIndex, setActiveIndex] = useState(0);
    const [surveyWithErrors, setSurveyWithErrorsState] = useState<SurveyWithErrors | undefined>(undefined)
    const [totalPadIds, setTotalPadIds] = useState(0);

    return (
        <PrimeReactProvider>
            <ZeroLayout subtitle="Importeer Excel">
                <Steps
                    style={{
                        margin: "1rem",
                    }}
                    activeIndex={activeIndex}
                    onSelect={(e) => setActiveIndex(e.index)}
                    readOnly={false}
                    model={[{
                        label: "Uploaden",
                    }, {
                        label: "Feedback",
                        disabled: (surveyWithErrors == undefined)
                    }, {
                        label: "Panden selecteren",
                        disabled: (surveyWithErrors == undefined)
                    }, {
                        label: "Review",
                        disabled: (totalPadIds == 0)
                    }, {
                        label: "Opslaan",
                        disabled: (totalPadIds == 0)
                    }]
                }/>
                {activeIndex === 0 && <ExcelUpload setSurveyWithErrors={(swe) => {
                    setActiveIndex(1)
                    setSurveyWithErrorsState(swe)
                }}/>}
                {activeIndex === 1 && surveyWithErrors &&
                    <Feedback navigateNext={() => setActiveIndex(2)} surveyWithErrors={surveyWithErrors}/>}
                {activeIndex === 2 && surveyWithErrors?.survey &&
                    <PandenSelectLoader
                        project={surveyWithErrors?.survey.project}
                        thisCompanyPandIds={surveyWithErrors?.survey.getSingleGridConnection().pandIds.asJsReadonlySetView()}
                        addThisCompanyPandId={(pandId: PandID) => {
                            setTotalPadIds(totalPadIds + 1)
                            setSurveyWithErrorsState(
                                surveyWithErrors.withPandId(pandId)
                            )
                        }}
                        removeThisCompanyPandId={(pandId: PandID) => {
                            setTotalPadIds(totalPadIds - 1)
                            setSurveyWithErrorsState(
                                surveyWithErrors.withoutPandId(pandId)
                            )
                        }}/>}
                {activeIndex === 3 && surveyWithErrors &&
                    <Message style={{margin: "1rem"}} severity="info" text="Alle checks OK"/>}
                {activeIndex === 4 && surveyWithErrors && <Save survey={surveyWithErrors.survey} /> }
            </ZeroLayout>
        </PrimeReactProvider>
    )
}