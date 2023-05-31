package edu.handong.csee.histudy.interceptor;

import edu.handong.csee.histudy.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final static String BEARER = "Bearer ";
    private final JwtService jwtService;

    @Value("${custom.origin.allowed}")
    private String client;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional<String> token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(value -> value.startsWith(BEARER))
                .map(value -> value.substring(BEARER.length()));
        Optional<Claims> claims = jwtService.validate(token);

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, client);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, DELETE, PATCH, OPTIONS");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, Content-Type, Authorization");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

        if (claims.isPresent()) {
            request.setAttribute("claims", claims.get());
            return true;
        }
        response.sendError(HttpStatus.UNAUTHORIZED.value());
        return false;
    }
}
