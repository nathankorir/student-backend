package com.school.student.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.MissingResourceException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDto> handleNoSuchElementException(NoSuchElementException ex, HttpServletRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder().errorCode(404).path(request.getRequestURI()).detail(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder().errorCode(500).path(request.getRequestURI()).detail(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDto);
    }

    @ExceptionHandler(MissingResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(MissingResourceException ex, HttpServletRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder().errorCode(404).path(request.getRequestURI()).detail(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDto);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder().errorCode(400).path(request.getRequestURI()).detail(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }
}
