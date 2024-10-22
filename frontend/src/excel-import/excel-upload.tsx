import {FileUpload, FileUploadErrorEvent} from "primereact/fileupload"
import {redirectToLogin} from "../admin/use-surveys"
import {FunctionComponent, useState} from "react"
import {Message} from "primereact/message"
import {SurveyWithErrors} from "zero-zummon"

export const ExcelUpload: FunctionComponent<{
    setSurveyWithErrors: (swe: SurveyWithErrors) => void
}> = ({setSurveyWithErrors}) => {
    // add error message
    const [errorMessage, setErrorMessage] = useState("")
    const resetErrorMessage = () => setErrorMessage("")

    return (
        <div>
            <FileUpload
                mode="basic"
                name="file"
                url={`${import.meta.env.VITE_ZTOR_URL}/excel-upload`}
                onSelect={resetErrorMessage}
                onUpload={event => {
                    setSurveyWithErrors(SurveyWithErrors.Companion.fromJson(event.xhr.responseText))
                }}
                withCredentials={true}
                onError={(event: FileUploadErrorEvent) => {
                    if (event.xhr.status === 401) {
                        redirectToLogin()
                        return
                    }

                    if (event.xhr.status === 0) {
                        setErrorMessage("Netwerkfout")
                        return
                    }

                    if (event.xhr.responseText) {
                        setErrorMessage(event.xhr.responseText)
                        return
                    }

                    if (event.xhr.statusText) {
                        setErrorMessage(event.xhr.statusText)
                        return
                    }
                }}
            />
            {errorMessage && <Message style={{marginTop: "1rem", whiteSpace: "pre"}} severity="error" text={errorMessage} />}
        </div>
    )
}
