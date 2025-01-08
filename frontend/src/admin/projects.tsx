import React, {FunctionComponent} from "react";
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import {useProjects} from "./use-projects";
import {PrimeReactProvider} from "primereact/api";
import {Project} from "zero-zummon"

import "primereact/resources/themes/lara-light-cyan/theme.css"
import 'primeicons/primeicons.css'
import {DeleteButton} from "./delete-button";
import {EditButton} from "./edit-button";
import {Button} from "primereact/button";
import {useNavigate} from "react-router-dom"

export const Projects: FunctionComponent = () => {
    const {loadingProjects, projects, changeProject, removeProject} = useProjects()
    const navigate = useNavigate();

    return (
        <PrimeReactProvider>
            <div css={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '1em 1em',
                boxShadow: '1px solid #ddd'
            }}>
                <h3>Projects List</h3>
                <Button
                    label="Nieuw"
                    icon="pi pi-pencil"
                    onClick={(event) => navigate(`/projects/new-project`)}
                />
            </div>
            <DataTable
                value={projects}
                loading={loadingProjects}
                sortField="created"
                sortOrder={-1}
                filterDisplay="row"
            >
                <Column field="name" header="Name" sortable filter/>
                <Column field="energiekeRegioId" header="Energie Regio Id" sortable filter/>

                <Column body={(project: Project) => (
                    <div css={{
                        display: 'flex',
                        '> *': {
                            margin: `${1 / 6}rem`
                        },
                    }}>
                        <DeleteButton type="projects" id={project.id} onDelete={removeProject}/>
                        <EditButton type="projects" id={project.id}/>
                    </div>
                )}/>
            </DataTable>
        </PrimeReactProvider>
    )
}