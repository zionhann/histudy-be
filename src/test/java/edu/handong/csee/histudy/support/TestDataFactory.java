package edu.handong.csee.histudy.support;

import edu.handong.csee.histudy.domain.*;
import java.util.List;

public class TestDataFactory {

  public static AcademicTerm createCurrentTerm() {
    return AcademicTerm.builder()
        .academicYear(2025)
        .semester(TermType.SPRING)
        .isCurrent(true)
        .build();
  }

  public static AcademicTerm createPastTerm() {
    return AcademicTerm.builder()
        .academicYear(2024)
        .semester(TermType.FALL)
        .isCurrent(false)
        .build();
  }

  public static AcademicTerm createAcademicTerm(int year, TermType semester, boolean isCurrent) {
    return AcademicTerm.builder()
        .academicYear(year)
        .semester(semester)
        .isCurrent(isCurrent)
        .build();
  }

  public static User createUser(String suffix) {
    return User.builder()
        .sub("sub-" + suffix)
        .sid("2250010" + suffix)
        .email("user" + suffix + "@test.com")
        .name("User " + suffix)
        .role(Role.USER)
        .build();
  }

  public static User createUser(String sub, String sid, String email, String name, Role role) {
    return User.builder().sub(sub).sid(sid).email(email).name(name).role(role).build();
  }

  public static Course createCourse(String name, String code, AcademicTerm term) {
    return Course.builder()
        .name(name)
        .code(code)
        .professor("Prof " + code)
        .academicTerm(term)
        .build();
  }

  public static Course createCourse(String name, String code, String professor, AcademicTerm term) {
    return Course.builder().name(name).code(code).professor(professor).academicTerm(term).build();
  }

  public static StudyGroup createStudyGroup(int tag, AcademicTerm term) {
    // Create a simple study group without members for basic testing
    return StudyGroup.of(tag, term, List.of());
  }

  public static StudyApplicant createStudyApplicant(
      AcademicTerm term, User user, List<User> preferredFriends, List<Course> preferredCourses) {
    return StudyApplicant.of(term, user, preferredFriends, preferredCourses);
  }

  public static StudyReport createStudyReport(StudyGroup studyGroup, String content) {
    return StudyReport.builder()
        .title("Test Report")
        .studyGroup(studyGroup)
        .content(content)
        .totalMinutes(60L)
        .participants(List.of())
        .images(List.of())
        .courses(List.of())
        .build();
  }

  public static StudyReport createStudyReport(
      StudyGroup studyGroup, String content, String imagePath) {
    return StudyReport.builder()
        .title("Test Report")
        .studyGroup(studyGroup)
        .content(content)
        .totalMinutes(60L)
        .participants(List.of())
        .images(List.of(imagePath))
        .courses(List.of())
        .build();
  }
}
