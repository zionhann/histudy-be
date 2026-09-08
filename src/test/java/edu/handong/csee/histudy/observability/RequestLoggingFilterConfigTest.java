package edu.handong.csee.histudy.observability;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

class RequestLoggingFilterConfigTest {

  @Test
  void 요청_로깅_필터를_등록하면_REQUEST와_ERROR_디스패치에_매핑한다() {
    // given
    RequestLoggingFilterConfig config = new RequestLoggingFilterConfig();

    // when
    FilterRegistrationBean<RequestLoggingFilter> registration =
        config.requestLoggingFilterRegistration();

    // then
    assertThat(registration.getFilter()).isInstanceOf(RequestLoggingFilter.class);
    assertThat(registration.getUrlPatterns()).containsExactly("/*");
    assertThat(registration.determineDispatcherTypes())
        .containsExactlyInAnyOrderElementsOf(
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));
  }
}
