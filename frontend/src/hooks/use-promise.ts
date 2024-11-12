import useLibPromise from "react-use-promise"

type UsePromiseReturn<T> = [
    undefined,
    undefined,
    true, // pending
] | [
    T, // result
    undefined,
    false,
] | [
    undefined,
    Error,
    false,
]

export function usePromise<T>(promiseFn: () => Promise<T>, deps?: any[]): UsePromiseReturn<T> {
    const [result, error, state] = useLibPromise(promiseFn(), deps)

    // @ts-ignore
    return [
        result,
        error,
        state === "pending",
    ]
}
