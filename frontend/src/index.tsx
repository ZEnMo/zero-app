import React from 'react'
import ReactDOM from 'react-dom/client'
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import './index.css'
import {DE_WIEKEN, getProjectConfiguration, HESSENPOORT, ProjectName} from "./components/company-survey-v2/project"
import {Survey, SurveyFromProject} from "./components/company-survey-v2/survey"
import {ThankYou} from './components/thank-you'
import reportWebVitals from './reportWebVitals'
import App from "./App";
import {LoginWidget} from "./user/login";
import {BedrijvenFormV1} from "./components/bedrijven-form-v1";
import {Admin} from "./admin/admin";
import {
    fetchSurveyById,
    SurveyById,
    SurveyByIdLoaderData,
    SurveyByIdRouteData,
} from "./components/company-survey-v2/survey-by-id"
import {Home} from "./components/home"
import {ExcelImport} from "./excel-import/excel-import"
import {NewSurveyByProjectName} from "./components/company-survey-v2/new-survey-by-project-name"
import {fetchBuurtcodesByProject} from "./panden-select/fetch-buurtcodes"
import {assertDefined} from "./services/util"

const router = createBrowserRouter([
    {
        path: "/",
        element: <Home />,
    },
    {
        path: "/proof-of-concept",
        element: <App />,
    },
    {
        path: "/bedrijven-v1",
        element: <BedrijvenFormV1 />
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
        element: <Admin />,
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

const root = ReactDOM.createRoot(
    //@ts-ignore
    document.getElementById('react-root'),
)
root.render(
    <React.StrictMode>
        <RouterProvider router={router} />
    </React.StrictMode>,
)

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals()
