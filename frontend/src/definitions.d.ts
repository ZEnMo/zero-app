/// <reference types="vite/client" />

// silences typescript complaining about importing images
declare module '*.png'

declare module 'memoize-immutable' {
    export default function memoize<Fn extends Function>(f: Fn): Fn
}
