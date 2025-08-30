package edu.handong.csee.histudy.config;

import edu.handong.csee.histudy.service.DiscordClient;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class HttpClientConfig {

  @Value("${custom.webhook.discord:}")
  private Optional<String> discordWebhookUrlOr;

  @Bean
  public DiscordClient discordClient() {
    if (discordWebhookUrlOr.isEmpty()) {
      return createNoOpClient();
    }
    try {
      RestClient client = RestClient.builder().baseUrl(discordWebhookUrlOr.get()).build();

      HttpServiceProxyFactory factory =
          HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();

      log.info("Discord webhook client configured successfully");
      return factory.createClient(DiscordClient.class);
    } catch (Exception e) {
      log.error(
          "Failed to create Discord client, falling back to no-op client: {}", e.getMessage());
      return createNoOpClient();
    }
  }

  private DiscordClient createNoOpClient() {
    return __ -> log.debug("Discord notification skipped");
  }
}
