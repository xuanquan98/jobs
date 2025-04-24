package com.example.jobs.exception;


public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(400, "BAD_REQUEST", message);
    }
}
