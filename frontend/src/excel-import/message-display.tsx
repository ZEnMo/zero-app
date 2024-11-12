import {FunctionComponent} from "react"
import {ValidationResult, KtList, Status} from "zero-zummon"
import {Message} from "primereact/message"

export const MessageDisplay: FunctionComponent<{
    validationResults?: KtList<ValidationResult>
}> = ({validationResults}) => {
    const results = validationResults ? validationResults.asJsReadonlyArrayView() : []
    const validMessages = results.filter(result => result.status === Status.VALID);
    const invalidMessages = results.filter(result => result.status === Status.INVALID);
    const missingDataMessages = results.filter(result => result.status === Status.MISSING_DATA);
    const notApplicableMessages = results.filter(result => result.status === Status.NOT_APPLICABLE);

    return (
        <>
            <div style={{
                display: "flex",
                flexDirection: "column",
                gap: "1rem",
                paddingBottom: "1rem",
                margin: "1rem"
            }}>
                {invalidMessages.length > 0 && (
                    <div>
                        <h3 style={{color: "red"}}>Invalid Data</h3>
                        {invalidMessages.map((error, i) => (
                            <Message severity="error" text={error.message} key={i} style={{margin: "0.5rem"}}/>
                        ))}
                    </div>
                )}

                {missingDataMessages.length > 0 && (
                    <div>
                        <h3 style={{color: "orange"}}>Missing Data</h3>
                        {missingDataMessages.map((error, i) => (
                            <Message severity="warn" text={error.message} key={i} style={{margin: "0.5rem"}}/>
                        ))}
                    </div>
                )}

                {notApplicableMessages.length > 0 && (
                    <div>
                        <h3 style={{color: "gray"}}>Not Applicable</h3>
                        {notApplicableMessages.map((error, i) => (
                            <Message severity="info" text={error.message} key={i} style={{margin: "0.5rem"}}/>
                        ))}
                    </div>
                )}

                {validMessages.length > 0 && (
                    <div>
                        <h3 style={{color: "green"}}>Valid Data</h3>
                        {validMessages.map((valid, i) => (
                            <Message severity="success" text={valid.message} key={i} style={{margin: "0.5rem"}}/>
                        ))}
                    </div>
                )}

                {invalidMessages.length === 0 && missingDataMessages.length === 0 && notApplicableMessages.length === 0 && validMessages.length === 0 && (
                    <Message severity="info" text="Alle checks OK"/>
                )}
            </div>
        </>
    )
}
