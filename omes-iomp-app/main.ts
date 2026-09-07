// #ifdef MP-WEIXIN
import "@/polyfills/amd-define";
// #endif

import App from "@/App.uvue";

// [cool-unix]需添加
import {cool} from "@/uni_modules/cool-unix";

import {createSSRApp} from "vue";
import "@/router";

export function createApp() {
    const app = createSSRApp(App);

    // [cool-unix]需添加
    app.use(cool);

    return {
        app
    };
}
