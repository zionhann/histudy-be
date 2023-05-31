package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.jwt.JwtPair;
import lombok.*;

import java.util.List;

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
}
