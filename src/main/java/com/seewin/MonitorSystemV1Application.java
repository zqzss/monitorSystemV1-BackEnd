package com.seewin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MonitorSystemV1Application {

    public static void main(String[] args) {
        SpringApplication.run(MonitorSystemV1Application.class, args);
    }

}
