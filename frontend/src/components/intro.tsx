import {FunctionComponent} from "react"
import {ZeroLayout} from "./zero-layout"
import {Link} from "react-router-dom"

export const Intro: FunctionComponent = () => (
    <ZeroLayout>
        <div style={{margin: "0 auto", maxWidth: "40rem"}}>
            <p>Welkom bij Zenmo Zero</p>
            <p>Deze pagina is in aanbouw. Wat u kunt vinden op deze website:</p>
            <ul>
                <li><Link to="/bedrijven-hessenpoort">Uitvraag Hessenpoort invullen</Link></li>
                <li><Link to="/bedrijven-de-wieken">Uitvraag De Wieken invullen</Link></li>
                <li><Link to="/admin">Uitvragen beheren</Link></li>
                <li><Link to="/proof-of-concept">Oud proof of concept "simuleer je buurt"</Link></li>
            </ul>
            <p>Links naar andere plekken:</p>
            <ul>
                <li><a href="https://zenmo.com">Zenmo hoofdpagina</a></li>
                <li><a href="https://keycloak.zenmo.com/realms/zenmo/account/">Eigen gebruikersaccount beheren</a></li>
                <li><a href="https://keycloak.zenmo.com/admin/zenmo/console/">Alle gebruikersaccounts beheren</a></li>
                <li><a href="https://github.com/zenmo/zero">Broncode van deze website</a></li>
            </ul>
        </div>
    </ZeroLayout>
)