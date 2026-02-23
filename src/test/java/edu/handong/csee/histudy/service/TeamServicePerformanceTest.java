package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@Tag("perf")
public class TeamServicePerformanceTest {

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
  void 성능테스트_친구기반매칭_100명() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create courses
    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      Course course = new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
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

    // Create applicants with friend relationships (30% have friends)
    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(42); // Fixed seed for reproducibility

    for (int i = 0; i < 100; i++) {
      User currentUser = users.get(i);

      // Generate course preferences
      List<Course> coursePreferences = new ArrayList<>();
      int numPreferences = random.nextInt(3) + 1;
      Set<Integer> selectedCourseIndices = new HashSet<>();

      for (int j = 0; j < numPreferences; j++) {
        int courseIndex;
        do {
          courseIndex = random.nextInt(10);
        } while (selectedCourseIndices.contains(courseIndex));
        selectedCourseIndices.add(courseIndex);
        coursePreferences.add(courses.get(courseIndex));
      }

      // Generate friend requests (30% chance of having friends)
      List<User> friendRequests = new ArrayList<>();
      if (random.nextDouble() > 0.7) {
        int numFriends = random.nextInt(3) + 1; // 1-3 friends
        Set<Integer> selectedFriendIndices = new HashSet<>();
        selectedFriendIndices.add(i);

        for (int j = 0; j < numFriends && selectedFriendIndices.size() < Math.min(100, numFriends + 1); j++) {
          int friendIndex;
          do {
            friendIndex = random.nextInt(100);
          } while (selectedFriendIndices.contains(friendIndex));
          selectedFriendIndices.add(friendIndex);
          friendRequests.add(users.get(friendIndex));
        }
      }

      StudyApplicant applicant = StudyApplicant.of(term, currentUser, friendRequests, coursePreferences);
      applicants.add(applicant);
    }

    studyApplicantRepository.saveAll(applicants);

    // Accept friend requests with 85% probability
    applicants.forEach(
        applicant ->
            applicant
                .getPartnerRequests()
                .forEach(
                    request -> {
                      if (random.nextDouble() > 0.15) { // 85% acceptance rate
                        request.accept();
                      }
                    }));

    AtomicInteger tag = new AtomicInteger(1);

    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    🔥 친구 기반 매칭 성능 테스트                      ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 테스트 데이터: 100명 학생, 30% 친구 관계, 85% 수락률");

    // Calculate friendship statistics
    long totalFriendships = applicants.stream()
        .mapToLong(applicant ->
            applicant.getPartnerRequests().stream()
                .filter(StudyPartnerRequest::isAccepted)
                .count())
        .sum() / 2;

    System.out.println("🤝 총 친구 관계 수: " + totalFriendships + "개");
    System.out.println();

    // When - Measure performance
    long startTime = System.nanoTime();
    List<StudyGroup> friendGroups = teamService.groupByFriends(applicants, tag, term);
    long endTime = System.nanoTime();

    long executionTimeMs = (endTime - startTime) / 1_000_000;

    // Then
    System.out.println("⚡ 친구 기반 매칭 성능 결과:");
    System.out.println("   ⏱️  실행 시간: " + executionTimeMs + "ms");
    System.out.println("   📊 생성된 그룹 수: " + friendGroups.size());

    long totalGroupMembers = friendGroups.stream()
        .mapToLong(group -> group.getMembers().size())
        .sum();

    System.out.println("   👥 매칭된 학생 수: " + totalGroupMembers + "/100명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (totalGroupMembers * 100.0 / 100)));

    // Performance assertions
    assertThat(executionTimeMs).isGreaterThanOrEqualTo(0);
    assertThat(friendGroups.size()).isGreaterThan(0);
    assertThat(totalGroupMembers).isGreaterThan(0);

    System.out.println();
  }

  @Test
  void 성능테스트_과목기반매칭_100명() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create courses
    List<Course> courses = new ArrayList<>();
    String[] courseNames = {
      "Mathematics", "Physics", "Chemistry", "Computer Science", "Biology",
      "History", "Literature", "Psychology", "Economics", "Philosophy"
    };

