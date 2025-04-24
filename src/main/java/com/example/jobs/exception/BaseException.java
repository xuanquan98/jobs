package com.example.jobs.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final int code;
    private final String status;

    public BaseException(int code, String status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
