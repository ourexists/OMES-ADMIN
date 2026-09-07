export const proxy = {
    // 开发环境：统一走 OMES-SAS 网关（认证 + Admin 业务 API）
    dev: {
        target: "http://127.0.0.1:9400",
        authTarget: "http://127.0.0.1:9400",
        changeOrigin: true,
        rewrite: (path: string) => path.replace("/dev", "")
    },

    // 生产环境：单一网关入口
    prod: {
        target: "https://www.tcwt.net:9400",
        authTarget: "https://www.tcwt.net:9400",
        changeOrigin: true,
        rewrite: (path: string) => path.replace("/prod", "/api")
    }
};

export const value = "dev";
