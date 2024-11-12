
export type ProjectConfiguration = {
    name: ProjectName,
    email: string,
    logo?: string,
    authorizationPdf?: string,
}

export type ProjectName = 'Hessenpoort' | 'De Wieken'

export const castProjectName: (projectName: string) => ProjectName = projectName => projectName as ProjectName

export const HESSENPOORT: ProjectConfiguration = {
    name: 'Hessenpoort',
    email: 'info@ondernemersvereniginghessenpoort.nl',
    logo: '/logo-hessenpoort.png',
    authorizationPdf: '/spectral-machtiging.pdf',
}

export const DE_WIEKEN: ProjectConfiguration = {
    name: 'De Wieken',
    email: 'info@zenmo.com',
    authorizationPdf: '/machtiging-datadeling-de-wieken.pdf'
}

const GENERIC: ProjectConfiguration = {
    name: castProjectName('Placeholder project'),
    email: 'info@zenmo.com',
    authorizationPdf: '/placeholer-machtiging.pdf'
}

const configs: Record<ProjectName, ProjectConfiguration> = {
    'Hessenpoort': HESSENPOORT,
    'De Wieken': DE_WIEKEN,
}

export const getProjectConfiguration = (projectName: ProjectName): ProjectConfiguration =>
    configs[projectName] || { ...GENERIC, name: projectName }
