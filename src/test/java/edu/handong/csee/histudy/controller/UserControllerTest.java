package edu.handong.csee.histudy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.form.UserForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(controllers = {UserController.class, ExceptionController.class})
class UserControllerTest {

    private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationInterceptor authenticationInterceptor;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

    mockMvc =
        MockMvcBuilders.standaloneSetup(new UserController(userService, jwtService))
            .setControllerAdvice(new ExceptionController())
            .addInterceptors(authenticationInterceptor)
            .build();
    }

    @Test
    void 사용자가_회원가입시_성공() throws Exception {
        UserForm userForm = new UserForm("google-sub-123", "Test User", "user@test.com", "22500101");
        JwtPair tokens = new JwtPair(List.of("access-token", "refresh-token"));

        doNothing().when(userService).signUp(any(UserForm.class));
        when(jwtService.issueToken(anyString(), anyString(), any(Role.class))).thenReturn(tokens);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userForm)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.isRegistered").value(true))
                .andExpect(jsonPath("$.tokenType").value("Bearer "))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void 사용자가_유저검색시_성공_V1() throws Exception {
        String token = "Bearer access-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("friend@test.com");
        when(user.getRole()).thenReturn(Role.USER);
        List<User> users = List.of(user);

        when(jwtService.extractToken(any(Optional.class))).thenReturn("access-token");
        when(jwtService.validate(anyString())).thenReturn(claims);
        when(userService.search(any(Optional.class))).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .param("search", "friend")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_유저검색시_성공_V2() throws Exception {
        String token = "Bearer access-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("friend@test.com");
        when(user.getRole()).thenReturn(Role.USER);
        List<User> users = List.of(user);

        when(jwtService.extractToken(any(Optional.class))).thenReturn("access-token");
        when(jwtService.validate(anyString())).thenReturn(claims);
        when(userService.search(any(Optional.class))).thenReturn(users);

        mockMvc.perform(get("/api/v2/users")
                .param("search", "friend")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_내정보조회시_성공() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        UserDto.UserMe userMe = mock(UserDto.UserMe.class);
        when(userService.getUserMe(any(Optional.class))).thenReturn(userMe);

        mockMvc.perform(get("/api/users/me")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_신청정보조회시_성공_V1() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        StudyApplicant applicant = mock(StudyApplicant.class);
        when(applicant.getPartnerRequests()).thenReturn(List.of());
        when(applicant.getPreferredCourses()).thenReturn(List.of());

        when(userService.getUserInfo(anyString())).thenReturn(Optional.of(applicant));

        mockMvc.perform(get("/api/users/me/forms")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_없는신청정보조회시_실패_V1() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        when(userService.getUserInfo(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me/forms")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_신청정보조회시_성공_V2() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        StudyApplicant applicant = mock(StudyApplicant.class);
        when(applicant.getPartnerRequests()).thenReturn(List.of());
        when(applicant.getPreferredCourses()).thenReturn(List.of());

        when(userService.getUserInfo(anyString())).thenReturn(Optional.of(applicant));

        mockMvc.perform(get("/api/v2/users/me/forms")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_없는신청정보조회시_실패_V2() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        when(userService.getUserInfo(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/users/me/forms")
                .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 권한없는사용자가_접근시_실패() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("rol", String.class)).thenReturn("INVALID_ROLE");

        mockMvc.perform(get("/api/users/me")
                .requestAttr("claims", claims))
                .andExpect(status().isForbidden());
    }

    @Test
    void 사용자가_키워드없이유저검색시_성공() throws Exception {
        String token = "Bearer access-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");

        when(jwtService.extractToken(any(Optional.class))).thenReturn("access-token");
        when(jwtService.validate(anyString())).thenReturn(claims);
        when(userService.search(any(Optional.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}