package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.ExceptionResponse;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.AcademicTermNotFoundException;
import edu.handong.csee.histudy.exception.CourseNotFoundException;
import edu.handong.csee.histudy.exception.DuplicateAcademicTermException;
import edu.handong.csee.histudy.exception.FileTransferException;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.exception.InvalidTokenTypeException;
import edu.handong.csee.histudy.exception.MissingEmailException;
import edu.handong.csee.histudy.exception.MissingParameterException;
import edu.handong.csee.histudy.exception.MissingSubException;
import edu.handong.csee.histudy.exception.MissingTokenException;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
import edu.handong.csee.histudy.exception.NoStudyApplicationFound;
import edu.handong.csee.histudy.exception.ReportNotFoundException;
import edu.handong.csee.histudy.exception.StudyGroupNotFoundException;
import edu.handong.csee.histudy.exception.UserAlreadyExistsException;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.service.DiscordService;
import io.jsonwebtoken.JwtException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

  private final DiscordService discordService;

  private void notifyException(Exception e, WebRequest req) {
    discordService.notifyException(e, req);
  }

  private ResponseEntity<ExceptionResponse> createErrorResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(ExceptionResponse.builder().status(status).message(message).build());
  }

  private ResponseEntity<ExceptionResponse> handleStandardException(
      Exception e, HttpStatus status, WebRequest request) {
    notifyException(e, request);
    return createErrorResponse(status, e.getMessage());
  }

  @ExceptionHandler({
    MissingParameterException.class,
    InvalidTokenTypeException.class,
    MissingEmailException.class,
    MissingSubException.class
  })
  public ResponseEntity<ExceptionResponse> handleBadRequest(Exception e, WebRequest request) {
    return handleStandardException(e, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e, WebRequest request) {
    notifyException(e, request);
    return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request format");
  }

  @ExceptionHandler({JwtException.class, MissingTokenException.class})
  public ResponseEntity<ExceptionResponse> handleUnauthorized(Exception e, WebRequest request) {
    return handleStandardException(e, HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ExceptionResponse> handleForbidden(
      ForbiddenException e, WebRequest request) {
    return handleStandardException(e, HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler({
    AcademicTermNotFoundException.class,
    CourseNotFoundException.class,
    StudyGroupNotFoundException.class,
    ReportNotFoundException.class,
    NoCurrentTermFoundException.class,
    NoStudyApplicationFound.class
  })
  public ResponseEntity<ExceptionResponse> handleNotFound(Exception e, WebRequest request) {
    return handleStandardException(e, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<UserDto.UserLogin> handleUserNotFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(UserDto.UserLogin.builder().isRegistered(false).build());
  }

  @ExceptionHandler({DuplicateAcademicTermException.class, UserAlreadyExistsException.class})
  public ResponseEntity<ExceptionResponse> handleConflict(Exception e, WebRequest request) {
    return handleStandardException(e, HttpStatus.CONFLICT, request);
  }

  @ExceptionHandler(FileTransferException.class)
  public ResponseEntity<ExceptionResponse> handleInternalServerError(
      FileTransferException e, WebRequest request) {
    return handleStandardException(e, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ExceptionResponse> runtimeException(
      RuntimeException e, WebRequest request) {
    log.error(
        "{}({}): {}", e.getMessage(), e.getClass().getName(), Arrays.toString(e.getStackTrace()));
    return handleStandardException(e, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }
}
