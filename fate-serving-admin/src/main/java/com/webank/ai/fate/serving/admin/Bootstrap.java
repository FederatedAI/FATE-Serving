package com.webank.ai.fate.serving.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
public class Bootstrap {

    public static void main(String[] args) {
        System.err.println("=========");
        SpringApplication.run(Bootstrap.class, args);

    }

}
