package edu.handong.csee.histudy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.form.TokenForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.controller.ExceptionController;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.jwt.GrantType;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.jwt.JwtProperties;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AuthController.class, ExceptionController.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtProperties jwtProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 사용자가_로그인시_성공() throws Exception {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getName()).thenReturn("Test User");
        when(user.getRole()).thenReturn(Role.USER);

        JwtPair tokens = new JwtPair(List.of("access-token", "refresh-token"));

        when(userService.getUser(any(Optional.class))).thenReturn(user);
        when(jwtService.issueToken(anyString(), anyString(), any(Role.class))).thenReturn(tokens);

        mockMvc.perform(get("/api/auth/login")
                .param("sub", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.isRegistered").value(true))
                .andExpect(jsonPath("$.tokenType").value("Bearer "))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void 사용자가_파라미터없이로그인시_성공() throws Exception {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getName()).thenReturn("Test User");
        when(user.getRole()).thenReturn(Role.USER);

        JwtPair tokens = new JwtPair(List.of("access-token", "refresh-token"));

        when(userService.getUser(any(Optional.class))).thenReturn(user);
        when(jwtService.issueToken(anyString(), anyString(), any(Role.class))).thenReturn(tokens);

        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.isRegistered").value(true));
    }

    @Test
    void 사용자가_토큰재발급시_성공() throws Exception {
        TokenForm tokenForm = new TokenForm("refresh_token", "refresh-token");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");

        when(jwtService.validate(anyString())).thenReturn(claims);
        when(jwtService.issueToken(any(Claims.class), any(GrantType.class))).thenReturn("new-access-token");

        mockMvc.perform(post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(tokenForm)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.tokenType").value("Bearer "))
                .andExpect(jsonPath("$.grantType").value("ACCESS_TOKEN"))
                .andExpect(jsonPath("$.token").value("new-access-token"));
    }

    @Test
    void 사용자가_토큰없이재발급시_실패() throws Exception {
        TokenForm tokenForm = new TokenForm("refresh_token", null);

        mockMvc.perform(post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(tokenForm)))
                .andExpect(status().isBadRequest());
    }
}