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

type Writeable<T> = { -readonly [P in keyof T]: T[P] };

export function usePromise<T>(promiseFn: () => Promise<T>, deps?: readonly any[]): UsePromiseReturn<T> {
    const [result, error, state] = useLibPromise(promiseFn, deps as Writeable<any[]>)

    // @ts-ignore
    return [
        result,
        error,
        state === "pending",
    ]
}
