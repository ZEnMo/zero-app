import {fetchBuurtcodesByProject} from "../../panden-select/fetch-buurtcodes"

export type FrontendProjectConfiguration = {
    name: ProjectName,
    email: string,
    logo?: string,
    authorizationPdf?: string,
}

export type ProjectConfiguration = FrontendProjectConfiguration & {
    buurtcodes: readonly string[],
}

export type ProjectName = 'Hessenpoort' | 'De Wieken'

export const castProjectName: (projectName: string) => ProjectName = projectName => projectName as ProjectName

export const HESSENPOORT: FrontendProjectConfiguration = {
    name: 'Hessenpoort',
    email: 'info@ondernemersvereniginghessenpoort.nl',
    logo: '/logo-hessenpoort.png',
    authorizationPdf: '/spectral-machtiging.pdf',
}

export const DE_WIEKEN: FrontendProjectConfiguration = {
    name: 'De Wieken',
    email: 'info@zenmo.com',
    authorizationPdf: '/machtiging-datadeling-de-wieken.pdf'
}

const GENERIC: FrontendProjectConfiguration = {
    name: castProjectName('Placeholder project'),
    email: 'info@zenmo.com',
    authorizationPdf: '/placeholer-machtiging.pdf'
}

const configs: Record<ProjectName, FrontendProjectConfiguration> = {
    'Hessenpoort': HESSENPOORT,
    'De Wieken': DE_WIEKEN,
}

export async function getProjectConfiguration(projectName: ProjectName): Promise<ProjectConfiguration> {
    const buurtcodes = await fetchBuurtcodesByProject(projectName)
    return {
        ...(configs[projectName] || { ...GENERIC, name: projectName }),
        buurtcodes,
    }
}
