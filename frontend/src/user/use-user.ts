import {useOnce} from "../hooks/use-once";
import {useState} from "react";

type UseUserReturn = {
    isLoading: boolean,
    isLoggedIn?: boolean,
    username?: string,
}

export const useUser = (): UseUserReturn => {
    const [state, setState] = useState<UseUserReturn>({
        isLoading: true,
        isLoggedIn: undefined,
        username: undefined,
    })

    useOnce(async () => {
        try {
            const response = await fetch(import.meta.env.VITE_ZTOR_URL + "/user-info", {
                credentials: "include",
            })
            if (response.status == 401) {
                setState({
                    isLoading: false,
                    isLoggedIn: false,
                })
            } else {
                const userInfo: any = await response.json()
                setState({
                    isLoading: false,
                    isLoggedIn: true,
                    username: userInfo.preferred_username,
                })
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