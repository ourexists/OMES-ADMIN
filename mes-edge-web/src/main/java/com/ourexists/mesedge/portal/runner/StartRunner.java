/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Slf4j
@Component
public class StartRunner implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (StringUtils.isEmpty(path)) {
            path = "";
        }
        String localUrl = "http://127.0.0.1:" + port + path;
        String externalUrl = "http://" + ip + ":" + port + path;
        log.info("\n----------------------------------------------------------\n\t" +
                "Application  is running! Access URLs:\n\t" +
                "Local访问网址: \t\t" + localUrl + "\n\t" +
                "External访问网址: \t" + externalUrl + "\n\t" +
                "----------------------------------------------------------");
        try {
            Runtime.getRuntime().exec("cmd /c start " + externalUrl);  // 可以指定自己的路径
        } catch (Exception ex) {
        }
    }
}
