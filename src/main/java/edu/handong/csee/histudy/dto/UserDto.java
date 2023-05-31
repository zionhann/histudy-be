package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.jwt.JwtPair;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

public class UserDto {

    @Builder
    @Getter
    public static class Login {
        private final Boolean isRegistered;
        private final String tokenType;
        private final JwtPair tokens;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Info {
        private String id;
        private String name;
        private String sid;
        List<Basic> friends = new ArrayList<>();
        List<String> 
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Basic {
        private String id;
        private String name;
        private String sid;
    }

}
