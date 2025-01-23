import React, { FormEvent, FunctionComponent, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { PrimeReactProvider } from "primereact/api";
import { InputText } from "primereact/inputtext";
import { Button } from "primereact/button";
import { User, Project, projectsFromJson } from "zero-zummon";
import { redirectToLogin } from "./use-users";
import { ProjectsDropdown } from "./projects-dropdown";
import { UserProjectsList } from "./user-projects-list";

export const UserForm: FunctionComponent = () => {
    const {userId} = useParams<{ userId: string }>();
    const [user, setUser] = useState<User | null>(null);
    const [originalData, setOriginalData] = useState<User | null>(null);
    const [selectedProjects, setSelectedProjects] = useState<Project[]>([]);
    const [userProjects, setUserProjects] = useState<Project[]>([]);

    const [loading, setLoading] = useState(false);
    const [isEditing, setIsEditing] = useState(false);

    const handleCancel = () => {
        if (originalData) {
            setUser(originalData); // Revert to original data
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

    useEffect(() => {
        if (userId) {
            const fetchUser = async () => {
                setLoading(true);
                try {
                    const response = await fetch(`${import.meta.env.VITE_ZTOR_URL}/users/${userId}`, {
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
                    } else {
                        alert(`Error fetching user: ${response.statusText}`);
                    }
                } catch (error) {
                    alert((error as Error).message);
                } finally {
                    setLoading(false);
                }
            };

            const fetchProjects = async () => {
                try {
                    const response = await fetch(`${import.meta.env.VITE_ZTOR_URL}/users/${userId}/projects`, {
                        credentials: "include",
                    });
                    if (!response.ok) return
    
                    setUserProjects(projectsFromJson(await response.text()))
                    setSelectedProjects(userProjects)
                } catch (error) {
                    console.error("Error fetching projects:", error);
                }
            };
    
            fetchProjects();
            fetchUser();
        } else {
            setIsEditing(true);
        }
    }, [userId]);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setLoading(true);
        try {
            const method = userId ? "PUT" : "POST";
            const url = `${import.meta.env.VITE_ZTOR_URL}/users`
            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify({
                    ...user,
                    projectIds: selectedProjects.map((project) => project.id), // Pass project IDs
                }),
            });

            if (response.status === 401) {
                return;
            }

            if (response.ok) {
                const userData = await response.json();
                setUser(userData);
                setOriginalData(userData);
                setUserProjects(selectedProjects);
            } else {
                alert(`Error: ${response.statusText}`);
            }
        } finally {
            setIsEditing(false);
            setLoading(false);
        }
    };

    return (
        <PrimeReactProvider>
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
