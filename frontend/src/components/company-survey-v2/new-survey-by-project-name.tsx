import {FunctionComponent} from "react"
import {Survey} from "./survey"
import {useLoaderData, useParams} from "react-router-dom"
import {castProjectName, getProjectConfiguration, ProjectConfiguration} from "./project"
import {assertDefined} from "../../services/util"

export const NewSurveyByProjectName: FunctionComponent<{}> = () => {
    const project = useLoaderData() as ProjectConfiguration

    return <Survey project={project} />
}
