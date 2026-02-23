package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("perf")
public class TeamServiceAlgorithmComparisonTest {

  private AcademicTermRepository academicTermRepository;
  private StudyGroupRepository studyGroupRepository;
  private UserRepository userRepository;
  private StudyApplicantRepository studyApplicantRepository;
  private StudyReportRepository studyReportRepository;
  private CourseRepository courseRepository;
  private ImagePathMapper imagePathMapper;

  private TeamService currentTeamService;
  private LegacyTeamService legacyTeamService;

  @BeforeEach
  void setUp() {
    academicTermRepository = new FakeAcademicTermRepository();
    studyGroupRepository = new FakeStudyGroupRepository();
    userRepository = new FakeUserRepository();
    studyApplicantRepository = new FakeStudyApplicationRepository();
    studyReportRepository = new FakeStudyReportRepository();
    courseRepository = new FakeCourseRepository();
    imagePathMapper = new ImagePathMapper();

    ReflectionTestUtils.setField(imagePathMapper, "origin", "http://localhost:8080");
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", "/reports/images/");

    currentTeamService =
        new TeamService(
            studyGroupRepository,
            userRepository,
            academicTermRepository,
            studyApplicantRepository,
            studyReportRepository,
            imagePathMapper);

    legacyTeamService =
        new LegacyTeamService(
            studyGroupRepository,
            userRepository,
            academicTermRepository,
            studyApplicantRepository,
            studyReportRepository,
            imagePathMapper);
  }

