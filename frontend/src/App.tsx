import React, {FunctionComponent} from 'react'
import {ZeroHeader} from "./components/zero-header";
import {Outlet} from "react-router-dom";

export const App: FunctionComponent = () => {
    return (
        <>
            <ZeroHeader />
            <Outlet />
        </>
    );
};
