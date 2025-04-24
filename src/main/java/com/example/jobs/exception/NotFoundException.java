package com.example.jobs.exception;


public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }
}
