package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.jwt.JwtPair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDto {

    @Schema(description = "List of user", type = "array")
    private List<UserMatching> users;

    @RequiredArgsConstructor
    @Getter
    public static class UserMatching {

        @Schema(description = "User name", example = "John Doe")
        private final String name;

        @Schema(description = "User student ID", example = "211234567")
        private final String sid;

        @Schema(description = "User email", example = "jd@example.com")
        private final String email;

        public UserMatching(User user) {
            this.name = user.getName();
            this.sid = user.getSid();
            this.email = user.getEmail();
        }
    }

    @Builder
    @Getter
    public static class UserLogin {
        @Schema(description = "Registration status", example = "true", type = "boolean")
        private final Boolean isRegistered;

        @Schema(description = "Token type", example = "Bearer ")
        private final String tokenType;

        @Schema(description = "Token pairs", type = "object")
        private final JwtPair tokens;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString
    public static class UserInfo {

        @Schema(description = "User ID", example = "1", type = "number")
        private String id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User student ID", example = "211234567")
        private String sid;

        @Schema(description = "list of friend added", type = "array")
        private List<UserBasic> friends;

        @Schema(description = "list of course added", type = "array")
        private List<CourseIdNameDto> courses;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserBasic {

        @Schema(description = "User ID", example = "1", type = "number")
        private String id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User student ID", example = "211234567")
        private String sid;

        public UserBasic(User user) {
            this.id = user.getId();
            this.sid = user.getSid();
            this.name = user.getName();
        }
    }

}
