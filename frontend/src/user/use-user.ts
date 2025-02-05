import {useOnce} from "../hooks/use-once";
import {useState} from "react";

type UseUserReturn = {
    isLoading: boolean,
    isLoggedIn?: boolean,
    username?: string,
    isAdmin?: boolean,
}

export const useUser = (): UseUserReturn => {
    const [state, setState] = useState<UseUserReturn>({
        isLoading: true,
        isLoggedIn: undefined,
        username: undefined,
        isAdmin: false,
    })

    useOnce(async () => {
        try {
            const response = await fetch(import.meta.env.VITE_ZTOR_URL + "/user-info", {
                credentials: "include",
            })

            if (!response.ok) {
                throw new Error(`Failed to get user: ${response.statusText}`)
            }

            if (response.status === 500) {
                setState(prevState => ({
                    ...prevState,
                    isAdmin: false,
                }))
            }

            if (response.ok) {
                const userInfo = await response.json();

                setState((prevState) => ({
                    ...prevState,
                    isLoading: false,
                    isLoggedIn: true,
                    username: userInfo.decodedAccessToken.preferred_username,
                    isAdmin: userInfo.isAdmin
                }));
            }
        } catch (e) {
            console.error(e)
        } finally {
            setState(prevState => ({
                ...prevState,
                isLoading: false,
            }))
        }
    })

    return state
}