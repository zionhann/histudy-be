package edu.handong.csee.histudy.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.TeamController;
import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.GroupReportRepository;
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
public class ReportCourseReportControllerTestsGroup {

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
    private GroupReportRepository groupReportRepository;
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
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .role(Role.MEMBER)
                .build());
        user.belongTo(new StudyGroup(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        GroupReport groupReport = GroupReport.builder()
                .title("reportTitle")
                .content("reportContent")
                .studyGroup(user.getStudyGroup())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        groupReportRepository.save(groupReport);

        Claims claims = Jwts.claims();
        claims.put("sub", user.getEmail());
        claims.put("rol", user.getRole().name());

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

    @DisplayName("보고서를 조회한다")
    @Test
    void ReportControllerTests_122() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .role(Role.MEMBER)
                .build());
        user.belongTo(new StudyGroup(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        GroupReport groupReport = GroupReport.builder()
                .title("reportTitle")
                .content("reportContent")
                .studyGroup(user.getStudyGroup())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        groupReportRepository.save(groupReport);

        Claims claims = Jwts.claims();
        claims.put("sub", user.getEmail());
        claims.put("rol", user.getRole().name());

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/team/reports/{id}", groupReport.getId())
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ReportDto.ReportInfo res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ReportDto.ReportInfo.class);

        // then
        assertEquals(groupReport.getId(), res.getId());
    }

    @DisplayName("그룹의 보고서를 수정할 수 있다: 요청폼 없음")
    @Test
    void ReportControllerTests_121() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .build());
        user.belongTo(new StudyGroup(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        GroupReport groupReport = GroupReport.builder()
                .title("reportTitle")
                .content("reportContent")
                .studyGroup(user.getStudyGroup())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        groupReportRepository.save(groupReport);

        Claims claims = Jwts.claims(
                Collections.singletonMap(Claims.SUBJECT, user.getEmail()));

        // when
        mvc
                .perform(patch("/api/team/reports/{reportId}", groupReport.getId())
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
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .role(Role.MEMBER)
                .build());
        user.belongTo(new StudyGroup(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        GroupReport groupReport = GroupReport.builder()
                .title("reportTitle")
                .content("reportContent")
                .studyGroup(user.getStudyGroup())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        groupReportRepository.save(groupReport);

        String form = mapper.writeValueAsString(ReportForm.builder()
                .title("modified title")
                .build());

        Claims claims = Jwts.claims();
        claims.put("sub", user.getEmail());
        claims.put("rol", user.getRole().name());

        // when
        mvc
                .perform(patch("/api/team/reports/{reportId}", groupReport.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("claims", claims)
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
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .role(Role.USER)
                .build());
        user.belongTo(new StudyGroup(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        GroupReport groupReport = GroupReport.builder()
                .title("reportTitle")
                .content("reportContent")
                .studyGroup(user.getStudyGroup())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        groupReportRepository.save(groupReport);

        Claims claims = Jwts.claims();
        claims.put("sub", user.getEmail());
        claims.put("rol", user.getRole().name());

        // when
        mvc
                .perform(delete("/api/team/reports/{reportId}", groupReport.getId())
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @DisplayName("그룹 보고서를 삭제할 수 있다: 보고서 ID가 존재하지 않는 경우")
    @Test
    void ReportControllerTests_240() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .sid("21811111")
                .name("username")
                .email("user@test.com")
                .role(Role.MEMBER)
                .build());
        user.belongTo(new StudyGroup(1));

        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        GroupReport groupReport = GroupReport.builder()
                .title("reportTitle")
                .content("reportContent")
                .studyGroup(user.getStudyGroup())
                .totalMinutes(60L)
                .participants(List.of(user))
                .courses(List.of(course))
                .build();
        groupReportRepository.save(groupReport);

        Claims claims = Jwts.claims();
        claims.put("sub", user.getEmail());
        claims.put("rol", user.getRole().name());

        // when
        mvc
                .perform(delete("/api/team/reports/{reportId}", 333)
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
