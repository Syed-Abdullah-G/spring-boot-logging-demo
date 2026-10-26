package com.example.loggingdemo.exception;

import java.time.Instant;

/**
 * JSON body returned to clients for every handled error.
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