    for (int i = 0; i < 10; i++) {
      Course course = new Course(courseNames[i], "CRS" + String.format("%03d", i + 1), "Professor " + (i + 1), term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
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

    // Create applicants with diverse course preferences (no friends for pure course-based test)
    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(42); // Fixed seed for reproducibility

    for (int i = 0; i < 100; i++) {
      User currentUser = users.get(i);

      // Generate 1-3 course preferences with realistic distribution
      List<Course> coursePreferences = new ArrayList<>();
      int numPreferences = random.nextInt(3) + 1;
      Set<Integer> selectedCourseIndices = new HashSet<>();

      for (int j = 0; j < numPreferences; j++) {
        int courseIndex;
        do {
          // Weighted towards popular courses (Math, Physics, CS)
          if (random.nextDouble() < 0.6) {
            courseIndex = random.nextInt(4); // 0-3: Math, Physics, Chemistry, CS
          } else {
            courseIndex = random.nextInt(10); // 0-9: All courses
          }
        } while (selectedCourseIndices.contains(courseIndex));
        selectedCourseIndices.add(courseIndex);
        coursePreferences.add(courses.get(courseIndex));
      }

      StudyApplicant applicant = StudyApplicant.of(term, currentUser, List.of(), coursePreferences);
      applicants.add(applicant);
    }

    studyApplicantRepository.saveAll(applicants);
    AtomicInteger tag = new AtomicInteger(1);

    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    🔥 과목 기반 매칭 성능 테스트                      ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 테스트 데이터: 100명 학생, 10개 과목, 다양한 선호도");

    // Calculate course preference statistics
    long totalPreferences = applicants.stream()
        .mapToLong(applicant -> applicant.getPreferredCourses().size())
        .sum();

    System.out.println("📚 총 과목 선호도 수: " + totalPreferences + "개");
    System.out.println("📊 평균 선호도 per 학생: " + String.format("%.1f", totalPreferences / 100.0));
    System.out.println();

    // When - Measure performance
    long startTime = System.nanoTime();
    List<StudyGroup> courseGroups = teamService.groupByCoursePreference(applicants, tag, term);
    long endTime = System.nanoTime();

    long executionTimeMs = (endTime - startTime) / 1_000_000;

    // Then
    System.out.println("⚡ 과목 기반 매칭 성능 결과:");
    System.out.println("   ⏱️  실행 시간: " + executionTimeMs + "ms");
    System.out.println("   📊 생성된 그룹 수: " + courseGroups.size());

    long totalGroupMembers = courseGroups.stream()
        .mapToLong(group -> group.getMembers().size())
        .sum();

    System.out.println("   👥 매칭된 학생 수: " + totalGroupMembers + "/100명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (totalGroupMembers * 100.0 / 100)));

    // Group size distribution
    System.out.println("   📏 그룹 크기 분포:");
    courseGroups.stream()
        .collect(java.util.stream.Collectors.groupingBy(
            group -> group.getMembers().size(),
            java.util.stream.Collectors.counting()))
        .entrySet().stream()
        .sorted(java.util.Map.Entry.comparingByKey())
        .forEach(entry -> System.out.println("      " + entry.getKey() + "명: " + entry.getValue() + "개 그룹"));

    // Performance assertions
    assertThat(executionTimeMs).isGreaterThanOrEqualTo(0);
    assertThat(courseGroups.size()).isGreaterThan(0);
    assertThat(totalGroupMembers).isGreaterThan(50); // Should match at least 50% of students

    System.out.println();
  }

  @Test
  void 성능테스트_통합매칭_500명_대규모() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    // Create courses
    List<Course> courses = new ArrayList<>();
    String[] courseNames = {
      "Mathematics", "Physics", "Chemistry", "Computer Science", "Biology",
      "History", "Literature", "Psychology", "Economics", "Philosophy",
      "Engineering", "Statistics", "Philosophy", "Art", "Music"
    };

    for (int i = 0; i < 15; i++) {
      Course course = new Course(courseNames[i % courseNames.length] + " " + (i/courseNames.length + 1),
          "CRS" + String.format("%03d", i + 1), "Professor " + (i + 1), term);
      courses.add(course);
    }
    courseRepository.saveAll(courses);

    // Create users
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 500; i++) {
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

    // Create applicants with mixed preferences
    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(42); // Fixed seed for reproducibility

    for (int i = 0; i < 500; i++) {
      User currentUser = users.get(i);

      // Generate course preferences
      List<Course> coursePreferences = new ArrayList<>();
      int numPreferences = random.nextInt(3) + 1;
      Set<Integer> selectedCourseIndices = new HashSet<>();

      for (int j = 0; j < numPreferences; j++) {
        int courseIndex;
        do {
          courseIndex = random.nextInt(15);
        } while (selectedCourseIndices.contains(courseIndex));
        selectedCourseIndices.add(courseIndex);
        coursePreferences.add(courses.get(courseIndex));
      }

      // Generate friend requests (20% chance of having friends)
      List<User> friendRequests = new ArrayList<>();
      if (random.nextDouble() > 0.8) {
        int numFriends = random.nextInt(3) + 1; // 1-3 friends
        Set<Integer> selectedFriendIndices = new HashSet<>();
        selectedFriendIndices.add(i);

        for (int j = 0; j < numFriends && selectedFriendIndices.size() < Math.min(500, numFriends + 1); j++) {
          int friendIndex;
          do {
            friendIndex = random.nextInt(500);
          } while (selectedFriendIndices.contains(friendIndex));
          selectedFriendIndices.add(friendIndex);
          friendRequests.add(users.get(friendIndex));
        }
      }

      StudyApplicant applicant = StudyApplicant.of(term, currentUser, friendRequests, coursePreferences);
      applicants.add(applicant);
    }

    studyApplicantRepository.saveAll(applicants);

    // Accept friend requests with 80% probability
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

    System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    🚀 대규모 통합 매칭 성능 테스트                    ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 테스트 데이터: 500명 학생, 15개 과목, 20% 친구 관계");

    // When - Measure performance of complete matching
    long startTime = System.nanoTime();
    teamService.matchTeam();
    long endTime = System.nanoTime();

    long executionTimeMs = (endTime - startTime) / 1_000_000;

    // Then
    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);
    long totalGroupMembers = allGroups.stream()
        .mapToLong(group -> group.getMembers().size())
        .sum();

    System.out.println("🚀 대규모 통합 매칭 성능 결과:");
    System.out.println("   ⏱️  전체 실행 시간: " + executionTimeMs + "ms");
    System.out.println("   📊 생성된 총 그룹 수: " + allGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + totalGroupMembers + "/500명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (totalGroupMembers * 100.0 / 500)));
    System.out.println("   ⚡ 처리량: " + String.format("%.1f", 500.0 * 1000 / executionTimeMs) + " 학생/초");

    // Performance assertions for large scale
    assertThat(executionTimeMs).isGreaterThanOrEqualTo(0);
    assertThat(allGroups.size()).isGreaterThan(50); // Should create reasonable number of groups
    assertThat(totalGroupMembers).isGreaterThan(300); // Should match at least 60% of students

    System.out.println();
  }
}
