package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.ExceptionResponse;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.AcademicTermNotFoundException;
import edu.handong.csee.histudy.exception.DuplicateAcademicTermException;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.exception.MissingParameterException;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import io.jsonwebtoken.JwtException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
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

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ExceptionResponse> jwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .message(e.getMessage())
                        .trace(Arrays.toString(e.getStackTrace()))
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserDto.UserLogin> userNotFound() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(UserDto.UserLogin.builder()
                        .isRegistered(false)
                        .build());
    }

    @ExceptionHandler({
            MissingParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ExceptionResponse> missingParameter(MissingParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .trace(Arrays.toString(e.getStackTrace()))
                        .build());
    }

  @ExceptionHandler(DuplicateAcademicTermException.class)
  public ResponseEntity<ExceptionResponse> duplicateAcademicTerm(DuplicateAcademicTermException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ExceptionResponse.builder().message(e.getMessage()).build());
  }

  @ExceptionHandler(AcademicTermNotFoundException.class)
  public ResponseEntity<ExceptionResponse> academicTermNotFound(AcademicTermNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ExceptionResponse.builder().message(e.getMessage()).build());
  }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> runtimeException(RuntimeException e) {
        log.error("{}({}): {}", e.getMessage(), e.getClass().getName(), Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(e.getMessage())
                        .trace(Arrays.toString(e.getStackTrace()))
                        .build());
    }
}
