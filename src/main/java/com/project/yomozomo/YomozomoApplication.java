package com.project.yomozomo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.project.yomozomo")
public class YomozomoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YomozomoApplication.class, args);
    }


}
