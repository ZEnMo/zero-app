import React, {FunctionComponent, PropsWithChildren} from "react"

export const ZeroLayout: FunctionComponent<PropsWithChildren & {
    subtitle?: string,
}> = ({
    children,
    subtitle,
}) => (
    <div style={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
    }}>
        <h1 style={{
            paddingLeft: "1em",
        }}>
            <img
                alt="Zenmo logo"
                src="https://zenmo.com/wp-content/uploads/elementor/thumbs/zenmo-logo-website-light-grey-square-o1piz2j6llwl7n0xd84ywkivuyf22xei68ewzwrvmc.png"
                style={{height: "1em", verticalAlign: "sub"}}/>
            &nbsp;
            Zenmo Zero
        </h1>
        {subtitle && <h3 style={{
            padding: "0 1em 1em 5rem",
            margin: 0,
        }}>
            {subtitle}
        </h3>}
        {children}
    </div>
)

