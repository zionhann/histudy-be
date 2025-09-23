package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.UserForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.support.TestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void 키워드없을시_전체학생조회() {
    // Given
    User student1 = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    User student2 = TestDataFactory.createUser("2", "22500102", "user2@test.com", "Bar", Role.USER);

    userRepository.save(student1);
    userRepository.save(student2);

    // When
    List<User> results = userService.search(Optional.empty());

    // Then
    assertThat(results.size()).isEqualTo(2);
  }

  @Test
  void 키워드제공시_매칭학생조회() {
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
  void 최초신청시_요청대기중() {
    // Given
    AcademicTerm term = TestDataFactory.createCurrentTerm();
    academicTermRepository.save(term);

    Course course = TestDataFactory.createCourse("Introduction to Test", "ECE2025", "Bar", term);
    courseRepository.saveAll(List.of(course));

    User student1 = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    User student2 = TestDataFactory.createUser("2", "22500102", "user2@test.com", "Bar", Role.USER);

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
  void 상호요청시_서로승인() {
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
  void 신청안했을시_정보없음() {
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
  void 신청했을시_정보있음() {
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

  @Test
  void 새사용자정보시_회원가입성공() {
    // Given
    UserForm userForm = new UserForm("google-sub-123", "New User", "newuser@test.com", "22500103");

    // When
    userService.signUp(userForm);

    // Then
    User savedUser = userRepository.findUserBySub("google-sub-123").orElse(null);
    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getEmail()).isEqualTo("newuser@test.com");
    assertThat(savedUser.getName()).isEqualTo("New User");
    assertThat(savedUser.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void 이미존재하는사용자시_예외발생() {
    // Given
    User existingUser =
        User.builder()
            .sub("google-sub-123")
            .sid("22500103")
            .email("existing@test.com")
            .name("Existing User")
            .build();
    userRepository.save(existingUser);

    UserForm userForm = new UserForm("google-sub-123", "New User", "newuser@test.com", "22500104");

    // When & Then
    assertThatThrownBy(() -> userService.signUp(userForm))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void sub제공시_사용자정보반환() {
    // Given
    User user =
        User.builder()
            .sub("google-sub-123")
            .sid("22500103")
            .email("user@test.com")
            .name("Test User")
            .build();
    userRepository.save(user);

    // When
    User foundUser = userService.getUser(Optional.of("google-sub-123"));

    // Then
    assertThat(foundUser.getEmail()).isEqualTo("user@test.com");
    assertThat(foundUser.getName()).isEqualTo("Test User");
  }

  @Test
  void sub없을시_예외발생() {
    // When & Then
    assertThatThrownBy(() -> userService.getUser(Optional.empty()))
        .isInstanceOf(MissingSubException.class);
  }

  @Test
  void 존재하지않는sub시_예외발생() {
    // When & Then
    assertThatThrownBy(() -> userService.getUser(Optional.of("nonexistent-sub")))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 이메일제공시_내정보반환() {
    // Given
    User user =
        User.builder()
            .sub("1")
            .sid("22500101")
            .email("user1@test.com")
            .name("Foo")
            .role(Role.USER)
            .build();
    userRepository.save(user);

    // When
    UserDto.UserMe userMe = userService.getUserMe(Optional.of("user1@test.com"));

    // Then
    assertThat(userMe.getEmail()).isEqualTo("user1@test.com");
    assertThat(userMe.getName()).isEqualTo("Foo");
  }

  @Test
  void 이메일없을시_예외발생() {
    // When & Then
    assertThatThrownBy(() -> userService.getUserMe(Optional.empty()))
        .isInstanceOf(MissingEmailException.class);
  }

  @Test
  void 신청폼제공시_신청성공() {
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
    userRepository.save(student2);

    ApplyForm form =
        ApplyForm.builder().friendIds(List.of("22500102")).courseIds(List.of(1L)).build();

    // When
    ApplyFormDto result = userService.apply(form, "user1@test.com");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getFriends()).hasSize(1);
  }

  @Test
  void 존재하지않는사용자신청시_예외발생() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    // When & Then
    assertThatThrownBy(() -> userService.apply(List.of(1L), List.of(1L), "nonexistent@test.com"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 현재학기없이신청시_예외발생() {
    // Given
    User student =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    // When & Then
    assertThatThrownBy(() -> userService.apply(List.of(), List.of(), "user1@test.com"))
        .isInstanceOf(NoCurrentTermFoundException.class);
  }

  @Test
  void 존재하지않는친구신청시_예외발생() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    User student =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    // When & Then
    assertThatThrownBy(() -> userService.apply(List.of(999L), List.of(), "user1@test.com"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 존재하지않는과목신청시_예외발생() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    User student =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    // When & Then
    assertThatThrownBy(() -> userService.apply(List.of(), List.of(999L), "user1@test.com"))
        .isInstanceOf(CourseNotFoundException.class);
  }

  @Test
  void 신청폼에존재하지않는친구시_예외발생() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    User student =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    ApplyForm form =
        ApplyForm.builder().friendIds(List.of("nonexistent")).courseIds(List.of()).build();

    // When & Then
    assertThatThrownBy(() -> userService.apply(form, "user1@test.com"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 신청서있을시_삭제성공() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE2025", "Bar", term);
    courseRepository.saveAll(List.of(course));

    User student =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    userService.apply(List.of(), List.of(1L), "user1@test.com");

    // When
    userService.deleteUserForm("22500101");

    // Then
    Optional<StudyApplicant> result = userService.getUserInfo("user1@test.com");
    assertThat(result).isEmpty();
  }

  @Test
  void 존재하지않는사용자삭제시_예외발생() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    // When & Then
    assertThatThrownBy(() -> userService.deleteUserForm("nonexistent"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 신청한사용자있을시_목록반환() {
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
    userRepository.save(student2);

    userService.apply(List.of(), List.of(1L), "user1@test.com");

    // When
    List<UserDto.UserInfo> appliedUsers = userService.getAppliedUsers();

    // Then
    assertThat(appliedUsers).hasSize(1);
    assertThat(appliedUsers.get(0).getEmail()).isEqualTo("user1@test.com");
  }

  @Test
  void 현재학기없이신청자조회시_예외발생() {
    // When & Then
    assertThatThrownBy(() -> userService.getAppliedUsers())
        .isInstanceOf(NoCurrentTermFoundException.class);
  }

  @Test
  void 존재하지않는새그룹으로이동시_새그룹생성() {
    // Given
    AcademicTerm term = new AcademicTerm(1L, 2025, TermType.SPRING, true);
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE2025", "Bar", term);
    courseRepository.saveAll(List.of(course));

    User student = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    userRepository.save(student);

    userService.apply(List.of(), List.of(1L), "user1@test.com");

    UserDto.UserEdit editForm =
        UserDto.UserEdit.builder()
            .id(student.getUserId())
            .team(999) // 존재하지 않는 그룹 태그
            .build();

    // When
    userService.editUser(editForm);

    // Then
    StudyGroup newGroup = studyGroupRepository.findByTagAndAcademicTerm(999, term).orElse(null);
    assertThat(newGroup).isNotNull();
    assertThat(newGroup.getTag()).isEqualTo(999);
    assertThat(newGroup.getMembers()).hasSize(1);
    assertThat(newGroup.getMembers().get(0).getUser()).isEqualTo(student);
  }
}
