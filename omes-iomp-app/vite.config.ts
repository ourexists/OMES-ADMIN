import {defineConfig} from "vite";
import {cool} from "@cool-vue/vite-plugin";
import tailwindcss from "tailwindcss";
import {join} from "node:path";
import uni from "@dcloudio/vite-plugin-uni";

const resolve = (dir: string) => join(__dirname, dir);

export default defineConfig({
    plugins: [
        uni(),
        cool({
            type: "uniapp-x",
            uniapp: {
                isPlugin: true
            },
            tailwind: {
                enable: true
            }
        })
    ],

    server: {
        port: 9990,
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:9400',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
                secure: false,
                ws: false,
            },
        },
    },

    css: {
        postcss: {
            plugins: [tailwindcss({config: resolve("./tailwind.config.ts")})]
        }
    }
});
