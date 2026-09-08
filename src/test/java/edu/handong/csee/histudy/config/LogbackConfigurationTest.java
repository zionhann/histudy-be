package edu.handong.csee.histudy.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

class LogbackConfigurationTest {

  @Test
  void classpath_루트에_Logback_설정이_있으면_중첩_경로에는_없다() {
    // given
    ClassLoader classLoader = getClass().getClassLoader();

    // when
    var rootResource = classLoader.getResource("logback-spring.xml");
    var nestedResource = classLoader.getResource("logback/logback-spring.xml");

    // then
    assertThat(rootResource).isNotNull();
    assertThat(nestedResource).isNull();
  }

  @Test
  void bootJar를_패키징하면_Logback_설정을_실행시_사용할_수_있다() throws IOException {
    // given
    String bootJarPath = System.getProperty("bootJarPath");
    assertThat(bootJarPath).as("bootJarPath system property").isNotBlank();

    // when
    try (JarFile bootJar = new JarFile(Path.of(bootJarPath).toFile())) {
      // then
      assertThat(bootJar.getEntry("BOOT-INF/classes/logback-spring.xml")).isNotNull();
    }
  }
}
