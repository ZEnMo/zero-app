import {useState} from "react";
import {useOnce} from "../hooks/use-once";
import {Project, projectsFromJson } from "zero-zummon"
import {useNavigate} from "react-router-dom";

type UseProjectReturn = {
    loadingProjects: boolean,
    projects: Project[],
    changeProject: (newProject: Project) => void,
    removeProject: (projectId: string) => void,
}

type UseProjectData = {
    loadingProject: boolean,
    project: Project,
}

export const useProjects = (): UseProjectReturn => {
    const [loadingProjects, setLoading] = useState(true)
    const [projects, setProjects] = useState<Project[]>([])

    const changeProject = (newProject: Project) => {
        setProjects(projects.map(project => project.id.toString() === newProject.id.toString() ? newProject : project))
    }

    useOnce(async () => {
        try {
            const response = await fetch(import.meta.env.VITE_ZTOR_URL + '/projects', {
                credentials: 'include',
            })
            if (response.status === 401) {
                redirectToLogin()
                return
            }
            if (response.status === 500) {
                throw new Error(`Failed: ${response.statusText}`)
            }

            setProjects(projectsFromJson(await response.text()))
        } catch (error) {
            alert((error as Error).message)
        } finally {
            setLoading(false)
        }
    })

    const removeProject = (projectId: any) => {
        setProjects(projects.filter(project => project.id.toString() !== projectId.toString()))
    }

    return {
        loadingProjects,
        projects,
        changeProject,
        removeProject,
    }
}

export const redirectToLogin = () => {
    window.location.href = import.meta.env.VITE_ZTOR_URL + '/login?redirectUrl=' + encodeURIComponent(window.location.href)
}
