package edu.handong.csee.histudy.interceptor;

import edu.handong.csee.histudy.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        Optional<String> headerOr = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));
        String token = jwtService.extractToken(headerOr);
        Claims claims = jwtService.validate(token);
        request.setAttribute("claims", claims);

        return true;
    }
}
