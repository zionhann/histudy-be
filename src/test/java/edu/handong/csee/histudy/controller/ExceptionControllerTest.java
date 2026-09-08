package edu.handong.csee.histudy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.handong.csee.histudy.dto.ExceptionResponse;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.service.DiscordService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class ExceptionControllerTest {

  private static final String REQUEST_ID = "request-123";

  private DiscordService discordService;
  private ExceptionController exceptionController;
  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  void setUp() {
    discordService = mock(DiscordService.class);
    exceptionController = new ExceptionController(discordService);

    Logger logger = (Logger) LoggerFactory.getLogger(ExceptionController.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
  }

  @AfterEach
  void tearDown() {
    Logger logger = (Logger) LoggerFactory.getLogger(ExceptionController.class);
    logger.detachAppender(logAppender);
    MDC.clear();
  }

  @Test
  void unhandledExceptionIncludesRequestIdAndLogsTypeWithoutRawMessage() {
    MDC.put("request_id", REQUEST_ID);
    RuntimeException exception = new IllegalStateException("secret-token-value");
    ServletWebRequest request = webRequest();

    ResponseEntity<?> response = exceptionController.runtimeException(exception, request);

    assertThat(response.getBody()).isInstanceOf(ExceptionResponse.class);
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertThat(body.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(logAppender.list)
        .anySatisfy(
            event -> {
              assertThat(event.getFormattedMessage())
                  .contains("unhandled_exception")
                  .contains("request_id=" + REQUEST_ID)
                  .contains("exception_type=IllegalStateException")
                  .doesNotContain("secret-token-value");
              assertThat(event.getThrowableProxy().getClassName())
                  .isEqualTo(IllegalStateException.class.getName());
            });
    verify(discordService).notifyException(exception, request);
  }

  @Test
  void unauthorizedExceptionDoesNotExposeParserMessageAndLogsFailure() {
    MDC.put("request_id", REQUEST_ID);
    JwtException exception = new JwtException("token-parser-details");

    ResponseEntity<?> response = exceptionController.handleUnauthorized(exception, webRequest());

    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertThat(body.getMessage()).isEqualTo("인증 정보가 유효하지 않습니다.");
    assertThat(body.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(logAppender.list)
        .anySatisfy(
            event ->
                assertThat(event.getFormattedMessage())
                    .contains("authentication_failed")
                    .contains("request_id=" + REQUEST_ID)
                    .contains("exception_type=JwtException")
                    .doesNotContain("token-parser-details"));
  }

  @Test
  void forbiddenExceptionLogsAuthorizationDenialWithRequestId() {
    MDC.put("request_id", REQUEST_ID);

    ResponseEntity<?> response =
        exceptionController.handleForbidden(new ForbiddenException(), webRequest());

    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertThat(body.getRequestId()).isEqualTo(REQUEST_ID);
    assertThat(logAppender.list)
        .anySatisfy(
            event ->
                assertThat(event.getFormattedMessage())
                    .contains("authorization_denied")
                    .contains("request_id=" + REQUEST_ID)
                    .contains("exception_type=ForbiddenException"));
  }

  private ServletWebRequest webRequest() {
    return new ServletWebRequest(new MockHttpServletRequest("GET", "/api/test"));
  }
}
