import {FunctionComponent} from "react";
import {useUser} from "./use-user";

export const LoginWidget: FunctionComponent = () => {
    const {
        isLoading,
        isLoggedIn,
        username,
    } = useUser()

    if (isLoading) {
        return null
    }

    if (!isLoggedIn) {
        return (
            <a href={import.meta.env.VITE_ZTOR_URL + "/login?redirectUrl=" + encodeURIComponent(location.href)}>Login</a>
        )
    }

    if (username === undefined) {
        return 'Unreachable code'
    }

    return (
        <span>
            <Circle letter={username[0]}></Circle>
            &nbsp;&nbsp;
            {username}
        </span>
    )
}

const Circle: FunctionComponent<{letter: string}> = ({letter}) => {
    return (
        <div style={{
            height: '2em',
            width: '2em',
            borderRadius: '1em',
            backgroundColor: 'blue',
            color: 'white',
            fontWeight: 'bold',
            display: 'inline-flex',
            justifyContent: 'center',
            alignItems: 'center', // if it looks like it's not vertically centered this is due to the font
            textAlign: 'center',
        }}>
            {letter.toUpperCase()}
        </div>
    )
}

