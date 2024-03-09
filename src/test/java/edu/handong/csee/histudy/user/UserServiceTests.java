package edu.handong.csee.histudy.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.ReportService;
import edu.handong.csee.histudy.service.UserService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceTests {
  @Autowired UserService userService;
  @Autowired UserRepository userRepository;
  @Autowired StudyGroupRepository studyGroupRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired UserCourseRepository userCourseRepository;
  @Autowired ReportService reportService;
  @MockBean AuthenticationInterceptor interceptor;
  @Autowired private StudyReportRepository studyReportRepository;

  @BeforeEach
  void setup() throws IOException {
    when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    Course course =
        Course.builder().name("기초전자공학실험").code("ECE20007").courseYear(2023).semester(1).build();
    courseRepository.save(course);
    Course courseB =
        Course.builder()
            .name("데이타구조")
            .code("ECE20010")
            .professor("김호준")
            .courseYear(2023)
            .semester(1)
            .build();
    courseRepository.save(courseB);
    Course courseC =
        Course.builder()
            .name("자바프로그래밍언어")
            .code("ECE20017")
            .professor("남재창")
            .courseYear(2023)
            .semester(1)
            .build();
    courseRepository.save(courseC);
  }

  @DisplayName("전체 유저 목록을 불러올 수 있어야 한다")
  @Test
  public void userListTest() {
    User userA =
        User.builder().sid("22000329").name("배주영").email("a@a.com").role(Role.USER).build();
    User userB =
        User.builder().sid("22000330").name("오인혁").email("a@b.com").role(Role.USER).build();
    User userC =
        User.builder().sid("22000332").name("한시온").email("a@c.com").role(Role.USER).build();
    User savedA = userRepository.save(userA);
    User savedB = userRepository.save(userB);
    User savedC = userRepository.save(userC);
    savedA.addUser(List.of(savedB));
    savedB.getReceivedRequests().stream().findAny().ifPresent(StudyPartnerRequest::accept);
    List<Long> courseIdxList = List.of(1L, 2L);
    List<Course> courses =
        courseIdxList.stream()
            .map(courseRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    List<PreferredCourse> preferredCours =
        courses.stream()
            .map(
                c ->
                    userCourseRepository.save(
                        PreferredCourse.builder().user(savedA).course(c).build()))
            .toList();
    savedA.getCourseSelections().addAll(preferredCours);
    List<Long> courseIdxList2 = List.of(1L, 2L, 3L);
    List<Course> courses2 =
        courseIdxList2.stream()
            .map(courseRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    List<PreferredCourse> choices2 =
        courses2.stream()
            .map(
                c ->
                    userCourseRepository.save(
                        PreferredCourse.builder().user(savedB).course(c).build()))
            .toList();
    savedB.getCourseSelections().addAll(choices2);
    StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(111, List.of(savedA, savedB)));
    ReportForm form =
        ReportForm.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .participants(List.of(savedA.getUserId()))
            .courses(List.of(1L, 2L, 3L))
            .build();
    ReportDto.ReportInfo report = reportService.createReport(form, "a@a.com");
    List<UserDto.UserInfo> users = userService.getUsers("");
    assertThat(users).isNotEmpty();
    assertThat(users.size()).isEqualTo(3);
    List<StudyReport> studyReports = studyReportRepository.findAll();
    System.out.println("reports = " + studyReports);
    System.out.println("users = " + users);
    assertThat(users.get(0).getTotalMinutes()).isEqualTo(60L);
    System.out.println("users = " + users);
  }

  @DisplayName("신청한 유저들의 리스트를 받을 수 있어야 한다")
  @Test
  public void testAppliedUsersTest() {
    User userA =
        User.builder().sid("22000329").name("배주영").email("a@a.com").role(Role.USER).build();
    User userB =
        User.builder().sid("22000330").name("오인혁").email("a@b.com").role(Role.USER).build();
    User userC =
        User.builder().sid("22000332").name("한시온").email("a@c.com").role(Role.USER).build();
    User savedA = userRepository.save(userA);
    User savedB = userRepository.save(userB);
    User savedC = userRepository.save(userC);
    savedA.addUser(List.of(savedB));
    savedB.getReceivedRequests().stream().findAny().ifPresent(StudyPartnerRequest::accept);
    List<Long> courseIdxList = courseRepository.findAll().stream().map(Course::getCourseId).toList();
    List<Course> courses =
        courseIdxList.stream()
            .map(courseRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    List<PreferredCourse> preferredCourse =
        courses.stream()
            .map(
                c ->
                    userCourseRepository.save(
                        PreferredCourse.builder().user(savedA).course(c).priority(0).build()))
            .toList();
    savedA.getCourseSelections().addAll(preferredCourse);
    List<Long> courseIdxList2 = courseRepository.findAll().stream().map(Course::getCourseId).toList();
    List<Course> courses2 =
        courseIdxList2.stream()
            .map(courseRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    List<PreferredCourse> choices2 =
        courses2.stream()
            .map(
                c ->
                    userCourseRepository.save(
                        PreferredCourse.builder().user(savedB).course(c).priority(0).build()))
            .toList();
    savedB.getCourseSelections().addAll(choices2);
    List<UserDto.UserInfo> users = userService.getAppliedUsers();
    assertThat(users.size()).isEqualTo(2);
    System.out.println("users = " + users);
  }

  @DisplayName("유저의 신청서를 지울 수 있어야 한다")
  @Test
  public void deleteFormTest() {
    User userA =
        User.builder().sid("22000329").name("배주영").email("a@a.com").role(Role.USER).build();
    User userB =
        User.builder().sid("22000330").name("오인혁").email("a@b.com").role(Role.USER).build();
    User userC =
        User.builder().sid("22000332").name("한시온").email("a@c.com").role(Role.USER).build();
    User savedA = userRepository.save(userA);
    User savedB = userRepository.save(userB);
    User savedC = userRepository.save(userC);

    Course course = Course.builder().name("courseName").build();
    Course savedCourse = courseRepository.save(course);

    ApplyForm form =
        ApplyForm.builder()
            .friendIds(List.of(savedB.getSid(), savedC.getSid()))
            .courseIds(List.of(savedCourse.getCourseId()))
            .build();

    ApplyForm form2 =
        ApplyForm.builder()
            .friendIds(List.of(savedA.getSid()))
            .courseIds(List.of(savedCourse.getCourseId()))
            .build();

    userService.apply(form, savedA.getEmail());
    userService.apply(form2, savedB.getEmail());

    assertThat(
            savedB.getReceivedRequests().stream()
                .filter(StudyPartnerRequest::isAccepted)
                .toList()
                .size())
        .isNotZero();
    userService.deleteUserForm(savedA.getSid());
    assertThat(
            savedB.getReceivedRequests().stream()
                .filter(StudyPartnerRequest::isAccepted)
                .toList()
                .size())
        .isZero();
    assertThat(
            savedB.getSentRequests().stream()
                .filter(StudyPartnerRequest::isPending)
                .toList()
                .size())
        .isNotZero();
    assertThat(savedA.getCourseSelections().size()).isZero();
  }

  @DisplayName("유저의 정보를 수정할 수 있어야한다")
  @Test
  public void userEditTest() {
    User user = User.builder().sid("22000329").name("배주영").email("a@a.com").role(Role.USER).build();

    User saved = userRepository.save(user);
    StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(111, List.of(saved)));

    UserDto.UserEdit dto =
        UserDto.UserEdit.builder()
            .userId(saved.getUserId())
            .sid("12345678")
            .name("조용히해라")
            .team(222)
            .build();

    UserDto.UserInfo edited = userService.editUser(dto);

    assertThat(edited.getName()).isEqualTo("조용히해라");
    assertThat(edited.getSid()).isEqualTo("12345678");
    assertThat(edited.getGroup()).isEqualTo(dto.getTeam());
  }
}
