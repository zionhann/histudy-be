package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.service.DiscordClient.Embed;
import edu.handong.csee.histudy.service.DiscordClient.Field;
import edu.handong.csee.histudy.service.DiscordClient.Payload;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordService {

  private static final int ERROR_COLOR = 15548997;
  private static final String UNKNOWN_VALUE = "Unknown";
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final DiscordClient client;

  public void notifyException(Exception e, WebRequest r) {
    try {
      if (r instanceof ServletWebRequest req) {
        String exceptionMessage = ExceptionUtils.getMessage(e);
        String requestMessage = getRequestMessage(req);
        String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
        String stackTrace = e.getStackTrace()[0].toString();

        Payload payload =
            createExceptionPayload(requestMessage, exceptionMessage, rootCauseMessage, stackTrace);
        client.executeWebhook(payload);
      }
    } catch (Exception ex) {
      log.error("Failed to send Discord notification for exception: {}", ex.getMessage(), ex);
    }
  }

  private String getRequestMessage(ServletWebRequest req) {
    try {
      return req.getHttpMethod().name() + " " + req.getRequest().getRequestURI();
    } catch (Exception e) {
      log.warn("Failed to build request message: {}", e.getMessage());
      return UNKNOWN_VALUE;
    }
  }

  private Payload createExceptionPayload(
      String requestSummary, String errorMessage, String rootCause, String stackTrace) {

    List<Field> fields = new ArrayList<>();

    fields.add(createField("Timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER)));
    fields.add(createField("Request", requestSummary));
    fields.add(createField("Location", stackTrace));

    if (errorMessage != null && !errorMessage.equals(rootCause)) {
      fields.add(createField("Root Cause", rootCause));
    }

    String title =
        (errorMessage != null && !errorMessage.isEmpty()) ? errorMessage : "Unknown Error Occurred";

    return new Payload(
        List.of(Embed.builder().title(title).color(ERROR_COLOR).fields(fields).build()));
  }

  private Field createField(String name, String value) {
    return Field.builder()
        .name(name)
        .value(value != null && !value.trim().isEmpty() ? value : UNKNOWN_VALUE)
        .inline(false)
        .build();
  }
}
