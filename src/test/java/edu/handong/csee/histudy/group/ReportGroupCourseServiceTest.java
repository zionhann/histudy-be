package edu.handong.csee.histudy.group;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.dto.TeamRankDto;
import edu.handong.csee.histudy.dto.TeamReportDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.ReportService;
import edu.handong.csee.histudy.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class ReportGroupCourseServiceTest {
    @Autowired
    TeamService teamService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    UserCourseRepository userCourseRepository;
    @Autowired
    FriendshipRepository friendshipRepository;
    @MockBean
    AuthenticationInterceptor interceptor;
    @Autowired
    StudyGroupRepository studyGroupRepository;
    @Autowired
    ReportService reportService;
    @Autowired
    private GroupReportRepository groupReportRepository;

    @BeforeEach
    void setup() throws IOException {
        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
        Course course = Course.builder()
                .name("기초전자공학실험")
                .code("ECE20007")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(course);
        Course courseB = Course.builder()
                .name("데이타구조")
                .code("ECE20010")
                .professor("김호준")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(courseB);
        Course courseC = Course.builder()
                .name("자바프로그래밍언어")
                .code("ECE20017")
                .professor("남재창")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(courseC);
    }

    @DisplayName("하나의 팀에 속한 사람들의 정보가 떠야한다")
    @Test
    @Transactional
    public void teamService() {
        User userA = User.builder()
                .sid("22000329")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User userB = User.builder()
                .sid("22000330")
                .email("a@b.com")
                .role(Role.USER)
                .build();
        User userC = User.builder()
                .sid("22000332")
                .email("a@c.com")
                .role(Role.USER)
                .build();
        User savedC = userRepository.save(userC);
        User savedA = userRepository.save(userA);
        User savedB = userRepository.save(userB);
        savedA.addUser(List.of(savedB));
        savedB.getReceivedRequests().stream().findAny().ifPresent(Friendship::accept);
        List<Long> courseIdxList = List.of(1L, 2L);
        List<Course> courses = courseIdxList.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<UserCourse> preferredCourse = courses.stream().map(c -> userCourseRepository.save(UserCourse.builder()
                .user(savedA)
                .course(c)
                .priority(0)
                .build())).toList();
        savedA.getCourseSelections().addAll(preferredCourse);
        List<Long> courseIdxList2 = List.of(1L, 2L, 3L);
        List<Course> courses2 = courseIdxList2.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<UserCourse> choices2 = courses2.stream().map(c -> userCourseRepository.save(UserCourse.builder()
                .user(savedB)
                .course(c)
                .priority(0)
                .build())).toList();
        savedB.getCourseSelections().addAll(choices2);
        StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(111, List.of(savedA, savedB)));

        String email = "";
        List<TeamDto> teams = teamService.getTeams(email);
        System.out.println("teams = " + teams);
        assertThat(teams.size()).isNotZero();
        assertThat(teams.get(0).getMembers().size()).isEqualTo(2);
    }

    @DisplayName("팀을 삭제할 수 있어야 한다")
    @Test
    @Transactional
    public void deleteTeamTest() {
        User userA = User.builder()
                .sid("22000329")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User savedA = userRepository.save(userA);
        StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(111, List.of(savedA)));
        savedA.belongTo(studyGroup);
        int result = teamService.deleteTeam(new TeamIdDto(savedA.getStudyGroup().getId()), "");
        assertThat(result).isNotZero();
        assertThat(savedA.getStudyGroup()).isNull();
    }

    @DisplayName("팀의 보고서를 확인할 수 있다")
    @Test
    @Transactional
    public void teamReportViewTest() {
        User userA = User.builder()
                .sid("22000329")
                .name("배주영")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User userB = User.builder()
                .sid("22000330")
                .name("오인혁")
                .email("a@b.com")
                .role(Role.USER)
                .build();
        User savedA = userRepository.save(userA);
        User savedB = userRepository.save(userB);
        StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(111, List.of(savedA, savedB)));
        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .totalMinutes(60L)
                .participants(List.of(savedB.getId()))
                .courses(List.of(1L, 2L, 3L))
                .build();
        reportService.createReport(form, "a@b.com");
        TeamReportDto dto = teamService.getTeamReports(studyGroup.getId(), "");
        assertThat(dto.getMembers().size()).isEqualTo(2);
        assertThat(dto.getReports().size()).isEqualTo(1);
        assertThat(dto.getTotalTime()).isEqualTo(60L);
        System.out.println("dto = " + dto);
    }

    @DisplayName("전체 팀이 총학습시간에 대해 내림차순으로 정렬되어야 한다")
    @Test
    void TeamServiceTest_193() {
        // given
        User userA = User.builder()
                .sid("22000329")
                .name("ab")
                .build();
        User userB = User.builder()
                .sid("22000123")
                .name("cd")
                .build();
        User savedUser = userRepository.save(userA);
        User savedUser2 = userRepository.save(userB);

        StudyGroup studyGroup = new StudyGroup(1, List.of(savedUser));
        StudyGroup studyGroup2 = new StudyGroup(2, List.of(savedUser2));

        GroupReport groupReport = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60L)
                .participants(List.of())
                .courses(List.of())
                .studyGroup(studyGroup)
                .images(List.of("img.jpg"))
                .build();

        GroupReport groupReport2 = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(120L)
                .participants(List.of())
                .courses(List.of())
                .studyGroup(studyGroup)
                .images(List.of("img2.jpg"))
                .build();

        GroupReport groupReport3 = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(210L)
                .participants(List.of())
                .courses(List.of())
                .studyGroup(studyGroup2)
                .images(List.of("img3.jpg"))
                .build();

        groupReportRepository.save(groupReport);
        groupReportRepository.save(groupReport2); // the latest report of team 1
        groupReportRepository.save(groupReport3); // the latest report of team 2

        // when
        TeamRankDto res = teamService.getAllTeams();

        // then
        assertThat(res.getTeams().size()).isEqualTo(2);
        assertThat(res.getTeams().get(0).getTotalMinutes()).isEqualTo(210);
        assertThat(res.getTeams().get(1).getTotalMinutes()).isEqualTo(180);

        assertThat(res.getTeams().get(0).getThumbnail()).isEqualTo("img3.jpg"); // team 2
        assertThat(res.getTeams().get(1).getThumbnail()).isEqualTo("img2.jpg"); // team 1

    }
}
