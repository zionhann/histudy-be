package edu.handong.csee.histudy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.CourseIdDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.service.CourseService;
import edu.handong.csee.histudy.service.DiscordService;
import edu.handong.csee.histudy.service.JwtService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthenticationInterceptor authenticationInterceptor;

  @MockitoBean private CourseService courseService;

  @MockitoBean private JwtService jwtService;

  @MockitoBean private DiscordService discordService;

  @BeforeEach
  void setUp() throws Exception {
    when(authenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

    mockMvc =
        MockMvcBuilders.standaloneSetup(new CourseController(courseService))
            .setControllerAdvice(new ExceptionController(discordService))
            .addInterceptors(authenticationInterceptor)
            .build();
  }

  @Test
  void 관리자가_강의목록업로드시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    MockMultipartFile file =
        new MockMultipartFile("file", "courses.csv", "text/csv", "course content".getBytes());

    doNothing().when(courseService).readCourseCSV(any());

    mockMvc
        .perform(multipart("/api/courses").file(file).requestAttr("claims", claims))
        .andExpect(status().isCreated());
  }

  @Test
  void 관리자가_빈파일업로드시_실패() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    MockMultipartFile file =
        new MockMultipartFile("file", "courses.csv", "text/csv", "".getBytes());

    mockMvc
        .perform(multipart("/api/courses").file(file).requestAttr("claims", claims))
        .andExpect(status().isNotAcceptable());
  }

  @Test
  void 관리자가_강의삭제시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("admin@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.ADMIN.name());

    CourseIdDto dto = mock(CourseIdDto.class);
    when(courseService.deleteCourse(any(CourseIdDto.class))).thenReturn(1);

    mockMvc
        .perform(
            post("/api/courses/delete")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));
  }

  @Test
  void 사용자가_강의목록전체조회시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    List<CourseDto.CourseInfo> courses = List.of();
    when(courseService.getCurrentCourses()).thenReturn(courses);

    mockMvc
        .perform(get("/api/courses").requestAttr("claims", claims))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 사용자가_강의목록검색시_성공() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    List<CourseDto.CourseInfo> courses = List.of();
    when(courseService.search(anyString())).thenReturn(courses);

    mockMvc
        .perform(get("/api/courses").requestAttr("claims", claims).param("search", "java"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));
  }

  @Test
  void 사용자가_빈검색어로_강의목록조회시_현재강의목록_반환() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    List<CourseDto.CourseInfo> currentCourses = List.of();
    when(courseService.getCurrentCourses()).thenReturn(currentCourses);

    mockMvc
        .perform(get("/api/courses").requestAttr("claims", claims).param("search", ""))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));

    verify(courseService).getCurrentCourses();
    verify(courseService, never()).search(anyString());
  }

  @Test
  void 사용자가_앞뒤공백포함_검색어로_강의목록조회시_트림처리후_검색() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    List<CourseDto.CourseInfo> courses = List.of();
    when(courseService.search(anyString())).thenReturn(courses);

    mockMvc
        .perform(get("/api/courses").requestAttr("claims", claims).param("search", "  java  "))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"));

    verify(courseService).search("java");
    verify(courseService, never()).getCurrentCourses();
  }

  @Test
  void 권한없는사용자가_강의업로드시_실패() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    MockMultipartFile file =
        new MockMultipartFile("file", "courses.csv", "text/csv", "course content".getBytes());

    mockMvc
        .perform(multipart("/api/courses").file(file).requestAttr("claims", claims))
        .andExpect(status().isForbidden());
  }

  @Test
  void 권한없는사용자가_강의삭제시_실패() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

    CourseIdDto dto = mock(CourseIdDto.class);

    mockMvc
        .perform(
            post("/api/courses/delete")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  @Test
  void 권한없는사용자가_강의조회시_실패() throws Exception {
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("member@test.com");
    when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

    mockMvc
        .perform(get("/api/courses").requestAttr("claims", claims))
        .andExpect(status().isForbidden());
  }
}
