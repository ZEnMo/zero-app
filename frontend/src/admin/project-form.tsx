import React, { FormEvent, FunctionComponent, useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { PrimeReactProvider } from "primereact/api";
import { InputText } from "primereact/inputtext";
import { Button } from "primereact/button";
import { Project } from "zero-zummon";
import { redirectToLogin } from "./use-projects";

export const ProjectForm: FunctionComponent = () => {
    const {projectId} = useParams<{ projectId: string }>();
    const [project, setProject] = useState<Project | null>(null);
    const [originalData, setOriginalData] = useState<Project | null>(null);

    const [loading, setLoading] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const navigate = useNavigate();

    const handleCancel = () => {
        if (originalData) {
            setProject(originalData); // Revert to original data
        }
        setIsEditing(false);
    };

    const handleEditToggle = () => {
        setIsEditing(true);
    };

    const handleInputChange =(e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setProject((prev) => ({ ...prev, [name]: value } as Project));
    };

    useEffect(() => {
        if (projectId) {
            const fetchProject = async () => {
                setLoading(true);
                try {
                    const response = await fetch(`${import.meta.env.VITE_ZTOR_URL}/projects/${projectId}`, {
                        credentials: "include",
                    });
                    if (response.status === 401) {
                        redirectToLogin();
                        return;
                    }
                    if (response.ok) {
                        const projectData = await response.json();
                        setProject(projectData);
                        setOriginalData(projectData);
                    } else {
                        alert(`Error fetching project: ${response.statusText}`);
                    }
                } catch (error) {
                    alert((error as Error).message);
                } finally {
                    setLoading(false);
                }
            };
            fetchProject();
        } else {
            setIsEditing(true);
        }
    }, [projectId]);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setLoading(true);
        try {
            const method = projectId ? "PUT" : "POST";
            const url = `${import.meta.env.VITE_ZTOR_URL}/projects`
            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify(project),
            });
            if (response.status === 401) {
                redirectToLogin();
                return;
            }

            if (response.ok) {
                navigate(`/projects`);
            } else {
                const errorData = await response.json();
                alert(`Error: ${errorData.message}`);
            }
        } finally {
            setIsEditing(false);
            setLoading(false);
        }
    };

    return (
        <PrimeReactProvider>
            <div style={{ padding: "20px", maxWidth: "500px", margin: "0 auto" }}>
                <h3>{projectId ? "Edit Project" : "Add Project"}</h3>
                <form
                    onSubmit={handleSubmit}
                    style={{ display: "flex", flexDirection: "column", gap: "10px" }}
                >
                    <label htmlFor="name">Name:</label>
                    <InputText
                        id="name"
                        name="name"
                        value={project?.name || ""}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                    />
                    <label htmlFor="energiekeRegioId">Energieke Regio ID:</label>
                    <InputText
                        id="energiekeRegioId"
                        name="energiekeRegioId"
                        value={project?.energiekeRegioId?.toString() || ""}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                    />

                    <div style={{ display: "flex", justifyContent: "space-between", marginTop: "10px" }}>
                        {isEditing ? (
                            <>
                                <Button label="Cancel" onClick={handleCancel} type="button" disabled={loading} />
                                <Button label={loading ? "Saving..." : "Save"} type="submit" disabled={loading} />
                            </>
                        ) : (
                            <Button label="Edit" onClick={handleEditToggle} type="button" disabled={loading} />
                        )}
                    </div>
                </form>
            </div>
        </PrimeReactProvider>
    );
};
