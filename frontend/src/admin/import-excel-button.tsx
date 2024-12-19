import {FunctionComponent} from "react"
import {useNavigate} from "react-router-dom"
import {Button} from "primereact/button"
import {PiMicrosoftExcelLogoFill} from "react-icons/pi"

export const ImportExcelButton: FunctionComponent = () => {
    const navigate = useNavigate()

    return (
        <Button onClick={() => navigate("/admin/import-excel")} aria-label="Importeer Excel" style={{padding: "0 1em"}}>
            <PiMicrosoftExcelLogoFill size="1.6rem" />
            &nbsp;
            Importeer Excel
        </Button>
    )
}
