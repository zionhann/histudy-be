package edu.handong.csee.histudy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.CourseService;
import edu.handong.csee.histudy.service.ImageService;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.ReportService;
import edu.handong.csee.histudy.service.TeamService;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(controllers = {TeamController.class, ExceptionController.class})
class TeamControllerTest {

    private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationInterceptor authenticationInterceptor;

    @MockBean
    private ReportService reportService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private TeamService teamService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AcademicTermRepository academicTermRepository;

    @MockBean
    private StudyGroupRepository studyGroupRepository;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new TeamController(
                    reportService,
                    courseService,
                    teamService,
                    imageService,
                    userRepository,
                    academicTermRepository,
                    studyGroupRepository))
            .setControllerAdvice(new ExceptionController())
            .addInterceptors(authenticationInterceptor)
            .build();
    }

    @Test
    void 그룹원이_스터디보고서생성시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        ReportForm form = ReportForm.builder()
                .courses(List.of(1L))
                .content("Study content")
                .totalMinutes(120L)
                .images(List.of("/path/to/image.jpg"))
                .build();

        ReportDto.ReportInfo reportInfo = mock(ReportDto.ReportInfo.class);
        when(reportService.createReport(any(ReportForm.class), anyString())).thenReturn(reportInfo);

        mockMvc.perform(post("/api/team/reports")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 그룹원이_보고서목록조회시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        List<ReportDto.ReportInfo> reports = List.of();
        when(reportService.getReports(anyString())).thenReturn(reports);

        mockMvc.perform(get("/api/team/reports")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 그룹원이_특정보고서조회시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        ReportDto.ReportInfo reportInfo = mock(ReportDto.ReportInfo.class);
        when(reportService.getReport(anyLong())).thenReturn(Optional.of(reportInfo));

        mockMvc.perform(get("/api/team/reports/1")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 그룹원이_없는보고서조회시_실패() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        when(reportService.getReport(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/team/reports/1")
                .requestAttr("claims", claims))
                .andExpect(status().isNotFound());
    }

    @Test
    void 그룹원이_보고서수정시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        ReportForm form = ReportForm.builder()
                .courses(List.of(1L))
                .content("Updated content")
                .totalMinutes(150L)
                .images(List.of("/path/to/updated_image.jpg"))
                .build();

        when(reportService.updateReport(anyLong(), any(ReportForm.class))).thenReturn(true);

        mockMvc.perform(patch("/api/team/reports/1")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk());
    }

    @Test
    void 그룹원이_없는보고서수정시_실패() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        ReportForm form = ReportForm.builder().build();
        when(reportService.updateReport(anyLong(), any(ReportForm.class))).thenReturn(false);

        mockMvc.perform(patch("/api/team/reports/1")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 그룹원이_보고서삭제시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        when(reportService.deleteReport(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/team/reports/1")
                .requestAttr("claims", claims))
                .andExpect(status().isOk());
    }

    @Test
    void 그룹원이_없는보고서삭제시_실패() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        when(reportService.deleteReport(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/team/reports/1")
                .requestAttr("claims", claims))
                .andExpect(status().isNotFound());
    }

    @Test
    void 그룹원이_선택강의목록조회시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        List<CourseDto.CourseInfo> courses = List.of();
        when(courseService.getTeamCourses(anyString())).thenReturn(courses);

        mockMvc.perform(get("/api/team/courses")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 그룹원이_팀원목록조회시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        List<UserDto.UserMeWithMasking> users = List.of();
        when(teamService.getTeamUsers(anyString())).thenReturn(users);

        mockMvc.perform(get("/api/team/users")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 그룹원이_보고서이미지업로드시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        String imagePath = "/path/to/image.jpg";
        when(imageService.getImagePaths(anyString(), any(), any(Optional.class))).thenReturn(imagePath);

        mockMvc.perform(multipart("/api/team/reports/image")
                .file(image)
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.imagePath").value(imagePath));
    }

    @Test
    void 그룹원이_특정보고서이미지업로드시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("member@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        String imagePath = "/path/to/image.jpg";
        when(imageService.getImagePaths(anyString(), any(), any(Optional.class))).thenReturn(imagePath);

        mockMvc.perform(multipart("/api/team/reports/1/image")
                .file(image)
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.imagePath").value(imagePath));
    }

    @Test
    void 권한없는사용자가_보고서작성시_실패() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        ReportForm form = ReportForm.builder().build();

        mockMvc.perform(post("/api/team/reports")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isForbidden());
    }
}