package edu.handong.csee.histudy.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LogbackConfigurationTest {

  @Test
  void logbackConfigurationIsAvailableAtTheClasspathRoot() {
    assertThat(getClass().getClassLoader().getResource("logback-spring.xml")).isNotNull();
  }
}
