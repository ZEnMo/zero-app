import React, { FormEvent, FunctionComponent, useEffect, useState, useRef } from "react";
import { useParams } from "react-router-dom";
import { PrimeReactProvider } from "primereact/api";
import { InputText } from "primereact/inputtext";
import { Button } from "primereact/button";
import { User, Project, projectsFromJson } from "zero-zummon";
import { redirectToLogin } from "./use-users";
import { ProjectsDropdown } from "./projects-dropdown";
import { UserProjectsList } from "./user-projects-list";
import { Toast } from "primereact/toast";

export const UserForm: FunctionComponent = () => {
    const {userId} = useParams<{ userId: string }>();
    const [user, setUser] = useState<User | null>(null);
    const [originalData, setOriginalData] = useState<User | null>(null);
    const [selectedProjects, setSelectedProjects] = useState<Project[]>([]);
    const [userProjects, setUserProjects] = useState<Project[]>([]);
    const msgs = useRef<Toast>(null);

    const [loading, setLoading] = useState(false);
    const [isEditing, setIsEditing] = useState(false);

    const handleCancel = () => {
        if (originalData) { // Revert to original data
            setUser(originalData);
            setSelectedProjects(userProjects)

        }
        setIsEditing(false);
    };

    const handleEditToggle = () => {
        setIsEditing(true);
    };

    const handleInputChange =(e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type, checked } = e.target;
        setUser((prev) => ({...prev,
            [name]: type === "checkbox" ? checked : value,
        } as User));
    };

    const transformProjects = (projects: any[]): Project[] => {
        const jsonString = JSON.stringify(projects);
        return projectsFromJson(jsonString);
    };

    useEffect(() => {
        if (userId) {
            const fetchUser = async () => {
                setLoading(true);
                try {
                    const response = await fetch(`${import.meta.env.VITE_ZTOR_URL}/users/${userId}/projects`, {
                        credentials: "include",
                    });
                    if (response.status === 401) {
                        redirectToLogin();
                        return;
                    }
                    if (response.ok) {
                        const userData = await response.json();
                        setUser(userData);
                        setOriginalData(userData);
                        setUserProjects(userData.projects)
                        const formattedProjects = transformProjects(userData.projects)
                        setSelectedProjects(formattedProjects)
                    } else {
                        alert(`Error fetching user: ${response.statusText}`);
                    }
                } catch (error) {
                    alert((error as Error).message);
                } finally {
                    setLoading(false);
                }
            };
            fetchUser();
        } else {
            setIsEditing(true);
        }
    }, [userId]);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setLoading(true);
        try {
            const sendUser = JSON.stringify({
                ...user,
                projects: selectedProjects.map((project) => ({
                    id: project.id.toString(),
                    name: project.name,
                })),
            })
            const method = userId ? "PUT" : "POST";
            const url = `${import.meta.env.VITE_ZTOR_URL}/users`
            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: sendUser,
            });

            if (response.status === 401) {
                return;
            }
            if (response.ok) {
                msgs.current?.show([
                    { sticky: true, severity: "success", summary: "Success", detail: "User saved successfully.", closable: true },
                ]);
                setUserProjects(selectedProjects);
            } else {
                msgs.current?.show([
                    {sticky: true, severity: 'error', summary: 'Error', detail: `Error: ${response.statusText}`, closable: false},
                ]);
            }
        } finally {
            setIsEditing(false);
            setLoading(false);
        }
    };

    return (
        <PrimeReactProvider>
            <Toast ref={msgs} />
            <div style={{ padding: "20px", maxWidth: "500px", margin: "0 auto" }}>
                <h3>{userId ? "Edit User" : "Add User"}</h3>
                <form
                    onSubmit={handleSubmit}
                    style={{ display: "flex", flexDirection: "column", gap: "10px" }}
                >
                    <label htmlFor="name">Keycloak ID:</label>
                    <InputText
                        id="id"
                        name="id"
                        value={user?.id || ""}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                    />
                    <label htmlFor="name">Note:</label>
                    <InputText
                        id="note"
                        name="note"
                        value={user?.note || ""}
                        onChange={handleInputChange}
                        disabled={!isEditing}
                    />
                    <div>
                        <label htmlFor="isAdmin" style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                            <input
                                type="checkbox"
                                id="isAdmin"
                                name="isAdmin"
                                checked={user?.isAdmin || false}
                                onChange={handleInputChange}
                                disabled={!isEditing}
                            />
                            Admin
                        </label>
                    </div>

                    <ProjectsDropdown
                        selectedProjects={selectedProjects}
                        onChange={setSelectedProjects}
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
                <UserProjectsList projects={userProjects}/>
            </div>
        </PrimeReactProvider>
    );
};
