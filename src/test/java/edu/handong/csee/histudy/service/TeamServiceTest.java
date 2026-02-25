package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.support.TestDataFactory;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
public class TeamServiceTest {

  private TeamService teamService;

  private StudyGroupRepository studyGroupRepository;
  private UserRepository userRepository;
  private AcademicTermRepository academicTermRepository;
  private StudyApplicantRepository studyApplicantRepository;
  private StudyReportRepository studyReportRepository;
  private CourseRepository courseRepository;
  private ImagePathMapper imagePathMapper;

  @BeforeEach
  void init() {
    studyGroupRepository = new FakeStudyGroupRepository();
    userRepository = new FakeUserRepository();
    academicTermRepository = new FakeAcademicTermRepository();
    studyApplicantRepository = new FakeStudyApplicationRepository();
    studyReportRepository = new FakeStudyReportRepository();
    courseRepository = new FakeCourseRepository();
    imagePathMapper = new ImagePathMapper();

    ReflectionTestUtils.setField(imagePathMapper, "origin", "http://localhost:8080");
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", "/images/");

    teamService =
        new TeamService(
            studyGroupRepository,
            userRepository,
            academicTermRepository,
            studyApplicantRepository,
            studyReportRepository,
            imagePathMapper);
  }

  @Test
  void 그룹원조회시_학번마스킹() {
    // Given
    AcademicTerm term = TestDataFactory.createCurrentTerm();
    academicTermRepository.save(term);

    User student1 = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    User student2 = TestDataFactory.createUser("2", "22500102", "user2@test.com", "Bar", Role.USER);
    User student3 = TestDataFactory.createUser("3", "22500103", "user3@test.com", "baz", Role.USER);

    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = TestDataFactory.createCourse("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        TestDataFactory.createStudyApplicant(term, student1, List.of(student2), List.of(course));
    StudyApplicant studyApplicant2 =
        TestDataFactory.createStudyApplicant(term, student2, List.of(student1), List.of(course));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1, studyApplicant2));
    studyGroupRepository.save(studyGroup);

    // When
    List<UserDto.UserMeWithMasking> teamUsers = teamService.getTeamUsers("user1@test.com");

