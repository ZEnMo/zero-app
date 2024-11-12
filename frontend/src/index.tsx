import React from 'react'
import ReactDOM from 'react-dom/client'
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import './index.css'
import {DE_WIEKEN, HESSENPOORT} from './components/company-survey-v2/project'
import {Survey} from './components/company-survey-v2/survey'
import {ThankYou} from './components/thank-you'
import reportWebVitals from './reportWebVitals'
import App from "./App";
import {LoginWidget} from "./user/login";
import {BedrijvenFormV1} from "./components/bedrijven-form-v1";
import {Admin} from "./admin/admin";
import {SurveyById, SurveyByIdRouteData} from "./components/company-survey-v2/survey-by-id";
import {Home} from "./components/home"
import {ExcelImport} from "./excel-import/excel-import"
import {NewSurveyByProjectName} from "./components/company-survey-v2/new-survey-by-project-name"

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
    },
    {
        path: "/bedrijven-hessenpoort",
        element: <Survey project={HESSENPOORT} />,
    },
    {
        path: "/bedrijven-de-wieken",
        element: <Survey project={DE_WIEKEN} />,
    },
    {
        path: "/bedrijven-uitvraag/:surveyId",
        element: <SurveyById />,
        loader: ({params: {surveyId}, request}): SurveyByIdRouteData => {
            if (!surveyId) {
                throw new Error("Survey ID is required")
            }
            const url = new URL(request.url);
            const deeplink = url.searchParams.get("deeplink");
            const secret = url.searchParams.get("secret");

            return {
                surveyId,
                deeplink,
                secret,
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
