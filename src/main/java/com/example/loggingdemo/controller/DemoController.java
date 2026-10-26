package com.example.loggingdemo.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints whose only purpose is to make the logging behaviour easy to see and test.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    /** Emits one message at every level so you can see which ones pass the configured threshold. */
    @GetMapping("/log-levels")
    public Map<String, String> logLevels() {
        log.trace("TRACE message - very fine-grained detail, normally off");
        log.debug("DEBUG message - diagnostic detail for developers");
        log.info("INFO message - normal application events");
        log.warn("WARN message - something unexpected but recoverable");
        log.error("ERROR message - an operation failed");
        return Map.of("message", "Check the console and logs/application.log");
    }

    /** Triggers an unexpected exception to show how ERROR logging with a stack trace looks. */
    @GetMapping("/error")
    public Map<String, String> triggerError() {
        log.info("About to simulate an unexpected failure");
        throw new IllegalStateException("Simulated failure for the logging demo");
    }
}
