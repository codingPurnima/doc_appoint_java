package com.docappoint.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime timestamp,
        Map<String, String> details
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, OffsetDateTime.now(), null);
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> details) {
        this(status, error, message, OffsetDateTime.now(), details);
    }
}
