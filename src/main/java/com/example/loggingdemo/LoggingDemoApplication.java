package com.example.loggingdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoggingDemoApplication {

    private static final Logger log = LoggerFactory.getLogger(LoggingDemoApplication.class);

    public static void main(String[] args) {
        log.info("Starting Logging Demo application");
        SpringApplication.run(LoggingDemoApplication.class, args);
        log.info("Logging Demo application started");
    }
}
