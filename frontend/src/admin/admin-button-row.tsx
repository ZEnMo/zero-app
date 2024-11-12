import {ImportExcelButton} from "./import-excel-button"
import {NewSurveyButton} from "./new-survey-button"

export const AdminButtonRow = () => (
    <div css={{display: "flex", gap: `${1/3}rem`}}>
        <ImportExcelButton/>
        <NewSurveyButton/>
    </div>
)
