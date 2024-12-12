import {useLoaderData} from "react-router-dom";
import {useState} from "react";
import {useOnce} from "../../hooks/use-once";
import {Survey as SurveyComponent} from "./survey";
import {getProjectConfiguration, ProjectConfiguration} from "./project"
import {redirectToLogin} from "../../admin/use-surveys"
import {usePromise} from "../../hooks/use-promise"
import {assertDefined} from "../../services/util"
import {ztorFetch} from "../../services/ztor-fetch"

export type SurveyByIdRouteData = {
    surveyId: string,
    deeplink: string | null,
    secret: string | null,
}

export type SurveyByIdLoaderData = {
    survey: any,
    project: ProjectConfiguration
}

export const SurveyById = () => {
    const {survey, project} = useLoaderData() as SurveyByIdLoaderData

    return <SurveyComponent survey={survey} project={project} />
}

export async function fetchSurveyById(routeData: SurveyByIdRouteData): Promise<any> {
    let path = '/company-surveys/' + routeData.surveyId
    if (routeData.deeplink && routeData.secret) {
        const searchParams = new URLSearchParams()
        searchParams.append('deeplink', routeData.deeplink)
        searchParams.append('secret', routeData.secret)
        path += "?" + searchParams.toString()
    }

    return ztorFetch(path)
}
