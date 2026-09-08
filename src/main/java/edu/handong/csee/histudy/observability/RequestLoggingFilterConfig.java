package edu.handong.csee.histudy.observability;

import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingFilterConfig {

  @Bean
  public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration() {
    FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RequestLoggingFilter());
    registration.addUrlPatterns("/*");
    registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR));
    return registration;
  }
}
