package edu.handong.csee.histudy.config;

import edu.handong.csee.histudy.service.DiscordClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class HttpClientConfig {

  @Value("${custom.webhook.discord:}")
  private String discordWebhookUrlOr;

  @Bean
  public DiscordClient discordClient() {
    if (!StringUtils.hasText(discordWebhookUrlOr)) {
      return createNoOpClient();
    }
    try {
      RestClient client = RestClient.builder().baseUrl(discordWebhookUrlOr).build();
      RestClientAdapter adapter = RestClientAdapter.create(client);
      HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

      log.info("Discord webhook client configured successfully");
      return factory.createClient(DiscordClient.class);
    } catch (Exception e) {
      log.error("Failed to create Discord client, falling back to no-op client.", e);
      return createNoOpClient();
    }
  }

  private DiscordClient createNoOpClient() {
    return __ -> log.debug("Discord notification skipped");
  }
}
