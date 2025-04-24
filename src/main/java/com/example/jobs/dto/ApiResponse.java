package com.example.jobs.dto;

import lombok.Data;
import java.util.List;

@Data
public class ApiResponse<T> {
    private int code;
    private String status;
    private String message;
    private List<String> errors;
    private T data;

    public ApiResponse(int code, String status, String message, List<String> errors, T data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.data = data;
    }

    // Success response
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "SUCCESS", "Operation successful", null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, "SUCCESS", message, null, data);
    }

    // Error response
    public static <T> ApiResponse<T> error(int code, String status, String message, List<String> errors) {
        return new ApiResponse<>(code, status, message, errors, null);
    }

    public static <T> ApiResponse<T> error(int code, String status, String message) {
        return new ApiResponse<>(code, status, message, null, null);
    }


}
