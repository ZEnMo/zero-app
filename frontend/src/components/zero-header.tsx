import React, {FunctionComponent, PropsWithChildren, useState} from "react"
import {Button} from "primereact/button";
import {Sidebar} from "primereact/sidebar";
import {css} from "@emotion/react";
import {To, useNavigate} from "react-router-dom";
import {useUser} from "../user/use-user";
import { redirectToLogin } from "../admin/use-users";

const sidebarStyle = css({
    width: '16rem',
    backgroundColor: '#f5f5f5',
    borderRight: '1px solid #ddd',
});

const buttonStyle = css({
    display: 'block',
    marginBottom: '3rem',
    width: '100%',

    textAlign: 'left',
    padding: '0.5em 1em',
    border: 'none',
    borderBottom: '1px solid #ddd',
    color: '#333',
    background: '#f5f5f5',
    transition: 'background-color 0.2s ease-in-out',
    fontWeight: 'normal',
    cursor: 'pointer',
    '&:hover': {
        backgroundColor: '#ebebeb',
        color: '#007ad9',
    },
});

export const ZeroHeader: FunctionComponent<PropsWithChildren & {}> = () => {
    const { isLoading, isLoggedIn, username, isAdmin } = useUser()

    const [visible, setVisible] = useState(false);
    const navigate = useNavigate();

    const loadContent = (navidateTo: To) => {
        setVisible(false);
        navigate(navidateTo)
    }
    return (
        <div className="app-header">
            
            <div className="header" css={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '1em 1em',
                backgroundColor: '#f5f5f5',
                boxShadow: '1px solid #ddd'
            }}>
                <Button icon="pi pi-bars" onClick={() => setVisible(true)}/>
                <div style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                }}>
                    {!isLoggedIn && (
                        <Button
                            label="Log In"
                            className="p-button-text"
                            onClick={redirectToLogin}
                            css={{ marginLeft: "auto", fontSize: "0.9em", cursor: "pointer" }}
                        />
                    )}
                    <a href="https://zenmo.com">
                        <img
                            alt="Zenmo logo"
                            src="https://zenmo.com/wp-content/uploads/elementor/thumbs/zenmo-logo-website-light-grey-square-o1piz2j6llwl7n0xd84ywkivuyf22xei68ewzwrvmc.png"
                            style={{height: "1.5em", verticalAlign: "sub"}}/>
                        &nbsp;
                        <b>Zenmo Zero</b>
                    </a>
                </div>
            </div>

            <Sidebar visible={visible} position="left" onHide={() => setVisible(false)} css={sidebarStyle}>
                <a onClick={() => loadContent(`/`)} css={buttonStyle}>
                    <i className="pi pi-fw pi-user" style={{marginRight: '0.5em'}}></i>
                    Home
                </a>
                <a onClick={() => loadContent('/surveys')} css={buttonStyle}>
                    <i className="pi pi-fw pi-file" style={{marginRight: '0.5em'}}></i>
                    Surveys
                </a>
                <a onClick={() => loadContent('/projects')} css={buttonStyle}>
                    <i className="pi pi-fw pi-file" style={{marginRight: '0.5em'}}></i>
                    Projects
                </a>
                {isAdmin && (
                    <a onClick={() => loadContent('/users')} css={buttonStyle}>
                        <i className="pi pi-fw pi-file" style={{ marginRight: '0.5em' }}></i>
                        Users
                    </a>
                )} 
                <a onClick={() => loadContent('/simulation')} css={buttonStyle}>
                    <i className="pi pi-fw pi-file" style={{marginRight: '0.5em'}}></i>
                    Simulation
                </a>
            </Sidebar>
        </div>
    );
};