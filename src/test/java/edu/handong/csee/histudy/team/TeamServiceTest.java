package edu.handong.csee.histudy.team;

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
public class TeamServiceTest {
    @Autowired
    TeamService teamService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ChoiceRepository choiceRepository;
    @Autowired
    FriendshipRepository friendshipRepository;
    @MockBean
    AuthenticationInterceptor interceptor;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    ReportService reportService;
    @Autowired
    private ReportRepository reportRepository;

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
                .id("123")
                .sid("22000329")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User userB = User.builder()
                .id("124")
                .sid("22000330")
                .email("a@b.com")
                .role(Role.USER)
                .build();
        User userC = User.builder()
                .id("125")
                .sid("22000332")
                .email("a@c.com")
                .role(Role.USER)
                .build();
        User savedC = userRepository.save(userC);
        User savedA = userRepository.save(userA);
        User savedB = userRepository.save(userB);
        savedA.add(savedB);
        savedB.getReceivedRequests().stream().findAny().ifPresent(Friendship::accept);
        List<Long> courseIdxList = List.of(1L, 2L);
        List<Course> courses = courseIdxList.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Choice> choices = courses.stream().map(c -> choiceRepository.save(Choice.builder()
                .user(savedA)
                .course(c)
                .build())).toList();
        savedA.getChoices().addAll(choices);
        List<Long> courseIdxList2 = List.of(1L, 2L, 3L);
        List<Course> courses2 = courseIdxList2.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Choice> choices2 = courses2.stream().map(c -> choiceRepository.save(Choice.builder()
                .user(savedB)
                .course(c)
                .build())).toList();
        savedB.getChoices().addAll(choices2);
        Team team = teamRepository.save(new Team(111));
        savedA.belongTo(team);
        savedB.belongTo(team);

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
                .id("123")
                .sid("22000329")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User savedA = userRepository.save(userA);
        Team team = teamRepository.save(new Team(111));
        savedA.belongTo(team);
        int result = teamService.deleteTeam(new TeamIdDto(savedA.getTeam().getId()), "");
        assertThat(result).isNotZero();
        assertThat(savedA.getTeam()).isNull();
    }

    @DisplayName("팀의 보고서를 확인할 수 있다")
    @Test
    @Transactional
    public void teamReportViewTest() {
        User userA = User.builder()
                .id("123")
                .sid("22000329")
                .name("배주영")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User userB = User.builder()
                .id("124")
                .sid("22000330")
                .name("오인혁")
                .email("a@b.com")
                .role(Role.USER)
                .build();
        User savedA = userRepository.save(userA);
        User savedB = userRepository.save(userB);
        Team team = teamRepository.save(new Team(111));
        savedA.belongTo(team);
        savedB.belongTo(team);
        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .totalMinutes(60L)
                .participants(List.of("22000328"))
                .courses(List.of(1L, 2L, 3L))
                .build();
        reportService.createReport(form, "a@b.com");
        TeamReportDto dto = teamService.getTeamReports(team.getId(), "");
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
                .id("123")
                .sid("22000329")
                .name("ab")
                .build();
        User userB = User.builder()
                .id("234")
                .sid("22000123")
                .name("cd")
                .build();
        User savedUser = userRepository.save(userA);
        User savedUser2 = userRepository.save(userB);

        Team team = new Team(1);
        savedUser.belongTo(team);

        Team team2 = new Team(2);
        savedUser2.belongTo(team2);

        Report report = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(60L)
                .participants(List.of())
                .courses(List.of())
                .team(team)
                .build();

        Report report2 = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(120L)
                .participants(List.of())
                .courses(List.of())
                .team(team)
                .build();

        Report report3 = Report.builder()
                .title("title")
                .content("content")
                .totalMinutes(210L)
                .participants(List.of())
                .courses(List.of())
                .team(team2)
                .build();

        reportRepository.save(report);
        reportRepository.save(report2);
        reportRepository.save(report3);

        // when
        TeamRankDto res = teamService.getAllTeams();

        // then
        assertThat(res.getCount()).isEqualTo(2);
        assertThat(res.getTeams().get(0).getTotalMinutes()).isEqualTo(210);
        assertThat(res.getTeams().get(1).getTotalMinutes()).isEqualTo(180);
    }
}
