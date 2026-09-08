package edu.handong.csee.histudy.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String REQUEST_ID_MDC_KEY = "request_id";
  private static final Pattern REQUEST_ID_PATTERN =
      Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
    long startedAt = System.nanoTime();
    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);

    int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    try {
      filterChain.doFilter(request, response);
      status = response.getStatus();
    } finally {
      log.info(
          "http_request request_id={} method={} path={} route={} status={} duration_ms={}",
          requestId,
          request.getMethod(),
          request.getRequestURI(),
          resolveRoute(request),
          status,
          elapsedMilliseconds(startedAt));
      MDC.remove(REQUEST_ID_MDC_KEY);
    }
  }

  private String resolveRequestId(String requestId) {
    return isValidRequestId(requestId) ? requestId : UUID.randomUUID().toString();
  }

  private boolean isValidRequestId(String requestId) {
    return requestId != null && REQUEST_ID_PATTERN.matcher(requestId).matches();
  }

  private String resolveRoute(HttpServletRequest request) {
    Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return route instanceof String ? (String) route : request.getRequestURI();
  }

  private long elapsedMilliseconds(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
