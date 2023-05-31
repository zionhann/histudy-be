package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.jwt.JwtPair;
import lombok.Builder;
import lombok.Getter;

public class UserDto {

    @Builder
    @Getter
    public static class Login {
        private final Boolean isRegistered;
        private final String tokenType;
        private final JwtPair tokens;
    }
}
