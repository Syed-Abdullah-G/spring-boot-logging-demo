package com.example.loggingdemo.exception;

public class InvalidProductException extends RuntimeException {

    public InvalidProductException(String message) {
        super(message);
    }
}
