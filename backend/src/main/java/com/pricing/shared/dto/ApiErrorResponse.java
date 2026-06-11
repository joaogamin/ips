package com.pricing.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, LocalDateTime.now(), null);
    }

    public static ApiErrorResponse of(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        return new ApiErrorResponse(status, error, message, path, LocalDateTime.now(), fieldErrors);
    }
}
