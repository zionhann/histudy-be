package edu.handong.csee.histudy.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.TeamController;
import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.ReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ReportControllerTests {

    MockMvc mvc;

    @Autowired
    TeamController teamController;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    AuthenticationInterceptor interceptor;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void init() throws IOException {
        mvc = MockMvcBuilders
                .standaloneSetup(teamController)
                .addInterceptors(interceptor)
                .build();
        when(interceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    @DisplayName("자기 그룹의 보고서 목록을 가져올 수 있다")
    @Test
    void ReportControllerTests_16() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .id("123")
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .build());
        user.belongTo(new Team(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        Report report = Report.builder()
                .title("reportTitle")
                .content("reportContent")
                .team(user.getTeam())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        reportRepository.save(report);

        Claims claims = Jwts.claims(
                Collections.singletonMap(Claims.SUBJECT, user.getEmail()));

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/team/reports")
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ReportDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ReportDto.class);

        // then
        assertEquals(1, res.getReports().size());
    }

    @DisplayName("그룹의 보고서를 수정할 수 있다: 요청폼 없음")
    @Test
    void ReportControllerTests_121() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .id("123")
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .build());
        user.belongTo(new Team(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        Report report = Report.builder()
                .title("reportTitle")
                .content("reportContent")
                .team(user.getTeam())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        reportRepository.save(report);

        Claims claims = Jwts.claims(
                Collections.singletonMap(Claims.SUBJECT, user.getEmail()));

        // when
        mvc
                .perform(patch("/api/team/reports/{reportId}", report.getId())
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @DisplayName("그룹의 보고서를 수정할 수 있다: 유효한 경우")
    @Test
    void ReportControllerTests_159() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .id("123")
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .build());
        user.belongTo(new Team(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        Report report = Report.builder()
                .title("reportTitle")
                .content("reportContent")
                .team(user.getTeam())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        reportRepository.save(report);

        String form = mapper.writeValueAsString(ReportForm.builder()
                .title("modified title")
                .build());

        // when
        mvc
                .perform(patch("/api/team/reports/{reportId}", report.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @DisplayName("그룹 보고서를 삭제할 수 있다.")
    @Test
    void ReportControllerTests_204() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .id("123")
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .build());
        user.belongTo(new Team(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        Report report = Report.builder()
                .title("reportTitle")
                .content("reportContent")
                .team(user.getTeam())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        reportRepository.save(report);

        String form = mapper.writeValueAsString(ReportForm.builder()
                .title("modified title")
                .build());

        // when
        mvc
                .perform(delete("/api/team/reports/{reportId}", report.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @DisplayName("그룹 보고서를 삭제할 수 있다: 보고서 ID가 존재하지 않는 경우")
    @Test
    void ReportControllerTests_240() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .id("123")
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .build());
        user.belongTo(new Team(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        Report report = Report.builder()
                .title("reportTitle")
                .content("reportContent")
                .team(user.getTeam())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        reportRepository.save(report);

        // when
        mvc
                .perform(delete("/api/team/reports/{reportId}", 333))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
