import React from 'react'
import ReactDOM from 'react-dom/client'
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import App from './App'
import './index.css'
import {BedrijvenForm} from './components/bedrijven-form'
import reportWebVitals from './reportWebVitals'

const router = createBrowserRouter([
    {
        path: "/",
        element: <App/>,
    },
    {
        path: "/bedrijven-drechtsteden",
        element: <BedrijvenForm />,
    }
]);

const root = ReactDOM.createRoot(
    document.getElementsByTagName('body')[0],
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
