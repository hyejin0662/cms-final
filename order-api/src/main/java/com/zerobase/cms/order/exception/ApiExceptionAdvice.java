package com.zerobase.cms.order.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomExceptionResponse> customExceptionHandler(final CustomException e) {

        return ResponseEntity
                .status(e.getStatus())
                .body(CustomExceptionResponse.builder()
                        .message(e.getMessage())
                        .code(e.getErrorCode().name())
                        .status(e.getStatus()).build());
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomExceptionResponse {
        private int status;
        private String code;
        private String message;
    }
}
