package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.jwt.JwtPair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDto {

    @Schema(description = "List of user", type = "array")
    private List<UserMatching> users;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserMatching {

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User student ID", example = "211234567")
        private String sid;

        @Schema(description = "User email", example = "jd@example.com")
        private String email;

        public UserMatching(User user) {
            this.name = user.getName();
            this.sid = user.getSid();
            this.email = user.getEmail();
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserLogin {
        @Schema(description = "Registration status", example = "true", type = "boolean")
        private Boolean isRegistered;

        @Schema(description = "Token type", example = "Bearer ")
        private String tokenType;

        @Schema(description = "Token pairs", type = "object")
        private JwtPair tokens;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString
    public static class UserInfo {

        @Schema(description = "User ID", example = "1", type = "number")
        private Long id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User student ID", example = "211234567")
        private String sid;

        @Schema(description = "Group tag", example = "112")
        private int group;

        @Schema(description = "list of friend added", type = "array")
        private List<UserBasic> friends;

        @Schema(description = "list of course added", type = "array")
        private List<CourseIdNameDto> courses;

        @Schema(description = "student's total minutes studied", type = "number")
        private long totalMinutes;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserBasic {

        @Schema(description = "User ID", example = "1", type = "number")
        private Long id;

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

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserMe {
        @Schema(description = "User ID", example = "1", type = "number")
        private Long id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User student ID", example = "211234567")
        private String sid;

        @Schema(description = "User email", example = "user@test.com")
        private String email;

        public UserMe(User user) {
            this.id = user.getId();
            this.sid = user.getSid();
            this.name = user.getName();
            this.email = user.getEmail();
        }
    }
}
