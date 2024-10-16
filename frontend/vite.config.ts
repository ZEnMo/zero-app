import {defineConfig} from "vite"
import react from "@vitejs/plugin-react"

export default defineConfig({
    plugins: [react({ jsxImportSource: '@emotion/react' })],
    // for dev
    server: {
        port: 3000,
    },
    build: {
        rollupOptions: {
            output: {
                entryFileNames: "[name].js",
                assetFileNames: '[name].css',
            },
        },
    },
})
