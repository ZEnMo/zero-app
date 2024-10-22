import {Button} from "primereact/button";
import {FunctionComponent, useRef, useState} from "react"
import {noop} from "lodash";
import {OverlayPanel} from "primereact/overlaypanel"
import {buildDeeplinkUrl} from "../components/company-survey-v2/deeplink"
// @ts-ignore
import {CopyToClipboard} from 'react-copy-to-clipboard';

export const DeeplinkButton: FunctionComponent<{surveyId: any, onDelete?: (surveyId: any) => void}> = ({surveyId, onDelete = noop}) => {
    const dialogRef = useRef(null);

    const [pending, setPending] = useState(false)
    const [deeplinkUrl, setDeeplinkUrl] = useState<string>('')

    const generateDeeplink = async (event: any) => {
        try {
            const response = await fetch(`${import.meta.env.VITE_ZTOR_URL}/company-surveys/${surveyId}/deeplink`, {
                method: 'POST',
                credentials: 'include',
            })
            if (!response.ok) {
                throw new Error(`Failed to generate deeplink: ${response.statusText}`)
            }
            setDeeplinkUrl(buildDeeplinkUrl(await response.json()))
            ;(dialogRef.current as any).toggle(event)
        } catch (error) {
            alert((error as Error).message)
        } finally {
            setPending(false)
        }
    }

    return (
        <>
            <Button icon="pi pi-share-alt" loading={pending} onClick={event => generateDeeplink(event)} severity="help" aria-label="Deeplink" />
            <OverlayPanel ref={dialogRef}>
                <div style={{
                    maxWidth: "30rem",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    flexDirection: "row",
                }}>
                    <div>
                        <a style={{maxWidth: "80%", wordBreak: "break-all"}} href={deeplinkUrl}>{deeplinkUrl}</a>
                    </div>
                    <div style={{paddingLeft: "1rem"}}>
                        <CopyToClipboard text={deeplinkUrl}>
                            <Button icon="pi pi-copy" outlined/>
                        </CopyToClipboard>
                    </div>
                </div>
            </OverlayPanel>
        </>
    )
}
