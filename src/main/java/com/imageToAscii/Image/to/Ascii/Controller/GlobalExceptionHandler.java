package com.imageToAscii.Image.to.Ascii.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnexpectedFormatException.class)
    public ResponseEntity<?> handleUnexpectedFormat(UnexpectedFormatException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("UNSUPPORTED_FORMAT", ex.getMessage()));
    }

    record ErrorResponse(String error, String message) {}
}
