package com.ourexists.omes.stream.equip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 唯一入口：在 JVM 内启动嵌入式 Flink MiniCluster，无需独立 Flink 安装。详见 {@code README.md}。 */
@SpringBootApplication
public class EquipFlinkBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquipFlinkBootApplication.class, args);
    }
}
