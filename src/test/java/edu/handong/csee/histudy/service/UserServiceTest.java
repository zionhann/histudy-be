package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  private UserService userService;

  private UserRepository userRepository;
  private CourseRepository courseRepository;
  private StudyGroupRepository studyGroupRepository;
  private AcademicTermRepository academicTermRepository;
  private StudyApplicantRepository studyApplicantRepository;

  @BeforeEach
  void init() {
    userRepository = new FakeUserRepository();
    courseRepository = new FakeCourseRepository();
    studyGroupRepository = new FakeStudyGroupRepository();
    academicTermRepository = new FakeAcademicTermRepository();
    studyApplicantRepository = new FakeStudyApplicationRepository();

    userService =
        new UserService(
            userRepository,
            courseRepository,
            studyGroupRepository,
            academicTermRepository,
            studyApplicantRepository);
  }

  @Test
  void 학생명단_검색_키워드없는경우_전부표시() {
    // Given
    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    userRepository.save(student2);

    // When
    List<User> results = userService.search(Optional.empty());

    // Then
    assertThat(results.size()).isEqualTo(2);
  }

  @Test
  void 학생명단_검색_키워드있는경우_해당하는학생만표시() {
    // Given
    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    userRepository.save(student2);

    // When
    List<User> results = userService.search(Optional.of("22500101"));

    // Then
    assertThat(results.size()).isEqualTo(1);
  }

  @Test
  void 스터디그룹_최초신청한경우_요청대기중() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE2025", "Bar", term);
    courseRepository.saveAll(List.of(course));

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    User bar = userRepository.save(student2);

    // When
    StudyApplicant applicant =
        userService.apply(List.of(bar.getUserId()), List.of(1L), "user1@test.com");

    // Then
    assertThat(applicant.getPartnerRequests().size()).isEqualTo(1);
    assertThat(applicant.getPartnerRequests().get(0).getRequestStatus())
        .isEqualTo(RequestStatus.PENDING);
  }

  @Test
  void 스터디그룹_같이하고싶은멤버로_추가된요청이있는경우_서로ACCEPTED() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE2025", "Bar", term);
    courseRepository.saveAll(List.of(course));

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    User bar = userRepository.save(student2);

    StudyApplicant applicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));
    studyApplicantRepository.save(applicant2);

    // When
    StudyApplicant applicant =
        userService.apply(List.of(bar.getUserId()), List.of(1L), "user1@test.com");

    // Then
    assertThat(applicant.getPartnerRequests().get(0).getRequestStatus())
        .isEqualTo(RequestStatus.ACCEPTED);
    assertThat(applicant2.getPartnerRequests().get(0).getRequestStatus())
        .isEqualTo(RequestStatus.ACCEPTED);
  }

  @Test
  void 스터디그룹_신청하지않은경우_신청정보_없음() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    userRepository.save(student1);

    // When
    Optional<StudyApplicant> res = userService.getUserInfo("user1@test.com");

    // Then
    assertThat(res.isEmpty()).isTrue();
  }

  @Test
  void 스터디그룹_신청한경우_신청정보_있음() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE2025", "Bar", term);
    courseRepository.saveAll(List.of(course));

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    User bar = userRepository.save(student2);

    userService.apply(List.of(bar.getUserId()), List.of(1L), "user1@test.com");

    // When
    Optional<StudyApplicant> res = userService.getUserInfo("user1@test.com");

    // Then
    assertThat(res.isPresent()).isTrue();
  }
}
