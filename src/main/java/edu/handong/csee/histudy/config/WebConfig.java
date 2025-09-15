package edu.handong.csee.histudy.config;

import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final JwtService jwtService;

  @Value("${custom.origin.allowed}")
  private String[] clients;

  @Value("${custom.path-patterns.exclude}")
  private String[] excludePathPatterns;

  @Value("${custom.path-patterns.include}")
  private String[] includePathPatterns;

  @Value("${custom.resource.path-pattern}")
  private String imageBasePathPattern;

  @Value("${custom.resource.location}")
  private String imageBaseLocation;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new AuthenticationInterceptor(jwtService))
        .excludePathPatterns(excludePathPatterns)
        .addPathPatterns(includePathPatterns);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOriginPatterns(clients)
        .allowedMethods("GET", "POST", "DELETE", "PATCH")
        .allowCredentials(true);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler(imageBasePathPattern)
        .addResourceLocations("file://" + imageBaseLocation);
  }
}
