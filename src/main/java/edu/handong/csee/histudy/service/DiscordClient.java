package edu.handong.csee.histudy.service;

import java.util.List;
import lombok.Builder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface DiscordClient {

  record Payload(String username, List<Embed> embeds) {
    public Payload(List<Embed> embeds) {
      this("HIStudy", embeds);
    }
  }

  @Builder
  record Embed(String title, int color, List<Field> fields) {}

  @Builder
  record Field(String name, String value, boolean inline) {}

  @PostExchange
  void executeWebhook(@RequestBody Payload payload);
}
