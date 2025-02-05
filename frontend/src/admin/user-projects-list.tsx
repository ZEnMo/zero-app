import { FunctionComponent } from "react";
import { Project } from "zero-zummon";
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { EditButton } from "./edit-button";

type UserProjectsListProps = {
    projects?: Project[];
};

export const UserProjectsList: FunctionComponent<UserProjectsListProps> = ({
    projects,
}) => {
    return (
        <div>
            <h3>Current Projects</h3>
            <DataTable value={projects} responsiveLayout="scroll">
                <Column field="name" header="Project Name" />
                <Column
                    body={(project: Project) => (
                        <EditButton type="projects" id={project.id}/>
                    )}
                    header="Actions"
                />
            </DataTable>
        </div>
    );
};
