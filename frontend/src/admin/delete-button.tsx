import {Button} from "primereact/button";
import {FunctionComponent, useState} from "react";
import {noop} from "lodash";

export const DeleteButton: FunctionComponent<{surveyId: any, onDelete?: (surveyId: any) => void}> = ({surveyId, onDelete = noop}) => {
    const [pending, setPending] = useState(false)
    const deleteSurvey = async () => {
        setPending(true)
        try {
            if (!confirm('Uitvraag verwijderen?')) {
                return
            }
            await fetch(`${import.meta.env.VITE_ZTOR_URL}/company-surveys/${surveyId}`, {
                method: 'DELETE',
                credentials: 'include',
            })
            onDelete(surveyId)
        } catch (error) {
            alert((error as Error).message)
        } finally {
            setPending(false)
        }
    }

    return (
        <Button icon="pi pi-trash" loading={pending} onClick={deleteSurvey} severity="danger" aria-label="Verwijderen" />
    )
}
