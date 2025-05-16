package com.smartbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // For periodic recommendation generation
public class SmartBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBookApplication.class, args);
    }
}