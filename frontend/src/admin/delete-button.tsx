import {Button} from "primereact/button";
import {FunctionComponent, useState} from "react";
import {noop} from "lodash";

export const DeleteButton: FunctionComponent<{type: string, id: any, onDelete?: (type: string , id: any) => void}> = ({type, id, onDelete = noop}) => {
    const [pending, setPending] = useState(false)
    const deleteSurvey = async () => {
        setPending(true)
        try {
            if (!confirm('Uitvraag verwijderen?')) {
                return
            }
            await fetch(`${import.meta.env.VITE_ZTOR_URL}/${type}/${id}`, {
                method: 'DELETE',
                credentials: 'include',
            })
            onDelete(id)
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
