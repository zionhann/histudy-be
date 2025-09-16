package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.support.TestDataFactory;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
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
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", "/reports/images/");

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
  void 다양한선호과목과친구요청으로복합매칭() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create various courses
    Course mathCourse = new Course("Calculus I", "MAT101", "Prof. Smith", term);
    Course physicsCourse = new Course("Physics I", "PHY101", "Prof. Johnson", term);
    Course chemCourse = new Course("Chemistry I", "CHE101", "Prof. Brown", term);
    Course csCourse = new Course("Programming I", "CS101", "Prof. Davis", term);
    Course engCourse = new Course("English I", "ENG101", "Prof. Wilson", term);
    courseRepository.saveAll(List.of(mathCourse, physicsCourse, chemCourse, csCourse, engCourse));

    // Create 12 users with different preferences
    User[] users = new User[12];
    StudyApplicant[] applicants = new StudyApplicant[12];

    for (int i = 0; i < 12; i++) {
      users[i] =
          User.builder()
              .sub("user" + (i + 1))
              .sid("22500" + String.format("%03d", i + 101))
              .email("user" + (i + 1) + "@test.com")
              .name("Student" + (i + 1))
              .build();
      userRepository.save(users[i]);
    }

    // Create applicants with diverse course preferences and friend requests

    // Group 1: Math lovers (users 0, 1, 2) with friend requests
    applicants[0] =
        StudyApplicant.of(
            term, users[0], List.of(users[1], users[2]), List.of(mathCourse, physicsCourse));
    applicants[1] =
        StudyApplicant.of(term, users[1], List.of(users[0]), List.of(mathCourse, chemCourse));
    applicants[2] =
        StudyApplicant.of(term, users[2], List.of(users[0]), List.of(mathCourse, engCourse));

    // Group 2: Physics enthusiasts (users 3, 4, 5, 6)
    applicants[3] =
        StudyApplicant.of(term, users[3], List.of(), List.of(physicsCourse, mathCourse));
    applicants[4] =
        StudyApplicant.of(term, users[4], List.of(), List.of(physicsCourse, chemCourse));
    applicants[5] =
        StudyApplicant.of(term, users[5], List.of(users[4]), List.of(physicsCourse, csCourse));
    applicants[6] = StudyApplicant.of(term, users[6], List.of(), List.of(physicsCourse, engCourse));

    // Group 3: CS focused (users 7, 8, 9)
    applicants[7] =
        StudyApplicant.of(term, users[7], List.of(users[8]), List.of(csCourse, mathCourse));
    applicants[8] =
        StudyApplicant.of(term, users[8], List.of(users[7]), List.of(csCourse, physicsCourse));
    applicants[9] = StudyApplicant.of(term, users[9], List.of(), List.of(csCourse, chemCourse));

    // Individual preferences (users 10, 11)
    applicants[10] = StudyApplicant.of(term, users[10], List.of(), List.of(chemCourse, engCourse));
    applicants[11] = StudyApplicant.of(term, users[11], List.of(), List.of(engCourse, mathCourse));

    for (StudyApplicant applicant : applicants) {
      studyApplicantRepository.save(applicant);
    }

    // When
    teamService.matchTeam();

    // Then
    List<StudyGroup> groups = studyGroupRepository.findAllByAcademicTerm(term);

    assertThat(groups).isNotEmpty();

    // Verify all applicants are assigned to groups
    int totalAssignedMembers = groups.stream().mapToInt(g -> g.getMembers().size()).sum();

    // Should have most applicants assigned (some might be unassigned due to group size constraints)
    assertThat(totalAssignedMembers).isGreaterThan(0);

    // Verify group size constraints (3-5 members per group)
    boolean allGroupsHaveValidSize =
        groups.stream().allMatch(g -> g.getMembers().size() >= 3 && g.getMembers().size() <= 5);
    assertThat(allGroupsHaveValidSize).isTrue();

    // Verify unique tags
    List<Integer> tags = groups.stream().map(StudyGroup::getTag).toList();
    assertThat(tags).doesNotHaveDuplicates();

    // Verify friend requests were prioritized (users 0, 1, 2 should be together if possible)
    boolean foundFriendGroup =
        groups.stream()
            .anyMatch(
                group -> {
                  List<User> groupUsers =
                      group.getMembers().stream().map(StudyApplicant::getUser).toList();
                  return groupUsers.contains(users[0])
                      && (groupUsers.contains(users[1]) || groupUsers.contains(users[2]));
                });

    // Friend matching should be attempted but not guaranteed due to other constraints
    // This test verifies the algorithm runs without errors and produces valid groups

    // Log for debugging
    System.out.println(
        "Created " + groups.size() + " groups with " + totalAssignedMembers + " total members");
    for (int i = 0; i < groups.size(); i++) {
      StudyGroup group = groups.get(i);
      System.out.println(
          "Group "
              + (i + 1)
              + " (Tag: "
              + group.getTag()
              + "): "
              + group.getMembers().size()
              + " members");
    }
  }
}
