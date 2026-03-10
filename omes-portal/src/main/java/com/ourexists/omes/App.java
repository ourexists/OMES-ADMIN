/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * OMES Portal 启动类。
 * 启动前会检测本机同端口是否已有实例在运行，若有则只打开浏览器并退出，保证同时只运行一个实例。
 */
@EnableScheduling
@SpringBootApplication
@EnableAsync
@PropertySource(value = {"file:config/config.properties"})
public class App {

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getProperty("server.port", "10010"));
        if (isAlreadyRunning(port)) {
            String url = "http://127.0.0.1:" + port + "/";
            openBrowser(url);
            System.exit(0);
        }
        SpringApplication.run(App.class, args);
    }

    /** 检测指定端口是否已有服务响应（即是否已有实例在运行） */
    private static boolean isAlreadyRunning(int port) {
        String url = "http://127.0.0.1:" + port + "/";
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("HEAD");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code >= 200 && code < 400;
        } catch (Exception e) {
            return false;
        }
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", url});
            }
        } catch (Exception ignored) {
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