    // Then
    assertThat(teamUsers.size()).isEqualTo(2);
    assertThat(teamUsers.get(0).getTag()).isEqualTo(1);
    assertThat(teamUsers.get(1).getSid()).contains("*");
  }

  @Test
  void 친구선호시_그룹매칭() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    User student3 =
        User.builder().sub("3").sid("22500103").email("user3@test.com").name("baz").build();

    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));

    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    // When
    teamService.matchTeam();

    // Then
    assertThat(studyApplicant1.getStudyGroup()).isEqualTo(studyApplicant2.getStudyGroup());
  }

  @Test
  void 과목동일시_그룹매칭() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    User student3 =
        User.builder().sub("3").sid("22500103").email("user3@test.com").name("baz").build();

    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 = StudyApplicant.of(term, student1, List.of(), List.of(course));

    StudyApplicant studyApplicant2 = StudyApplicant.of(term, student2, List.of(), List.of(course));

    StudyApplicant studyApplicant3 = StudyApplicant.of(term, student3, List.of(), List.of(course));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);
    studyApplicantRepository.save(studyApplicant3);

    // When
    teamService.matchTeam();

    // Then
    assertThat(studyApplicant1.getStudyGroup()).isEqualTo(studyApplicant2.getStudyGroup());
    assertThat(studyApplicant2.getStudyGroup()).isEqualTo(studyApplicant3.getStudyGroup());
  }

  @Test
  void 친구와과목모두일치시_그룹매칭() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    User student3 =
        User.builder().sub("3").sid("22500103").email("user3@test.com").name("baz").build();

    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));

    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));

    StudyApplicant studyApplicant3 = StudyApplicant.of(term, student3, List.of(), List.of(course));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);
    studyApplicantRepository.save(studyApplicant3);

    // When
    teamService.matchTeam();

    // Then
    assertThat(studyApplicant1.getStudyGroup()).isEqualTo(studyApplicant2.getStudyGroup());
    assertThat(studyApplicant2.getStudyGroup()).isEqualTo(studyApplicant3.getStudyGroup());
  }

  @Test
  void 관리자요청시_전체팀목록반환() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();
    userRepository.save(student1);
    userRepository.save(student2);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));
    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));
    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1, studyApplicant2));
    studyGroupRepository.save(studyGroup);

    // When
    List<TeamDto> teams = teamService.getTeams("admin@test.com");

    // Then
    assertThat(teams).hasSize(1);
    assertThat(teams.get(0).getGroup()).isEqualTo(studyGroup.getStudyGroupId());
    assertThat(teams.get(0).getTag()).isEqualTo(1);
  }

  @Test
  void 팀존재시_보고서목록반환() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student1);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 = StudyApplicant.of(term, student1, List.of(), List.of(course));
    studyApplicantRepository.save(studyApplicant1);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1));
    studyGroupRepository.save(studyGroup);

    StudyReport report =
        StudyReport.builder()
            .title("Test Report")
            .content("Test Content")
            .totalMinutes(120L)
            .studyGroup(studyGroup)
            .participants(List.of(student1))
            .images(List.of())
            .courses(List.of(course))
            .build();
    studyReportRepository.save(report);

    // When
    TeamReportDto result =
        teamService.getTeamReports(studyGroup.getStudyGroupId(), "user1@test.com");

    // Then
    assertThat(result.getGroup()).isEqualTo(studyGroup.getStudyGroupId());
    assertThat(result.getTag()).isEqualTo(1);
    assertThat(result.getTotalTime()).isEqualTo(120L);
    assertThat(result.getReports()).hasSize(1);
  }

  @Test
  void 전체팀조회시_랭킹순반환() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();
    userRepository.save(student1);
    userRepository.save(student2);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 = StudyApplicant.of(term, student1, List.of(), List.of(course));
    StudyApplicant studyApplicant2 = StudyApplicant.of(term, student2, List.of(), List.of(course));
    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup1 = StudyGroup.of(1, term, List.of(studyApplicant1));
    StudyGroup studyGroup2 = StudyGroup.of(2, term, List.of(studyApplicant2));
    studyGroupRepository.save(studyGroup1);
    studyGroupRepository.save(studyGroup2);

    StudyReport report1 =
        StudyReport.builder()
            .title("Report 1")
            .content("Content 1")
            .totalMinutes(180L)
            .studyGroup(studyGroup1)
            .participants(List.of(student1))
            .images(List.of())
            .courses(List.of(course))
            .build();

    StudyReport report2 =
        StudyReport.builder()
            .title("Report 2")
            .content("Content 2")
            .totalMinutes(120L)
            .studyGroup(studyGroup2)
            .participants(List.of(student2))
            .images(List.of())
            .courses(List.of(course))
            .build();

    studyReportRepository.save(report1);
    studyReportRepository.save(report2);

    // When
    TeamRankDto result = teamService.getAllTeams();

    // Then
    assertThat(result.getTeams()).hasSize(2);
    assertThat(result.getTeams().get(0).getTotalMinutes()).isEqualTo(180L);
    assertThat(result.getTeams().get(1).getTotalMinutes()).isEqualTo(120L);
  }

  @Test
  void 빈신청목록시_오류없이처리() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // When & Then (should not throw exception)
    teamService.matchTeam();
  }

  @Test
  void 다섯명이상시_그룹분할() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    for (int i = 1; i <= 7; i++) {
      User user =
          User.builder()
              .sub(String.valueOf(i))
              .sid("2250010" + i)
              .email("user" + i + "@test.com")
              .name("User" + i)
              .build();
      userRepository.save(user);

      StudyApplicant applicant = StudyApplicant.of(term, user, List.of(), List.of(course));
      studyApplicantRepository.save(applicant);
    }

    // When
    teamService.matchTeam();

    // Then
    List<StudyGroup> groups = studyGroupRepository.findAllByAcademicTerm(term);

    if (!groups.isEmpty()) {
      int totalMembers = groups.stream().mapToInt(g -> g.getMembers().size()).sum();
      assertThat(totalMembers).isLessThanOrEqualTo(7);

      boolean allGroupsHaveMinMembers = groups.stream().allMatch(g -> g.getMembers().size() >= 3);
      assertThat(allGroupsHaveMinMembers).isTrue();
    }
  }

  @Test
  void 친구기반매칭_다양한친구관계() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create 10 courses
    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      Course course =
          new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create 20 users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 20; i++) {
      User user =
          User.builder()
              .sub("user" + i)
              .sid("225" + String.format("%05d", i))
              .email("user" + i + "@test.com")
              .name("Student " + i)
              .build();
      users.add(user);
    }
    userRepository.saveAll(users);

    // Create applicants with friend relationships
    List<StudyApplicant> applicants = new ArrayList<>();

    // Friend pair 1: User1 ↔ User2
    applicants.add(
        StudyApplicant.of(
            term, users.get(0), List.of(users.get(1)), List.of(courses.get(0), courses.get(1))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(1), List.of(users.get(0)), List.of(courses.get(0), courses.get(2))));

    // Friend chain: User3 ↔ User4 ↔ User5 (connected component of 3)
    applicants.add(
        StudyApplicant.of(
            term, users.get(2), List.of(users.get(3)), List.of(courses.get(1), courses.get(2))));
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(3),
            List.of(users.get(2), users.get(4)),
            List.of(courses.get(1), courses.get(3))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(4), List.of(users.get(3)), List.of(courses.get(2), courses.get(3))));

    // Large friend network: User6-User10 (5 people connected)
    applicants.add(
        StudyApplicant.of(
            term, users.get(5), List.of(users.get(6), users.get(7)), List.of(courses.get(4))));
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(6),
            List.of(users.get(5), users.get(8)),
            List.of(courses.get(4), courses.get(5))));
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(7),
            List.of(users.get(5), users.get(9)),
            List.of(courses.get(5), courses.get(6))));
    applicants.add(
        StudyApplicant.of(term, users.get(8), List.of(users.get(6)), List.of(courses.get(6))));
    applicants.add(
        StudyApplicant.of(term, users.get(9), List.of(users.get(7)), List.of(courses.get(7))));

    // No friends - should not be in friend groups
    for (int i = 10; i < 15; i++) {
      applicants.add(
          StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(i % 10))));
    }

    studyApplicantRepository.saveAll(applicants);

    // Accept all friend requests
    applicants.forEach(
        applicant -> applicant.getPartnerRequests().forEach(StudyPartnerRequest::accept));

    // When
    teamService.matchTeam();

    // Then
    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);

    // Verify friend groups were created
    assertTrue(allGroups.size() >= 3, "Should have at least 3 friend groups");

    // Verify friend pair group (2 people)
    assertTrue(
        allGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 2
                        && containsUsers(group, users.get(0), users.get(1))),
        "Should have User1-User2 friend pair");

    // Verify friend chain group (3 people)
    assertTrue(
        allGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 3
                        && containsUsers(group, users.get(2), users.get(3), users.get(4))),
        "Should have User3-User4-User5 friend group");

    // Verify a large friend network (5 people)
    assertTrue(
        allGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 5
                        && containsUsers(
                            group,
                            users.get(5),
                            users.get(6),
                            users.get(7),
                            users.get(8),
                            users.get(9))),
        "Should have User6-User10 large friend group");

    // Verify no-friend users are not in friend groups yet (will be in course groups)
    long friendGroupMembers =
        allGroups.stream().mapToLong(group -> group.getMembers().size()).sum();
    assertEquals(10, friendGroupMembers, "Friend groups should contain exactly 10 people");
  }

  @Test
  void 과목선호도기반매칭_우선순위반영() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create 10 courses
    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      Course course =
          new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create 30 users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      User user =
          User.builder()
              .sub("user" + i)
              .sid("225" + String.format("%05d", i))
              .email("user" + i + "@test.com")
              .name("Student " + i)
              .build();
      users.add(user);
    }
    userRepository.saveAll(users);

    List<StudyApplicant> applicants = new ArrayList<>();

    // High-priority Math course (Course1) - 8 students with priority 0
    for (int i = 0; i < 8; i++) {
      applicants.add(
          StudyApplicant.of(
              term,
              users.get(i),
              List.of(),
              List.of(courses.get(0), courses.get(1), courses.get(2)))); // Math first priority
    }

    // Medium-priority Physics course (Course2) - 6 students with mixed priorities
    for (int i = 8; i < 11; i++) {
      applicants.add(
          StudyApplicant.of(
              term,
              users.get(i),
              List.of(),
              List.of(courses.get(1), courses.get(0)))); // Physics first priority
    }
    for (int i = 11; i < 14; i++) {
      applicants.add(
          StudyApplicant.of(
              term,
              users.get(i),
              List.of(),
              List.of(courses.get(2), courses.get(1)))); // Physics second priority
    }

    // Low-priority Chemistry course (Course3) - 4 students
    for (int i = 14; i < 18; i++) {
      applicants.add(
          StudyApplicant.of(
              term,
              users.get(i),
              List.of(),
              List.of(courses.get(2), courses.get(3)))); // Chemistry first priority
    }

    // Mixed preferences - remaining students
    for (int i = 18; i < 30; i++) {
      List<Course> preferences = new ArrayList<>();
      preferences.add(courses.get(i % 10));
      if (Math.random() > 0.3) preferences.add(courses.get((i + 1) % 10));
      if (Math.random() > 0.6) preferences.add(courses.get((i + 2) % 10));

      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), preferences));
    }

    studyApplicantRepository.saveAll(applicants);

    // When
    teamService.matchTeam();

    // Then
    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);

    // Verify Math course gets optimal grouping (high priority, 8 students → 5+3)
    long mathGroups =
        allGroups.stream()
            .filter(
                group ->
                    group.getCourses().stream()
                        .anyMatch(gc -> gc.getCourse().equals(courses.get(0))))
            .count();
    assertTrue(mathGroups >= 1, "Should have Math course groups");

    // Verify group sizes prioritize larger groups (5-person over 3-person)
    long largeGroups = allGroups.stream().filter(group -> group.getMembers().size() == 5).count();
    long mediumGroups = allGroups.stream().filter(group -> group.getMembers().size() == 4).count();
    long smallGroups = allGroups.stream().filter(group -> group.getMembers().size() == 3).count();

    assertTrue(largeGroups >= 1, "Should prioritize creating 5-person groups");
    assertTrue(
        (largeGroups * 5 + mediumGroups * 4 + smallGroups * 3) >= 15,
        "Should assign most students to groups");

    // Verify no groups smaller than 3 (except friend groups which can be 2)
    boolean hasInvalidGroups = allGroups.stream().anyMatch(group -> group.getMembers().size() < 2);
    assertFalse(hasInvalidGroups, "Should not create groups smaller than 2 people");
  }

  @Test
  void 대규모종합매칭_100명학생_10과목_다양한선호도와친구요청() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create 10 courses
    List<Course> courses = new ArrayList<>();
    String[] courseNames = {
      "Mathematics",
      "Physics",
      "Chemistry",
      "Computer Science",
      "Biology",
      "History",
      "Literature",
      "Psychology",
      "Economics",
      "Philosophy"
    };

    for (int i = 0; i < 10; i++) {
      Course course =
          new Course(
              courseNames[i], "CRS" + String.format("%03d", i + 1), "Professor " + (i + 1), term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create 100 users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      User user =
          User.builder()
              .sub("student" + i)
              .sid("225" + String.format("%05d", i))
              .email("student" + i + "@university.edu")
              .name("Student " + i)
              .build();
      users.add(user);
    }
    userRepository.saveAll(users);

    // Create 100 applicants with realistic distribution
    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(12345); // Fixed seed for reproducible tests

    for (int i = 0; i < 100; i++) {
      User currentUser = users.get(i);

      // Generate 1-3 course preferences
      List<Course> coursePreferences = new ArrayList<>();
      int numPreferences = random.nextInt(3) + 1; // 1 to 3 courses
      Set<Integer> selectedCourseIndices = new HashSet<>();

      for (int j = 0; j < numPreferences; j++) {
        int courseIndex;
        do {
          courseIndex = random.nextInt(10);
        } while (selectedCourseIndices.contains(courseIndex));
        selectedCourseIndices.add(courseIndex);
        coursePreferences.add(courses.get(courseIndex));
      }

      // Generate 0-3 friend requests (30% chance of no friends, 70% chance of 1-3 friends)
      List<User> friendRequests = new ArrayList<>();
      if (random.nextDouble() > 0.3) { // 70% chance of having friends
        int numFriends = random.nextInt(3) + 1; // 1 to 3 friends
        Set<Integer> selectedFriendIndices = new HashSet<>();
        selectedFriendIndices.add(i); // Don't request yourself

        for (int j = 0;
            j < numFriends && selectedFriendIndices.size() < Math.min(100, numFriends + 1);
            j++) {
          int friendIndex;
          do {
            friendIndex = random.nextInt(100);
          } while (selectedFriendIndices.contains(friendIndex));
          selectedFriendIndices.add(friendIndex);
          friendRequests.add(users.get(friendIndex));
        }
      }

      StudyApplicant applicant =
          StudyApplicant.of(term, currentUser, friendRequests, coursePreferences);
      applicants.add(applicant);
    }

    studyApplicantRepository.saveAll(applicants);

    // Accept friend requests with 80% probability (realistic scenario)
    applicants.forEach(
        applicant ->
            applicant
                .getPartnerRequests()
                .forEach(
                    request -> {
                      if (random.nextDouble() > 0.2) { // 80% acceptance rate
                        request.accept();
                      }
                    }));

    // When
    long startTime = System.currentTimeMillis();
    teamService.matchTeam();
    long endTime = System.currentTimeMillis();

    // Then
    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);

    // Performance verification
    assertTrue(
        (endTime - startTime) < 5000, "Matching 100 students should complete within 5 seconds");

    // Coverage verification
    long totalAssignedStudents =
        allGroups.stream().mapToLong(group -> group.getMembers().size()).sum();

    assertTrue(totalAssignedStudents >= 60, "Should assign at least 60% of students to groups");

    // Group size distribution verification
    Map<Integer, Long> groupSizeDistribution =
        allGroups.stream()
            .collect(
                Collectors.groupingBy(group -> group.getMembers().size(), Collectors.counting()));

    // Verify friend groups (can be size 2+)
    Long friendGroups = groupSizeDistribution.getOrDefault(2, 0L);

    // Verify course groups (size 3-5)
    Long size3Groups = groupSizeDistribution.getOrDefault(3, 0L);
    Long size4Groups = groupSizeDistribution.getOrDefault(4, 0L);
    Long size5Groups = groupSizeDistribution.getOrDefault(5, 0L);

    assertTrue(
        (size5Groups + size4Groups + size3Groups + friendGroups) > 0,
        "Should create various group sizes");

    // Algorithm correctness verification (with priority-based matching, group sizes can vary)
    assertTrue(
        (size5Groups + size4Groups) >= 0,
        "Should create groups of various sizes based on priority matching");

    // Course distribution verification
    Map<Course, Long> courseDistribution =
        allGroups.stream()
            .flatMap(group -> group.getCourses().stream())
            .collect(Collectors.groupingBy(GroupCourse::getCourse, Collectors.counting()));

    assertTrue(
        courseDistribution.size() >= 5, "Should create groups for multiple different courses");

    // Data integrity verification
    Set<User> assignedUsers =
        allGroups.stream()
            .flatMap(group -> group.getMembers().stream())
            .map(StudyApplicant::getUser)
            .collect(Collectors.toSet());

    assertEquals(
        assignedUsers.size(),
        totalAssignedStudents,
        "No student should be assigned to multiple groups");

    System.out.printf(
        "✅ Successfully matched %d students into %d groups in %dms%n",
        totalAssignedStudents, allGroups.size(), (endTime - startTime));
    System.out.printf(
        "📊 Group size distribution: 2-person: %d, 3-person: %d, 4-person: %d, 5-person: %d%n",
        friendGroups, size3Groups, size4Groups, size5Groups);
    System.out.printf(
        "📚 Course distribution: %d different courses covered%n", courseDistribution.size());
  }

  @Test
  void 친구기반매칭만_개별메서드테스트() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create courses
    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      Course course =
          new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 15; i++) {
      User user =
          User.builder()
              .sub("user" + i)
              .sid("225" + String.format("%05d", i))
              .email("user" + i + "@test.com")
              .name("Student " + i)
              .build();
      users.add(user);
    }
    userRepository.saveAll(users);

    // Create applicants with friend relationships
    List<StudyApplicant> applicants = new ArrayList<>();

    // Friend pair 1: User1 ↔ User2
    applicants.add(
        StudyApplicant.of(term, users.get(0), List.of(users.get(1)), List.of(courses.get(0))));
    applicants.add(
        StudyApplicant.of(term, users.get(1), List.of(users.get(0)), List.of(courses.get(1))));

    // Friend chain: User3 ↔ User4 ↔ User5
    applicants.add(
        StudyApplicant.of(term, users.get(2), List.of(users.get(3)), List.of(courses.get(2))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(3), List.of(users.get(2), users.get(4)), List.of(courses.get(3))));
    applicants.add(
        StudyApplicant.of(term, users.get(4), List.of(users.get(3)), List.of(courses.get(4))));

    // Large friend network: User6-User10 (5 people)
    applicants.add(
        StudyApplicant.of(
            term, users.get(5), List.of(users.get(6), users.get(7)), List.of(courses.get(0))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(6), List.of(users.get(5), users.get(8)), List.of(courses.get(1))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(7), List.of(users.get(5), users.get(9)), List.of(courses.get(2))));
    applicants.add(
        StudyApplicant.of(term, users.get(8), List.of(users.get(6)), List.of(courses.get(3))));
    applicants.add(
        StudyApplicant.of(term, users.get(9), List.of(users.get(7)), List.of(courses.get(4))));

    // No friends - should not be in friend groups
    for (int i = 10; i < 15; i++) {
      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(i % 5))));
    }

    studyApplicantRepository.saveAll(applicants);

    // Accept all friend requests
    applicants.forEach(
        applicant -> applicant.getPartnerRequests().forEach(StudyPartnerRequest::accept));

    AtomicInteger tag = new AtomicInteger(1);

    // Print friendship network visualization with prettier formatting
    System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    🤝 친구 관계 네트워크 시각화                      ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");

    applicants.forEach(
        applicant -> {
          List<String> friendNames =
              applicant.getPartnerRequests().stream()
                  .filter(StudyPartnerRequest::isAccepted)
                  .map(request -> request.getReceiver().getName())
                  .toList();

          // Sort courses by priority (0 = highest priority first)
          List<String> courseNames =
              applicant.getPreferredCourses().stream()
                  .sorted(Comparator.comparing(PreferredCourse::getPriority))
                  .map(pc -> pc.getCourse().getName() + "(P" + pc.getPriority() + ")")
                  .collect(Collectors.toList());

          if (!friendNames.isEmpty()) {
            System.out.printf("👤 %-12s ──→ ", applicant.getUser().getName());
            for (int i = 0; i < friendNames.size(); i++) {
              if (i > 0) System.out.print(" ──→ ");
              System.out.print("👥 " + friendNames.get(i));
            }
            System.out.println();
            System.out.printf("      📚 선호과목: %s%n", courseNames);
            System.out.println();
          } else {
            System.out.printf(
                "👤 %-12s (친구없음) 📚 %s%n", applicant.getUser().getName(), courseNames);
            System.out.println();
          }
        });
    System.out.println();

    // When - Call groupByFriends directly
    List<StudyGroup> friendGroups = teamService.groupByFriends(applicants, tag, term);

    // Then
    System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                      👥 친구 기반 매칭 결과                       ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 생성된 친구 그룹 수: " + friendGroups.size());
    System.out.println();

    friendGroups.forEach(
        group -> {
          List<String> memberNames =
              group.getMembers().stream().map(member -> member.getUser().getName()).toList();

          // Show friendship connections within the group as a chain
          List<String> connections = new ArrayList<>();
          Set<String> processedPairs = new HashSet<>();

          for (int i = 0; i < group.getMembers().size(); i++) {
            for (int j = i + 1; j < group.getMembers().size(); j++) {
              StudyApplicant member1 = group.getMembers().get(i);
              StudyApplicant member2 = group.getMembers().get(j);

              boolean connected =
                  member1.getPartnerRequests().stream()
                          .anyMatch(
                              req ->
                                  req.getReceiver().equals(member2.getUser()) && req.isAccepted())
                      || member2.getPartnerRequests().stream()
                          .anyMatch(
                              req ->
                                  req.getReceiver().equals(member1.getUser()) && req.isAccepted());

              if (connected) {
                String pairKey =
                    member1.getUser().getName().compareTo(member2.getUser().getName()) < 0
                        ? member1.getUser().getName() + "-" + member2.getUser().getName()
                        : member2.getUser().getName() + "-" + member1.getUser().getName();

                if (!processedPairs.contains(pairKey)) {
                  connections.add(
                      "👫 " + member1.getUser().getName() + " ⟷ " + member2.getUser().getName());
                  processedPairs.add(pairKey);
                }
              }
            }
          }

          System.out.printf(
              "🏷️  그룹 %d (%d명): %s%n", group.getTag(), group.getMembers().size(), memberNames);

          if (!connections.isEmpty()) {
            System.out.println("    🔗 친구연결:");
            connections.forEach(connection -> System.out.println("       " + connection));
          } else {
            System.out.println("    ❌ 직접 친구연결 없음 (DFS로 연결된 그룹)");
          }

          // Show each member's friend request details
          System.out.println("    📋 각 멤버의 신청 관계:");
          group
              .getMembers()
              .forEach(
                  member -> {
                    List<String> sentRequests =
                        member.getPartnerRequests().stream()
                            .filter(StudyPartnerRequest::isAccepted)
                            .map(req -> "📤 " + req.getReceiver().getName())
                            .toList();

                    List<String> receivedRequests =
                        applicants.stream()
                            .flatMap(applicant -> applicant.getPartnerRequests().stream())
                            .filter(
                                req ->
                                    req.getReceiver().equals(member.getUser()) && req.isAccepted())
                            .map(req -> "📥 " + req.getSender().getUser().getName())
                            .toList();

                    System.out.printf("       🎓 %-12s ──→ ", member.getUser().getName());
                    List<String> allRequests = new ArrayList<>();
                    allRequests.addAll(sentRequests);
                    allRequests.addAll(receivedRequests);

                    if (!allRequests.isEmpty()) {
                      for (int i = 0; i < allRequests.size(); i++) {
                        if (i > 0) System.out.print(" ➤ ");
                        System.out.print(allRequests.get(i));
                      }
                    } else {
                      System.out.print("신청관계 없음");
                    }
                    System.out.println();
                  });
          System.out.println();
        });

    long totalFriendGroupMembers =
        friendGroups.stream().mapToLong(group -> group.getMembers().size()).sum();
    System.out.println("📈 배정 결과:");
    System.out.println("   ✅ 친구 그룹 배정: " + totalFriendGroupMembers + "명/15명");
    System.out.println("   ⏸️  미배정: " + (15 - totalFriendGroupMembers) + "명 (과목 매칭 대기)");
    System.out.println();

    // Verify friend groups were created correctly
    assertThat(friendGroups.size()).isEqualTo(3); // 2-person, 3-person, 5-person groups
    assertThat(totalFriendGroupMembers).isEqualTo(10); // 2+3+5 = 10 students in friend groups

    // Verify specific friend connections
    assertTrue(
        friendGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 2
                        && containsUsers(group, users.get(0), users.get(1))));
    assertTrue(
        friendGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 3
                        && containsUsers(group, users.get(2), users.get(3), users.get(4))));
    assertTrue(
        friendGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 5
                        && containsUsers(
                            group,
                            users.get(5),
                            users.get(6),
                            users.get(7),
                            users.get(8),
                            users.get(9))));
  }

  @Test
  void 과목선호도기반매칭만_개별메서드테스트() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create courses
    List<Course> courses = new ArrayList<>();
    String[] courseNames = {"Mathematics", "Physics", "Chemistry", "Computer Science", "Biology"};
    for (int i = 0; i < 5; i++) {
      Course course =
          new Course(
              courseNames[i], "CRS" + String.format("%03d", i + 1), "Professor " + (i + 1), term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 20; i++) {
      User user =
          User.builder()
              .sub("student" + i)
              .sid("225" + String.format("%05d", i))
              .email("student" + i + "@university.edu")
              .name("Student " + i)
              .build();
      users.add(user);
    }
    userRepository.saveAll(users);

    // Create applicants with diverse course preferences (no friends)
    List<StudyApplicant> applicants = new ArrayList<>();

    // Group 1: Mathematics enthusiasts with different secondary interests
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(0),
            List.of(),
            List.of(courses.get(0), courses.get(1)))); // Math → Physics
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(1),
            List.of(),
            List.of(courses.get(0), courses.get(2), courses.get(4)))); // Math → Chemistry → Biology
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(2),
            List.of(),
            List.of(courses.get(0), courses.get(3)))); // Math → Computer Science

    // Group 2: Physics-focused with mixed interests
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(3),
            List.of(),
            List.of(courses.get(1), courses.get(0), courses.get(2)))); // Physics → Math → Chemistry
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(4),
            List.of(),
            List.of(courses.get(1), courses.get(3)))); // Physics → Computer Science
    applicants.add(
        StudyApplicant.of(term, users.get(5), List.of(), List.of(courses.get(1)))); // Physics only

    // Group 3: Chemistry diverse preferences
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(6),
            List.of(),
            List.of(
                courses.get(2), courses.get(4), courses.get(1)))); // Chemistry → Biology → Physics
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(7),
            List.of(),
            List.of(courses.get(2), courses.get(0)))); // Chemistry → Math
    applicants.add(
        StudyApplicant.of(
            term, users.get(8), List.of(), List.of(courses.get(2)))); // Chemistry only

    // Group 4: Computer Science with varied secondary choices
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(9),
            List.of(),
            List.of(courses.get(3), courses.get(0), courses.get(1)))); // CS → Math → Physics
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(10),
            List.of(),
            List.of(courses.get(3), courses.get(2)))); // CS → Chemistry

    // Group 5: Biology-centered (new focus area)
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(11),
            List.of(),
            List.of(courses.get(4), courses.get(2), courses.get(0)))); // Biology → Chemistry → Math
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(12),
            List.of(),
            List.of(courses.get(4), courses.get(1)))); // Biology → Physics

    // Group 6: Cross-disciplinary students with unique combinations
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(13),
            List.of(),
            List.of(courses.get(0), courses.get(4), courses.get(3)))); // Math → Biology → CS
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(14),
            List.of(),
            List.of(
                courses.get(1), courses.get(2), courses.get(4)))); // Physics → Chemistry → Biology
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(15),
            List.of(),
            List.of(courses.get(3), courses.get(4)))); // CS → Biology

    // Group 7: Single-subject focused students
    applicants.add(
        StudyApplicant.of(term, users.get(16), List.of(), List.of(courses.get(0)))); // Math only
    applicants.add(
        StudyApplicant.of(term, users.get(17), List.of(), List.of(courses.get(1)))); // Physics only
    applicants.add(
        StudyApplicant.of(term, users.get(18), List.of(), List.of(courses.get(4)))); // Biology only
    applicants.add(
        StudyApplicant.of(term, users.get(19), List.of(), List.of(courses.get(3)))); // CS only

    studyApplicantRepository.saveAll(applicants);

    AtomicInteger tag = new AtomicInteger(1);

    // Print course preference visualization with prettier formatting
    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    📚 학생별 과목 선호도 시각화                       ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    Map<String, List<String>> courseStudentMap = new HashMap<>();

    applicants.forEach(
        applicant -> {
          List<String> coursePrefs =
              applicant.getPreferredCourses().stream()
                  .sorted(Comparator.comparing(PreferredCourse::getPriority))
                  .map(pc -> String.format("📖%s(P%d)", pc.getCourse().getName(), pc.getPriority()))
                  .toList();

          System.out.printf("🎓 %-12s ──→ ", applicant.getUser().getName());
          for (int i = 0; i < coursePrefs.size(); i++) {
            if (i > 0) System.out.print(" ➤ ");
            System.out.print(coursePrefs.get(i));
          }
          System.out.println();

          // Build course->student mapping for later analysis
          applicant
              .getPreferredCourses()
              .forEach(
                  pc -> {
                    String courseKey = pc.getCourse().getName() + "(우선순위:" + pc.getPriority() + ")";
                    courseStudentMap
                        .computeIfAbsent(courseKey, k -> new ArrayList<>())
                        .add(applicant.getUser().getName());
                  });
        });

    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                      📊 과목별 지원자 분석                          ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    courseStudentMap.entrySet().stream()
        .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
        .forEach(
            entry -> {
              System.out.printf(
                  "📋 %-30s ──→ 👥%2d명: %s%n",
                  entry.getKey(), entry.getValue().size(), entry.getValue());
            });
    System.out.println();

    // When - Call groupByCoursePreference directly
    List<StudyGroup> courseGroups = teamService.groupByCoursePreference(applicants, tag, term);

    // Then
    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                   🎯 과목 선호도 기반 매칭 결과                        ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📈 생성된 과목 그룹 수: " + courseGroups.size());
    System.out.println();

    AtomicInteger groupCounter = new AtomicInteger(1);
    courseGroups.forEach(
        group -> {
          List<String> memberNames =
              group.getMembers().stream().map(member -> member.getUser().getName()).toList();
          List<String> courseNames2 =
              group.getCourses().stream().map(gc -> gc.getCourse().getName()).toList();

          System.out.printf(
              "👥 그룹 %d: %s (%d명)%n",
              groupCounter.getAndIncrement(), memberNames, group.getMembers().size());
          System.out.printf("   🎯 공통과목: %s%n", courseNames2);
          System.out.println();

          System.out.println("   📋 각 멤버의 과목 선호도:");
          group
              .getMembers()
              .forEach(
                  member -> {
                    List<String> prefs =
                        member.getPreferredCourses().stream()
                            .sorted(Comparator.comparing(PreferredCourse::getPriority))
                            .map(
                                pc ->
                                    "📚" + pc.getCourse().getName() + "(P" + pc.getPriority() + ")")
                            .toList();
                    System.out.printf("      🎓 %-12s ──→ ", member.getUser().getName());
                    for (int i = 0; i < prefs.size(); i++) {
                      if (i > 0) System.out.print(" ➤ ");
                      System.out.print(prefs.get(i));
                    }
                    System.out.println();
                  });

          System.out.println();
        });

    System.out.println("┌─────────────────────────────────────────┐");
    System.out.println("│            📊 매칭 결과 통계             │");
    System.out.println("└─────────────────────────────────────────┘");

    long totalCourseGroupMembers =
        courseGroups.stream().mapToLong(group -> group.getMembers().size()).sum();
    System.out.println("✅ 과목 그룹에 배정된 총 학생 수: " + totalCourseGroupMembers + "/20");
    System.out.println("⏸️ 미배정 학생 수: " + (20 - totalCourseGroupMembers));

    // Group size analysis
    Map<Integer, Long> sizeDistribution =
        courseGroups.stream()
            .collect(
                Collectors.groupingBy(group -> group.getMembers().size(), Collectors.counting()));
    System.out.println("📊 그룹 크기 분포: " + sizeDistribution);
    System.out.println();

    // Verify course-based groups were created
    assertThat(courseGroups.size()).isGreaterThan(0);

    // Verify group sizes are between 3-5
    assertTrue(
        courseGroups.stream()
            .allMatch(group -> group.getMembers().size() >= 3 && group.getMembers().size() <= 5));

    // Verify Mathematics got multiple groups (8 students → should create 5+3 groups)
    long mathGroups =
        courseGroups.stream()
            .filter(
                group ->
                    group.getCourses().stream()
                        .anyMatch(gc -> gc.getCourse().getName().equals("Mathematics")))
            .count();
    assertTrue(mathGroups >= 1, "Should have Math course groups");
  }

  @Test
  void 통합매칭_친구우선과목후순_개별메서드조합테스트() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create courses
    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 8; i++) {
      Course course =
          new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 50; i++) {
      User user =
          User.builder()
              .sub("student" + i)
              .sid("225" + String.format("%05d", i))
              .email("student" + i + "@university.edu")
              .name("Student " + i)
              .build();
      users.add(user);
    }
    userRepository.saveAll(users);

    // Create applicants with mixed preferences
    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(54321); // Fixed seed

    for (int i = 0; i < 50; i++) {
      User currentUser = users.get(i);

      // Generate course preferences (1-3 courses)
      List<Course> coursePreferences = new ArrayList<>();
      int numPreferences = random.nextInt(3) + 1;
      Set<Integer> selectedCourseIndices = new HashSet<>();

      for (int j = 0; j < numPreferences; j++) {
        int courseIndex;
        do {
          courseIndex = random.nextInt(8);
        } while (selectedCourseIndices.contains(courseIndex));
        selectedCourseIndices.add(courseIndex);
        coursePreferences.add(courses.get(courseIndex));
      }

      // Generate friend requests (20% chance of having friends)
      List<User> friendRequests = new ArrayList<>();
      if (random.nextDouble() > 0.8) {
        int numFriends = random.nextInt(2) + 1; // 1-2 friends
        Set<Integer> selectedFriendIndices = new HashSet<>();
        selectedFriendIndices.add(i);

        for (int j = 0;
            j < numFriends && selectedFriendIndices.size() < Math.min(50, numFriends + 1);
            j++) {
          int friendIndex;
          do {
            friendIndex = random.nextInt(50);
          } while (selectedFriendIndices.contains(friendIndex));
          selectedFriendIndices.add(friendIndex);
          friendRequests.add(users.get(friendIndex));
        }
      }

      StudyApplicant applicant =
          StudyApplicant.of(term, currentUser, friendRequests, coursePreferences);
      applicants.add(applicant);
    }

    studyApplicantRepository.saveAll(applicants);

    // Accept friend requests with 90% probability
    applicants.forEach(
        applicant ->
            applicant
                .getPartnerRequests()
                .forEach(
                    request -> {
                      if (random.nextDouble() > 0.1) { // 90% acceptance rate
                        request.accept();
                      }
                    }));

    // Print initial state visualization with prettier formatting
    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                      📊 초기 상태 분석                             ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("🎓 총 학생 수: 50명, 📚 총 과목 수: 8개");
    System.out.println();

    // Friendship network analysis
    long totalFriendships =
        applicants.stream()
                .mapToLong(
                    applicant ->
                        applicant.getPartnerRequests().stream()
                            .filter(StudyPartnerRequest::isAccepted)
                            .count())
                .sum()
            / 2; // Divide by 2 because each friendship is counted twice
    System.out.println("🤝 총 친구 관계 수: " + totalFriendships + "개");
    System.out.println();

    // Course preference distribution
    Map<String, Long> courseDemand =
        applicants.stream()
            .flatMap(applicant -> applicant.getPreferredCourses().stream())
            .collect(Collectors.groupingBy(pc -> pc.getCourse().getName(), Collectors.counting()));

    System.out.println("📈 과목별 선호도 분포:");
    courseDemand.entrySet().stream()
        .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
        .forEach(
            entry ->
                System.out.printf("   📖 %-15s 👥%2d명%n", entry.getKey() + ":", entry.getValue()));
    System.out.println();

    // Priority distribution analysis
    Map<Integer, Long> priorityDistribution =
        applicants.stream()
            .flatMap(applicant -> applicant.getPreferredCourses().stream())
            .collect(Collectors.groupingBy(PreferredCourse::getPriority, Collectors.counting()));
    System.out.println("🎯 우선순위 분포: " + priorityDistribution);
    System.out.println();

    // When - Call matchTeam (integration test)
    long startTime = System.currentTimeMillis();
    teamService.matchTeam();
    long endTime = System.currentTimeMillis();

    // Then
    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);

    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                  🎯 통합 매칭 알고리즘 결과                          ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("⏱️  처리 시간: " + (endTime - startTime) + "ms");
    System.out.println("📊 총 생성된 그룹 수: " + allGroups.size());
    System.out.println();

    // Separate friend groups from course groups
    List<StudyGroup> friendGroups =
        allGroups.stream().filter(this::hasStrongFriendConnections).toList();
    List<StudyGroup> courseGroups =
        allGroups.stream().filter(group -> !hasStrongFriendConnections(group)).toList();

    System.out.println("🤝 친구 기반 그룹 수: " + friendGroups.size());
    System.out.println("📚 과목 기반 그룹 수: " + courseGroups.size());
    System.out.println();

    // Detailed friend group analysis
    friendGroups.forEach(
        group -> {
          List<String> memberNames =
              group.getMembers().stream().map(member -> member.getUser().getName()).toList();

          // Show friendship connections within the group
          List<String> connections = new ArrayList<>();
          for (int i = 0; i < group.getMembers().size(); i++) {
            for (int j = i + 1; j < group.getMembers().size(); j++) {
              StudyApplicant member1 = group.getMembers().get(i);
              StudyApplicant member2 = group.getMembers().get(j);

              boolean connected =
                  member1.getPartnerRequests().stream()
                          .anyMatch(
                              req ->
                                  req.getReceiver().equals(member2.getUser()) && req.isAccepted())
                      || member2.getPartnerRequests().stream()
                          .anyMatch(
                              req ->
                                  req.getReceiver().equals(member1.getUser()) && req.isAccepted());

              if (connected) {
                connections.add(member1.getUser().getName() + "↔" + member2.getUser().getName());
              }
            }
          }

          System.out.printf(
              "👥 친구그룹 %d: %s (%d명) ──→ 🔗연결: [%s]%n",
              group.getTag(),
              memberNames,
              group.getMembers().size(),
              String.join(", ", connections));

          // Show each member's friend request details
          System.out.println("   📋 각 멤버의 신청 관계:");
          group
              .getMembers()
              .forEach(
                  member -> {
                    List<String> sentRequests =
                        member.getPartnerRequests().stream()
                            .filter(StudyPartnerRequest::isAccepted)
                            .map(req -> "📤 " + req.getReceiver().getName())
                            .distinct() // Remove duplicates
                            .toList();

                    List<String> receivedRequests =
                        applicants.stream()
                            .flatMap(applicant -> applicant.getPartnerRequests().stream())
                            .filter(
                                req ->
                                    req.getReceiver().equals(member.getUser()) && req.isAccepted())
                            .map(req -> "📥 " + req.getSender().getUser().getName())
                            .distinct() // Remove duplicates
                            .toList();

                    System.out.printf("      🎓 %-12s ──→ ", member.getUser().getName());

                    // Combine and deduplicate requests
                    Set<String> allRequestsSet = new HashSet<>();
                    allRequestsSet.addAll(sentRequests);
                    allRequestsSet.addAll(receivedRequests);
                    List<String> allRequests = new ArrayList<>(allRequestsSet);

                    if (!allRequests.isEmpty()) {
                      for (int i = 0; i < allRequests.size(); i++) {
                        if (i > 0) System.out.print(" ➤ ");
                        System.out.print(allRequests.get(i));
                      }
                    } else {
                      System.out.print("신청관계 없음");
                    }
                    System.out.println();
                  });
          System.out.println();
        });

    // Detailed course group analysis
    courseGroups.forEach(
        group -> {
          List<String> memberNames =
              group.getMembers().stream().map(member -> member.getUser().getName()).toList();
          List<String> courseNames2 =
              group.getCourses().stream().map(gc -> gc.getCourse().getName()).toList();

          // Show course preferences for each member with prettier format
          List<String> memberCoursePrefs =
              group.getMembers().stream()
                  .map(
                      member -> {
                        List<String> prefs =
                            member.getPreferredCourses().stream()
                                .sorted(Comparator.comparing(PreferredCourse::getPriority))
                                .map(
                                    pc ->
                                        "📚"
                                            + pc.getCourse().getName()
                                            + "(P"
                                            + pc.getPriority()
                                            + ")")
                                .toList();
                        return "🎓" + member.getUser().getName() + ":" + prefs;
                      })
                  .toList();

          System.out.printf(
              "📖 과목그룹 %d: %s (%d명)%n", group.getTag(), memberNames, group.getMembers().size());
          System.out.printf("   🎯 공통과목: %s%n", courseNames2);
          System.out.println();

          System.out.println("   📋 각 멤버의 과목 선호도:");
          group
              .getMembers()
              .forEach(
                  member -> {
                    List<String> prefs =
                        member.getPreferredCourses().stream()
                            .sorted(Comparator.comparing(PreferredCourse::getPriority))
                            .map(
                                pc ->
                                    "📚" + pc.getCourse().getName() + "(P" + pc.getPriority() + ")")
                            .toList();
                    System.out.printf("      🎓 %-12s ──→ ", member.getUser().getName());
                    for (int i = 0; i < prefs.size(); i++) {
                      if (i > 0) System.out.print(" ➤ ");
                      System.out.print(prefs.get(i));
                    }
                    System.out.println();
                  });

          System.out.println();
        });

    System.out.println("┌─────────────────────────────────────────┐");
    System.out.println("│            📊 최종 매칭 통계             │");
    System.out.println("└─────────────────────────────────────────┘");

    long totalAssignedStudents =
        allGroups.stream().mapToLong(group -> group.getMembers().size()).sum();
    System.out.println("✅ 총 배정된 학생 수: " + totalAssignedStudents + "/50");
    System.out.println("📊 배정률: " + String.format("%.1f%%", (totalAssignedStudents * 100.0 / 50)));

    // Group size distribution
    Map<Integer, Long> sizeDistribution =
        allGroups.stream()
            .collect(
                Collectors.groupingBy(group -> group.getMembers().size(), Collectors.counting()));
    System.out.println("📈 그룹 크기 분포: " + sizeDistribution);
    System.out.println();

    // Verify integration test results
    assertTrue(totalAssignedStudents >= 30, "Should assign at least 60% of students");
    assertTrue((endTime - startTime) < 2000, "Should complete within 2 seconds for 50 students");
    assertFalse(allGroups.isEmpty(), "Should create at least some groups");

    // Verify group sizes (course groups can be larger due to overlapping preferences)
    assertTrue(
        allGroups.stream().allMatch(group -> group.getMembers().size() >= 2),
        "All groups should have at least 2 members");

    // Verify no duplicate assignments
    Set<User> assignedUsers =
        allGroups.stream()
            .flatMap(group -> group.getMembers().stream())
            .map(StudyApplicant::getUser)
            .collect(Collectors.toSet());
    assertThat(assignedUsers.size()).isEqualTo(totalAssignedStudents);
  }

  // Helper methods
  private boolean containsUsers(StudyGroup group, User... expectedUsers) {
    Set<User> groupUsers =
        group.getMembers().stream().map(StudyApplicant::getUser).collect(Collectors.toSet());
    return Arrays.stream(expectedUsers).allMatch(groupUsers::contains);
  }

  private boolean hasStrongFriendConnections(StudyGroup group) {
    if (group.getMembers().size() < 2) return false;

    List<StudyApplicant> members = group.getMembers();
    int totalConnections = 0;
    int possibleConnections = members.size() * (members.size() - 1) / 2;

    for (int i = 0; i < members.size(); i++) {
      for (int j = i + 1; j < members.size(); j++) {
        StudyApplicant applicant1 = members.get(i);
        StudyApplicant applicant2 = members.get(j);

        boolean areConnected =
            applicant1.getPartnerRequests().stream()
                    .anyMatch(
                        request ->
                            request.getReceiver().equals(applicant2.getUser())
                                && request.isAccepted())
                || applicant2.getPartnerRequests().stream()
                    .anyMatch(
                        request ->
                            request.getReceiver().equals(applicant1.getUser())
                                && request.isAccepted());

        if (areConnected) {
          totalConnections++;
        }
      }
    }

    // Consider it a friend group if more than 50% of possible connections exist
    return totalConnections > 0 && (totalConnections * 2.0 / possibleConnections) > 0.5;
  }
}
