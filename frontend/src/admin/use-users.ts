import {useState} from "react";
import {useOnce} from "../hooks/use-once";
import {User, usersFromJson } from "zero-zummon"

type UseUserReturn = {
    loadingUsers: boolean,
    users: User[],
    changeUser: (newUser: User) => void,
    removeUser: (userId: string) => void,
}

export const useUsers = (): UseUserReturn => {
    const [loadingUsers, setLoading] = useState(true)
    const [users, setUsers] = useState<User[]>([])

    const changeUser = (newUser: User) => {
        setUsers(users.map(user => user.id.toString() === newUser.id.toString() ? newUser : user))
    }

    useOnce(async () => {
        try {
            const response = await fetch(import.meta.env.VITE_ZTOR_URL + '/users', {
                credentials: 'include',
            })
            if (response.status === 401) {
                redirectToLogin()
                return
            }
            if (!response.ok) {
                throw new Error(`Could not load users: ${response.status} ${response.statusText}`)
            }

            setUsers(usersFromJson(await response.text()))
        } catch (error) {
            alert((error as Error).message)
        } finally {
            setLoading(false)
        }
    })

    const removeUser = (userId: any) => {
        setUsers(users.filter(user => user.id.toString() !== userId.toString()))
    }

    return {
        loadingUsers,
        users,
        changeUser,
        removeUser,
    }
}

export const redirectToLogin = () => {
    window.location.href = import.meta.env.VITE_ZTOR_URL + '/login?redirectUrl=' + encodeURIComponent(window.location.href)
}
