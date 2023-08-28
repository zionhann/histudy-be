package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.ExceptionResponse;
import edu.handong.csee.histudy.exception.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> forbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .message(e.getMessage())
                        .trace(Arrays.toString(e.getStackTrace()))
                        .build());
    }
}
