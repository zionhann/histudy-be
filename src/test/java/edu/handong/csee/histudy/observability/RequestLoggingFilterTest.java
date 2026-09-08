package edu.handong.csee.histudy.observability;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

class RequestLoggingFilterTest {

  private static final String REQUEST_ID_HEADER = "X-Request-ID";

  private final RequestLoggingFilter filter = new RequestLoggingFilter();
  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  void setUp() {
    Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
  }

  @AfterEach
  void tearDown() {
    Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    logger.detachAppender(logAppender);
    MDC.clear();
  }

  @Test
  void incomingRequestIdIsReturnedAndIncludedInTheCompletedRequestLog() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
    request.addHeader(REQUEST_ID_HEADER, "request-123");
    request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/test");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(REQUEST_ID_HEADER)).isEqualTo("request-123");
    assertThat(MDC.get("request_id")).isNull();
    assertThat(logAppender.list)
        .anySatisfy(
            event -> {
              assertThat(event.getFormattedMessage())
                  .contains("http_request")
                  .contains("request_id=request-123")
                  .contains("method=GET")
                  .contains("path=/api/test")
                  .contains("status=200");
              assertThat(event.getMDCPropertyMap()).containsEntry("request_id", "request-123");
            });
  }

  @Test
  void invalidIncomingRequestIdIsReplacedWithGeneratedId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
    request.addHeader(REQUEST_ID_HEADER, "invalid request id");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    String requestId = response.getHeader(REQUEST_ID_HEADER);
    assertThat(requestId).isNotEqualTo("invalid request id").matches("[0-9a-f-]{36}");
    assertThat(MDC.get("request_id")).isNull();
  }

  @Test
  void failedRequestIsLoggedAsServerErrorAndTheOriginalExceptionIsPropagated() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain failingChain = (servletRequest, servletResponse) -> {
      throw new IOException("downstream failure");
    };

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> filter.doFilter(request, response, failingChain))
        .isInstanceOf(IOException.class);

    assertThat(logAppender.list)
        .anySatisfy(
            event -> assertThat(event.getFormattedMessage()).contains("status=500"));
  }
}
