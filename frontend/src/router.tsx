import { createBrowserRouter } from "react-router-dom";

import {DE_WIEKEN, getProjectConfiguration, HESSENPOORT, ProjectName} from "./components/company-survey-v2/project"
import {SurveyFromProject} from "./components/company-survey-v2/survey"
import {ThankYou} from './components/thank-you'
import {LoginWidget} from "./user/login";
import {BedrijvenFormV1} from "./components/bedrijven-form-v1";
import {Surveys} from "./admin/surveys";
import {fetchSurveyById, SurveyById, SurveyByIdLoaderData} from "./components/company-survey-v2/survey-by-id"
import {Intro} from "./components/intro"
import {ExcelImport} from "./excel-import/excel-import"
import {NewSurveyByProjectName} from "./components/company-survey-v2/new-survey-by-project-name"
import {assertDefined} from "./services/util"
import {Simulation} from "./simulation";
import {App} from "./App";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <App />,
        children: [
            {path: "", element: <Intro />},
            {path: "/surveys", element: <Surveys />},
            {path: "/simulation", element: <Simulation />},
        ],
    },
    {
        path: "/bedrijven-v1",
        element: <BedrijvenFormV1 />
    },
    {
        path: "/proof-of-concept",
        element: <Simulation />,
    },
    {
        path: "/new-survey/:projectName",
        element: <NewSurveyByProjectName />,
        loader: async ({params: {projectName}, request}) => {
            return getProjectConfiguration(assertDefined(projectName) as ProjectName)
        }
    },
    {
        path: "/bedrijven-hessenpoort",
        element: <SurveyFromProject />,
        loader: async () => getProjectConfiguration(HESSENPOORT.name)
    },
    {
        path: "/bedrijven-de-wieken",
        element: <SurveyFromProject />,
        loader: async () => getProjectConfiguration(DE_WIEKEN.name)
    },
    {
        path: "/bedrijven-uitvraag/:surveyId",
        element: <SurveyById />,
        loader: async ({params: {surveyId}, request}): Promise<SurveyByIdLoaderData> => {
            if (!surveyId) {
                throw new Error("Survey ID is required")
            }
            const url = new URL(request.url);
            const deeplink = url.searchParams.get("deeplink");
            const secret = url.searchParams.get("secret");

            const survey = await fetchSurveyById({
                surveyId,
                deeplink,
                secret,
            })

            const project = await getProjectConfiguration(survey.zenmoProject)

            return {
                survey,
                project,
            }
        }
    },
    {
        path: "/bedankt",
        element: <ThankYou />,
    },
    {
        path: "/admin",
        element: <Surveys />,
    },
    {
        path: "/admin/import-excel",
        element: <ExcelImport />
    },
    {
        path: "/login",
        element: <LoginWidget />
    }
]);