  @Test
  void 알고리즘성능비교_친구기반매칭_100명() {
    // Setup test data
    AcademicTerm term = createTestTerm();
    List<StudyApplicant> applicants = createTestApplicantsWithFriends(100, term);
    AtomicInteger currentTag = new AtomicInteger(1);
    AtomicInteger legacyTag = new AtomicInteger(1);

    System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                   🔄 친구 기반 매칭 알고리즘 성능 비교                   ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 테스트 데이터: 100명 학생, 30% 친구 관계");

    // Test current implementation
    long currentStart = System.currentTimeMillis();
    List<StudyGroup> currentGroups =
        currentTeamService.groupByFriends(applicants, currentTag, term);
    long currentEnd = System.currentTimeMillis();

    // Reset applicants state for fair comparison
    resetApplicantsState(applicants);

    // Test legacy implementation
    long legacyStart = System.currentTimeMillis();
    Set<StudyGroup> legacyGroups = legacyTeamService.matchFriendFirst(applicants, legacyTag, term);
    long legacyEnd = System.currentTimeMillis();

    // Calculate statistics
    int currentMatched = currentGroups.stream().mapToInt(g -> g.getMembers().size()).sum();
    int legacyMatched = legacyGroups.stream().mapToInt(g -> g.getMembers().size()).sum();

    System.out.println("\n⚡ 현재 알고리즘 결과:");
    System.out.println("   ⏱️  실행 시간: " + (currentEnd - currentStart) + "ms");
    System.out.println("   📊 생성된 그룹 수: " + currentGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + currentMatched + "/100명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (currentMatched / 100.0) * 100));

    System.out.println("\n⚡ 이전 알고리즘 결과:");
    System.out.println("   ⏱️  실행 시간: " + (legacyEnd - legacyStart) + "ms");
    System.out.println("   📊 생성된 그룹 수: " + legacyGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + legacyMatched + "/100명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (legacyMatched / 100.0) * 100));

    System.out.println("\n🔄 성능 개선도:");
    long timeDiff = (legacyEnd - legacyStart) - (currentEnd - currentStart);
    int matchDiff = currentMatched - legacyMatched;
    System.out.println("   ⏱️  시간 차이: " + timeDiff + "ms " + (timeDiff > 0 ? "개선" : "느려짐"));
    System.out.println("   👥 매칭 개선: " + matchDiff + "명 " + (matchDiff > 0 ? "더 매칭" : "덜 매칭"));
    System.out.println();

    assertThat(currentGroups).isNotEmpty();
    assertThat(legacyGroups).isNotEmpty();
    assertThat(currentMatched).isBetween(1, 100);
    assertThat(legacyMatched).isBetween(1, 100);
  }

  @Test
  void 알고리즘성능비교_과목기반매칭_100명() {
    // Setup test data
    AcademicTerm term = createTestTerm();
    List<StudyApplicant> applicants = createTestApplicantsWithCoursePreferences(100, term);
    AtomicInteger currentTag = new AtomicInteger(1);
    AtomicInteger legacyTag = new AtomicInteger(1);

    System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                   📚 과목 기반 매칭 알고리즘 성능 비교                   ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 테스트 데이터: 100명 학생, 15개 과목, 다양한 우선순위");

    // Test current implementation
    long currentStart = System.currentTimeMillis();
    List<StudyGroup> currentGroups =
        currentTeamService.groupByCoursePreference(applicants, currentTag, term);
    long currentEnd = System.currentTimeMillis();

    // Reset applicants state for fair comparison
    resetApplicantsState(applicants);

    // Test legacy implementation
    long legacyStart = System.currentTimeMillis();
    Set<StudyGroup> legacyGroups = legacyTeamService.matchCourseFirst(applicants, legacyTag, term);
    long legacyEnd = System.currentTimeMillis();

    // Calculate statistics
    int currentMatched = currentGroups.stream().mapToInt(g -> g.getMembers().size()).sum();
    int legacyMatched = legacyGroups.stream().mapToInt(g -> g.getMembers().size()).sum();

    // Calculate student satisfaction
    double currentSatisfaction = calculateStudentSatisfaction(currentGroups);
    double legacySatisfaction = calculateStudentSatisfaction(new ArrayList<>(legacyGroups));

    System.out.println("\n⚡ 현재 알고리즘 결과:");
    System.out.println("   ⏱️  실행 시간: " + (currentEnd - currentStart) + "ms");
    System.out.println("   📊 생성된 그룹 수: " + currentGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + currentMatched + "/100명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (currentMatched / 100.0) * 100));
    System.out.println("   😊 학생 만족도: " + String.format("%.1f점", currentSatisfaction));

    System.out.println("\n⚡ 이전 알고리즘 결과:");
    System.out.println("   ⏱️  실행 시간: " + (legacyEnd - legacyStart) + "ms");
    System.out.println("   📊 생성된 그룹 수: " + legacyGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + legacyMatched + "/100명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (legacyMatched / 100.0) * 100));
    System.out.println("   😊 학생 만족도: " + String.format("%.1f점", legacySatisfaction));

    System.out.println("\n🔄 성능 개선도:");
    long timeDiff = (legacyEnd - legacyStart) - (currentEnd - currentStart);
    int matchDiff = currentMatched - legacyMatched;
    double satisfactionDiff = currentSatisfaction - legacySatisfaction;
    System.out.println("   ⏱️  시간 차이: " + timeDiff + "ms " + (timeDiff > 0 ? "개선" : "느려짐"));
    System.out.println("   👥 매칭 개선: " + matchDiff + "명 " + (matchDiff > 0 ? "더 매칭" : "덜 매칭"));
    System.out.println(
        "   😊 만족도 개선: "
            + String.format("%.1f점", satisfactionDiff)
            + " "
            + (satisfactionDiff > 0 ? "개선" : "하락"));
    System.out.println();

    assertThat(currentGroups).isNotEmpty();
    assertThat(legacyGroups).isNotEmpty();
    assertThat(currentMatched).isBetween(1, 100);
    assertThat(legacyMatched).isBetween(1, 100);
    assertThat(currentSatisfaction).isBetween(0.0, 100.0);
    assertThat(legacySatisfaction).isBetween(0.0, 100.0);
  }

  @Test
  void 알고리즘성능비교_대규모통합매칭_500명() {
    // Setup test data
    AcademicTerm term = createTestTerm();
    List<StudyApplicant> applicants = createLargeScaleTestData(500, term);
    AtomicInteger currentTag = new AtomicInteger(1);
    AtomicInteger legacyTag = new AtomicInteger(1);

    System.out.println("╔═══════════════════════════════════════════════════════════════════╗");
    System.out.println("║                  🚀 대규모 통합 매칭 알고리즘 성능 비교                  ║");
    System.out.println("╚═══════════════════════════════════════════════════════════════════╝");
    System.out.println("📊 테스트 데이터: 500명 학생, 15개 과목, 20% 친구 관계");

    // Test current implementation
    long currentStart = System.currentTimeMillis();
    List<StudyGroup> currentFriendGroups =
        currentTeamService.groupByFriends(applicants, currentTag, term);
    List<StudyGroup> currentCourseGroups =
        currentTeamService.groupByCoursePreference(applicants, currentTag, term);
    List<StudyGroup> currentAllGroups = new ArrayList<>();
    currentAllGroups.addAll(currentFriendGroups);
    currentAllGroups.addAll(currentCourseGroups);
    long currentEnd = System.currentTimeMillis();

    // Reset applicants state for fair comparison
    resetApplicantsState(applicants);

    // Test legacy implementation
    long legacyStart = System.currentTimeMillis();
    Set<StudyGroup> legacyFriendGroups =
        legacyTeamService.matchFriendFirst(applicants, legacyTag, term);
    Set<StudyGroup> legacyCourseGroups =
        legacyTeamService.matchCourseFirst(applicants, legacyTag, term);
    Set<StudyGroup> legacyAllGroups = new HashSet<>();
    legacyAllGroups.addAll(legacyFriendGroups);
    legacyAllGroups.addAll(legacyCourseGroups);
    long legacyEnd = System.currentTimeMillis();

    // Calculate statistics
    int currentMatched = currentAllGroups.stream().mapToInt(g -> g.getMembers().size()).sum();
    int legacyMatched = legacyAllGroups.stream().mapToInt(g -> g.getMembers().size()).sum();

    double currentThroughput = 500.0 / ((currentEnd - currentStart) / 1000.0);
    double legacyThroughput = 500.0 / ((legacyEnd - legacyStart) / 1000.0);

    System.out.println("\n⚡ 현재 알고리즘 결과:");
    System.out.println("   ⏱️  전체 실행 시간: " + (currentEnd - currentStart) + "ms");
    System.out.println("   📊 생성된 총 그룹 수: " + currentAllGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + currentMatched + "/500명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (currentMatched / 500.0) * 100));
    System.out.println("   ⚡ 처리량: " + String.format("%.1f", currentThroughput) + " 학생/초");

    System.out.println("\n⚡ 이전 알고리즘 결과:");
    System.out.println("   ⏱️  전체 실행 시간: " + (legacyEnd - legacyStart) + "ms");
    System.out.println("   📊 생성된 총 그룹 수: " + legacyAllGroups.size());
    System.out.println("   👥 매칭된 학생 수: " + legacyMatched + "/500명");
    System.out.println("   📈 매칭률: " + String.format("%.1f%%", (legacyMatched / 500.0) * 100));
    System.out.println("   ⚡ 처리량: " + String.format("%.1f", legacyThroughput) + " 학생/초");

    System.out.println("\n🔄 성능 개선도:");
    long timeDiff = (legacyEnd - legacyStart) - (currentEnd - currentStart);
    int matchDiff = currentMatched - legacyMatched;
    double throughputDiff = currentThroughput - legacyThroughput;
    System.out.println("   ⏱️  시간 차이: " + timeDiff + "ms " + (timeDiff > 0 ? "개선" : "느려짐"));
    System.out.println("   👥 매칭 개선: " + matchDiff + "명 " + (matchDiff > 0 ? "더 매칭" : "덜 매칭"));
    System.out.println("   ⚡ 처리량 개선: " + String.format("%.1f", throughputDiff) + " 학생/초");
    System.out.println();

    assertThat(currentAllGroups).isNotEmpty();
    assertThat(legacyAllGroups).isNotEmpty();
    assertThat(currentMatched).isBetween(1, 500);
    assertThat(legacyMatched).isBetween(1, 500);
    assertThat(currentThroughput).isGreaterThan(0.0);
    assertThat(legacyThroughput).isGreaterThan(0.0);
  }

  private AcademicTerm createTestTerm() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2024).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);
    return term;
  }

  private List<StudyApplicant> createTestApplicantsWithFriends(int count, AcademicTerm term) {
    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(42); // Fixed seed for reproducible results

    for (int i = 0; i < count; i++) {
      User user =
          User.builder()
              .sub("sub-" + i)
              .sid("2021" + String.format("%04d", i + 1))
              .name("학생" + (i + 1))
              .email("student" + (i + 1) + "@example.com")
              .role(Role.USER)
              .build();
      userRepository.save(user);

      StudyApplicant applicant = StudyApplicant.of(term, user, List.of(), List.of());
      studyApplicantRepository.save(applicant);
      applicants.add(applicant);
    }

    // Create friend relationships (30% chance)
    for (int i = 0; i < count; i++) {
      for (int j = i + 1; j < count; j++) {
        if (random.nextDouble() < 0.3) {
          RequestStatus status =
              random.nextDouble() < 0.85 ? RequestStatus.ACCEPTED : RequestStatus.PENDING;
          StudyPartnerRequest request =
              StudyPartnerRequest.of(applicants.get(i), applicants.get(j).getUser(), status);
          applicants.get(i).getPartnerRequests().add(request);
        }
      }
    }

    return applicants;
  }

  private List<StudyApplicant> createTestApplicantsWithCoursePreferences(
      int count, AcademicTerm term) {
    List<StudyApplicant> applicants = new ArrayList<>();
    List<Course> courses = createTestCourses();
    Random random = new Random(42);

    for (int i = 0; i < count; i++) {
      User user =
          User.builder()
              .sub("sub-" + i)
              .sid("2021" + String.format("%04d", i + 1))
              .name("학생" + (i + 1))
              .email("student" + (i + 1) + "@example.com")
              .role(Role.USER)
              .build();
      userRepository.save(user);

      StudyApplicant applicant = StudyApplicant.of(term, user, List.of(), List.of());

      // Create diverse course preferences (limited to valid priority values 0-2)
      int numPreferences =
          Math.min(random.nextInt(3) + 1, 3); // 1-3 preferences, max 3 for Priority enum
      Set<Course> selectedCourses = new HashSet<>();

      for (int p = 0; p < numPreferences; p++) {
        Course course;
        do {
          course = courses.get(random.nextInt(courses.size()));
        } while (selectedCourses.contains(course));
        selectedCourses.add(course);

        PreferredCourse preference = PreferredCourse.of(applicant, course, p);
        applicant.getPreferredCourses().add(preference);
      }

      studyApplicantRepository.save(applicant);
      applicants.add(applicant);
    }

    return applicants;
  }

  private List<StudyApplicant> createLargeScaleTestData(int count, AcademicTerm term) {
    List<StudyApplicant> applicants = new ArrayList<>();
    List<Course> courses = createTestCourses();
    Random random = new Random(42);

    for (int i = 0; i < count; i++) {
      User user =
          User.builder()
              .sub("sub-" + i)
              .sid("2021" + String.format("%04d", i + 1))
              .name("학생" + (i + 1))
              .email("student" + (i + 1) + "@example.com")
              .role(Role.USER)
              .build();
      userRepository.save(user);

      StudyApplicant applicant = StudyApplicant.of(term, user, List.of(), List.of());

      // Add course preferences (limited to valid priority values 0-2)
      int numPreferences =
          Math.min(random.nextInt(3) + 1, 3); // 1-3 preferences, max 3 for Priority enum
      Set<Course> selectedCourses = new HashSet<>();

      for (int p = 0; p < numPreferences; p++) {
        Course course;
        do {
          course = courses.get(random.nextInt(courses.size()));
        } while (selectedCourses.contains(course));
        selectedCourses.add(course);

        PreferredCourse preference = PreferredCourse.of(applicant, course, p);
        applicant.getPreferredCourses().add(preference);
      }

      studyApplicantRepository.save(applicant);
      applicants.add(applicant);
    }

    // Create friend relationships (20% chance for large scale)
    for (int i = 0; i < count; i++) {
      for (int j = i + 1; j < count && j < i + 10; j++) { // Limit connections for performance
        if (random.nextDouble() < 0.2) {
          RequestStatus status =
              random.nextDouble() < 0.85 ? RequestStatus.ACCEPTED : RequestStatus.PENDING;
          StudyPartnerRequest request =
              StudyPartnerRequest.of(applicants.get(i), applicants.get(j).getUser(), status);
          applicants.get(i).getPartnerRequests().add(request);
        }
      }
    }

    return applicants;
  }

  private List<Course> createTestCourses() {
    String[] courseNames = {
      "데이터구조", "알고리즘", "운영체제", "데이터베이스", "컴퓨터네트워크",
      "소프트웨어공학", "컴퓨터구조", "프로그래밍언어론", "인공지능", "머신러닝",
      "웹프로그래밍", "모바일프로그래밍", "게임프로그래밍", "시스템프로그래밍", "생물학"
    };

    // Use a shared term to avoid multiple term creation
    AcademicTerm sharedTerm =
        AcademicTerm.builder().academicYear(2024).semester(TermType.SPRING).isCurrent(true).build();

    List<Course> courses = new ArrayList<>();
    for (int i = 0; i < courseNames.length; i++) {
      Course course =
          Course.builder()
              .name(courseNames[i])
              .code("CS" + String.format("%03d", i + 1))
              .professor("Prof " + courseNames[i])
              .academicTerm(sharedTerm)
              .build();

      // Set courseId using reflection for comparison tests
      ReflectionTestUtils.setField(course, "courseId", (long) (i + 1));
      courses.add(course);
    }
    return courses;
  }

  private void resetApplicantsState(List<StudyApplicant> applicants) {
    // Reset study group assignment for fair comparison
    applicants.forEach(
        applicant -> {
          try {
            ReflectionTestUtils.setField(applicant, "studyGroup", null);
          } catch (Exception e) {
            // If reflection fails, recreate the applicant
          }
        });
  }

  private double calculateStudentSatisfaction(List<StudyGroup> groups) {
    double totalSatisfaction = 0;
    int totalStudents = 0;

    for (StudyGroup group : groups) {
      for (StudyApplicant member : group.getMembers()) {
        // Find the best priority match for this student in their group
        int bestPriority = findBestPriorityMatch(member, group);
        // Convert priority to satisfaction (0 = 100%, 1 = 80%, 2 = 60%, etc.)
        double satisfaction = Math.max(0, 100 - (bestPriority * 20));
        totalSatisfaction += satisfaction;
        totalStudents++;
      }
    }

    return totalStudents > 0 ? totalSatisfaction / totalStudents : 0;
  }

  private int findBestPriorityMatch(StudyApplicant student, StudyGroup group) {
    // Find common courses among group members
    Set<Course> groupCourses =
        group.getMembers().stream()
            .flatMap(member -> member.getPreferredCourses().stream())
            .map(PreferredCourse::getCourse)
            .collect(Collectors.toSet());

    // Find student's best priority among group courses
    return student.getPreferredCourses().stream()
        .filter(pc -> groupCourses.contains(pc.getCourse()))
        .mapToInt(PreferredCourse::getPriority)
        .min()
        .orElse(Integer.MAX_VALUE); // No match found
  }
}
