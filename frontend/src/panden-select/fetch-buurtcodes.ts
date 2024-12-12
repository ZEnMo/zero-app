import {ztorFetch} from "../services/ztor-fetch"

export async function fetchBuurtcodesByProject(projectName: string): Promise<string[]> {
    return ztorFetch<string[]>(`/projects/by-name/${projectName}/buurtcodes`)
}
