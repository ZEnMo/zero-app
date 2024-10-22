import {FunctionComponent, useState} from "react"
import {Button} from "primereact/button"
import {Survey} from "zero-zummon"
import {Message} from "primereact/message"
import {useNavigate} from "react-router-dom"

export const Save: FunctionComponent<{survey: Survey}> = ({survey}) => {
    const [errorMessage, setErrorMessage] = useState("")
    let navigate = useNavigate()

    const save = async () => {
        // TODO: don't generate deeplink
        const url = import.meta.env.VITE_ZTOR_URL + '/company-surveys'
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
                body: survey.toPrettyJson()
            })

            if (response.status !== 201) {
                let message = "Er is iets misgegaan."
                setErrorMessage(message)
                const body = await response.json()
                if (body?.error?.message) {
                    message += " Details: " + body.error.message
                    setErrorMessage(message)
                }
                return
            }

            navigate('/admin')
        } catch (e) {
            let message = "Er is iets misgegaan."
            // @ts-ignore
            if ('message' in e) {
                message += " Details: " + e.message
            }
            setErrorMessage(message)
            return
        }
    }

    return (
        <div style={{margin: "1rem"}}>
            {errorMessage && <Message style={{marginBottom: "1rem"}} severity="error" text={errorMessage} />}
            <Button label="Opslaan" icon="pi pi-save" onClick={save} />
        </div>
    )
}