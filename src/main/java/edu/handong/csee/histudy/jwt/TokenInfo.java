package edu.handong.csee.histudy.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenInfo {
    private final String tokenType = "Bearer ";
    private GrantType grantType;
    private String token;
}
