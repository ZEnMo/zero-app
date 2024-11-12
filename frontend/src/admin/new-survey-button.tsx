import React, {FunctionComponent, useRef} from "react"
import {ztorFetch} from "../services/ztor-fetch"
import {Project} from "zero-zummon"
import {Menu} from "primereact/menu"
import {Button} from "primereact/button"
import {MenuItem} from "primereact/menuitem"
import {useNavigate} from "react-router-dom"
import {usePromise} from "../hooks/use-promise"
import {assertDefined} from "../services/util"

export const NewSurveyButton: FunctionComponent = () => {
    const [projects = [], error, loading] = usePromise<Project[]>(() => ztorFetch("/projects"), [])
    const navigate = useNavigate();

    const menu = useRef<Menu>(null);
    const items: MenuItem[] = [
        {
            label: 'Project',
            items: projects.map(project => ({
                label: project.name,
                command() {
                    navigate(`/new-survey/${project.name}`)
                }
            }))
        }
    ];

    return (
        <>
            <Menu model={items} popup ref={menu} id="popup_menu"/>
            <Button
                label="Invullen"
                icon="pi pi-pencil"
                onClick={(event) => assertDefined(menu.current).toggle(event)}
                aria-controls="popup_menu"
                aria-haspopup/>
        </>
    )
}
