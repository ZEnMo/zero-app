import {FunctionComponent, useState} from "react"
import {ztorFetch} from "../services/ztor-fetch"
import {BooleanCheckbox} from "./boolean-checkbox"

export const SurveyIncludeInSimulationCheckbox: FunctionComponent<{
    surveyId: any,
    includeInSimulation: boolean,
    setIncludeInSimulation: (active: boolean) => void
}> = (
    {surveyId, includeInSimulation, setIncludeInSimulation}
) => {
    const [disabled, setDisabled] = useState(false);
    const onChange = async (includeInSimulation: boolean) => {
        setDisabled(true);
        try {
            await ztorFetch(`/company-surveys/${surveyId}/include-in-simulation`, {
                method: 'PUT',
                body: JSON.stringify(includeInSimulation),
                headers: {
                    'Content-Type': 'application/json',
                }
            })
            setIncludeInSimulation(includeInSimulation)
        } catch (e) {
            alert(`Error setting active state: ${e}`)
        } finally {
            setDisabled(false);
        }
    }

    return <BooleanCheckbox
        value={includeInSimulation}
        onChange={onChange}
        disabled={disabled}
    />
}
