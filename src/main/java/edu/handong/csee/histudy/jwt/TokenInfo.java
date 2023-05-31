package edu.handong.csee.histudy.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenInfo {
    private final String tokenType = "Bearer ";
    private final GrantType grantType;
    private final String token;
}
