package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.PreferredCourse;
import edu.handong.csee.histudy.domain.StudyPartnerRequest;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.jwt.JwtPair;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDto {

  @Schema(description = "List of user", type = "array")
  private List<? extends UserMatching> users;

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @Deprecated
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

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class UserMatchingWithMasking extends UserMatching {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User student ID", example = "223****2")
    private String sid;

    public UserMatchingWithMasking(User user) {
      this.id = user.getId();
      this.name = user.getName();
      this.sid = user.getSidWithMasking();
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

    @Schema(description = "Role", example = "USER")
    private String role;
  }

  @Builder
  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class UserInfo {

    @Schema(description = "User ID", example = "1", type = "number")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User student ID", example = "211234567")
    private String sid;

    @Schema(description = "Group tag", example = "112")
    private Integer group;

    @Schema(description = "User email", example = "abc@example.ac.kr")
    private String email;

    @Schema(description = "list of friend added", type = "array")
    private List<UserBasic> friends;

    @Schema(description = "list of course added", type = "array")
    private List<CourseDto.BasicCourseInfo> courses;

    public UserInfo(User user, StudyApplicant applicant) {
      this.id = user.getId();
      this.name = user.getName();
      this.sid = user.getSid();
      this.email = user.getEmail();
      this.group = (applicant.getStudyGroup() == null) ? null : applicant.getStudyGroup().getTag();
      this.friends =
          applicant.getPartnerRequests().stream()
              .filter(StudyPartnerRequest::isAccepted)
              .map(StudyPartnerRequest::getReceiver)
              .map(UserBasic::new)
              .toList();
      this.courses =
          applicant.getPreferredCourses().stream()
              .sorted(Comparator.comparing(PreferredCourse::getPriority))
              .map(c -> new CourseDto.BasicCourseInfo(c.getCourse()))
              .toList();
    }

    public UserInfo(User user) {
      this.id = user.getId();
      this.name = user.getName();
      this.sid = user.getSid();
      this.email = user.getEmail();
      this.group = null;
      this.friends = Collections.emptyList();
      this.courses = Collections.emptyList();
    }
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
  public static class UserBasicWithMasking extends UserBasic {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User student ID", example = "223****2")
    private String sid;

    public UserBasicWithMasking(User user) {
      this.id = user.getId();
      this.sid = user.getSidWithMasking();
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

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class UserMeWithMasking extends UserMe {
    @Schema(description = "User ID", example = "1", type = "number")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User student ID", example = "223****4")
    private String sid;

    @Schema(description = "User email", example = "user@test.com")
    private String email;

    @Schema(description = "Group tag", example = "112")
    private Integer tag;

    public UserMeWithMasking(User user, int tag) {
      this.id = user.getId();
      this.sid = user.getSidWithMasking();
      this.name = user.getName();
      this.email = user.getEmail();
      this.tag = tag;
    }
  }

  @Builder
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserEdit {

    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "Group tag", example = "112")
    private Integer team;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "User student ID", example = "21800012")
    private String sid;
  }
}
