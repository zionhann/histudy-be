package edu.handong.csee.histudy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.form.AcademicTermForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.dto.AcademicTermDto;
import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.dto.TeamReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.service.AcademicTermService;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.TeamService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockBean private AuthenticationInterceptor authenticationInterceptor;

  @MockBean private TeamService teamService;

  @MockBean private UserService userService;

  @MockBean private AcademicTermService academicTermService;

  @MockBean private JwtService jwtService;

  @BeforeEach
  void setUp() throws Exception {
    when(authenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new AdminController(teamService, userService, academicTermService))
            .setControllerAdvice(ExceptionController.class)
            .addInterceptors(authenticationInterceptor)
            .build();
  }

  @Test
  void 관리자가_그룹활동조회시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    List<TeamDto> teams = List.of();
    when(teamService.getTeams(anyString())).thenReturn(teams);

    mockMvc
        .perform(get("/api/admin/manageGroup").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 관리자가_그룹삭제시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    TeamIdDto dto = mock(TeamIdDto.class);
    when(teamService.deleteTeam(any(TeamIdDto.class), anyString())).thenReturn(1);

    mockMvc
        .perform(
            delete("/api/admin/group")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  void 관리자가_그룹보고서조회시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    TeamReportDto reportDto = mock(TeamReportDto.class);
    when(teamService.getTeamReports(anyLong(), anyString())).thenReturn(reportDto);

    mockMvc
        .perform(get("/api/admin/groupReport/1").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 관리자가_신청유저목록조회시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    List<UserDto.UserInfo> users = List.of();
    when(userService.getAppliedUsers()).thenReturn(users);

    mockMvc
        .perform(get("/api/admin/allUsers").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 관리자가_그룹매칭실행시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    doNothing().when(teamService).matchTeam();

    mockMvc
        .perform(post("/api/admin/team-match").requestAttr("claims", claims))
        .andExpect(status().isCreated());
  }

  @Test
  void 관리자가_미매칭유저조회시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    List<UserDto.UserInfo> users = List.of();
    when(userService.getUnmatchedUsers()).thenReturn(users);

    mockMvc
        .perform(get("/api/admin/unmatched-users").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 관리자가_유저지원폼삭제시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    doNothing().when(userService).deleteUserForm(anyString());

    mockMvc
        .perform(delete("/api/admin/form").requestAttr("claims", claims).param("sid", "22500101"))
        .andExpect(status().isOk());
  }

  @Test
  void 관리자가_유저정보수정시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    UserDto.UserEdit form = mock(UserDto.UserEdit.class);
    doNothing().when(userService).editUser(any(UserDto.UserEdit.class));

    mockMvc
        .perform(
            post("/api/admin/edit-user")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
        .andExpect(status().isOk());
  }

  @Test
  void 관리자가_미배정유저목록조회시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    List<UserDto.UserInfo> users = List.of();
    when(userService.getAppliedWithoutGroup()).thenReturn(users);

    mockMvc
        .perform(get("/api/admin/users/unassigned").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 일반유저가_관리자API접근시_실패() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    mockMvc
        .perform(get("/api/admin/manageGroup").requestAttr("claims", claims))
        .andExpect(status().isForbidden());
  }

  @Test
  void 관리자가_학기생성시_성공() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    AcademicTermForm form = new AcademicTermForm(2025, TermType.SPRING);
    doNothing().when(academicTermService).createAcademicTerm(any(AcademicTermForm.class));

    // When & Then
    mockMvc
        .perform(
            post("/api/admin/academicTerm")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
        .andExpect(status().isCreated());

    verify(academicTermService).createAcademicTerm(any(AcademicTermForm.class));
  }

  @Test
  void 비관리자가_학기생성시_실패() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    AcademicTermForm form = new AcademicTermForm(2025, TermType.SPRING);

    // When & Then
    mockMvc
        .perform(
            post("/api/admin/academicTerm")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
        .andExpect(status().isForbidden());

    verify(academicTermService, never()).createAcademicTerm(any());
  }

  @Test
  void 관리자가_학기목록조회시_성공() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    List<AcademicTermForm> termForms =
        List.of(
            new AcademicTermForm(2024, TermType.FALL), new AcademicTermForm(2025, TermType.SPRING));
    AcademicTermDto response = new AcademicTermDto(termForms);
    when(academicTermService.getAllAcademicTerms()).thenReturn(response);

    // When & Then
    mockMvc
        .perform(get("/api/admin/academicTerm").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.academicTerms").isArray())
        .andExpect(jsonPath("$.academicTerms.length()").value(2))
        .andDo(MockMvcResultHandlers.print());

    verify(academicTermService).getAllAcademicTerms();
  }

  @Test
  void 비관리자가_학기목록조회시_실패() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    // When & Then
    mockMvc
        .perform(get("/api/admin/academicTerm").requestAttr("claims", claims))
        .andExpect(status().isForbidden());

    verify(academicTermService, never()).getAllAcademicTerms();
  }

  @Test
  void 관리자가_현재학기설정시_성공() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    Long termId = 1L;
    doNothing().when(academicTermService).setCurrentTerm(termId);

    // When & Then
    mockMvc
        .perform(
            patch("/api/admin/academicTerm/{id}/current", termId).requestAttr("claims", claims))
        .andExpect(status().isOk());

    verify(academicTermService).setCurrentTerm(termId);
  }

  @Test
  void 비관리자가_현재학기설정시_실패() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    Long termId = 1L;

    // When & Then
    mockMvc
        .perform(
            patch("/api/admin/academicTerm/{id}/current", termId).requestAttr("claims", claims))
        .andExpect(status().isForbidden());

    verify(academicTermService, never()).setCurrentTerm(any());
  }

  @Test
  void 학기생성시_잘못된_JSON형식일때_실패() throws Exception {
    // Given
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    String invalidJson = "{\"year\": \"invalid\", \"term\": \"SPRING\"}";

    // When & Then
    mockMvc
        .perform(
            post("/api/admin/academicTerm")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(invalidJson))
        .andExpect(status().isBadRequest());

    verify(academicTermService, never()).createAcademicTerm(any());
  }
}
