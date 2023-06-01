package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.jwt.JwtPair;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDto {

    private List<Matching> users;

    @RequiredArgsConstructor
    @Getter
    public static class Matching {
        private final String name;
        private final String sid;
        private final String email;

        public Matching(User user) {
            this.name = user.getName();
            this.sid = user.getSid();
            this.email = user.getEmail();
        }
    }

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
        List<String> courses = new ArrayList<>();
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
