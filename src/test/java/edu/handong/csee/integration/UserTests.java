package edu.handong.csee.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.HistudyApplication;
import edu.handong.csee.histudy.controller.UserController;
import edu.handong.csee.histudy.controller.form.UserInfo;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HistudyApplication.class)
@AutoConfigureMockMvc
@Transactional
public class UserTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserController userController;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${custom.jwt.secret.user}")
    String userToken;

    @DisplayName("로그인시 가입여부 확인: 신규 가입")
    @Test
    void UserTests_23() throws Exception {
        // Given

        // When
        MvcResult mvcResult = mvc
                .perform(get("/api/auth/login")
                        .queryParam("sub", "123"))
                .andExpect(status().isNotFound())
                .andReturn();

        UserDto.UserLogin res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.UserLogin.class);

        // Then
        assertThat(res.getIsRegistered()).isFalse();
        assertThat(res.getRole()).isNull();
        assertThat(res.getTokenType()).isNull();
        assertThat(res.getTokens()).isNull();
    }

    @DisplayName("신규 가입인 경우 회원가입")
    @Test
    void UserTests_63() throws Exception {
        // Given
        UserInfo form = new UserInfo(
                "123",
                "test",
                "test@example.com",
                "22300012");
        // When
        MvcResult mvcResult = mvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andReturn();

        UserDto.UserLogin res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.UserLogin.class);

        // Then
        assertThat(res.getIsRegistered()).isTrue();
        assertThat(res.getRole()).isEqualTo("USER");
        assertThat(res.getTokenType()).isEqualTo("Bearer ");
        assertThat(res.getTokens()).isNotNull();
    }

    @DisplayName("로그인시 가입 여부 확인: 기존 유저")
    @Test
    void UserTests_58() throws Exception {
        // Given
        userService.signUp(new UserInfo(
                "123",
                "test",
                "test@example.com",
                "22300012"
        ));

        // When
        MvcResult mvcResult = mvc
                .perform(get("/api/auth/login")
                        .queryParam("sub", "123"))
                .andExpect(status().isOk())
                .andReturn();

        UserDto.UserLogin res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.UserLogin.class);

        // Then
        assertThat(res.getIsRegistered()).isTrue();
        assertThat(res.getRole()).isEqualTo("USER");
        assertThat(res.getTokenType()).isEqualTo("Bearer ");
        assertThat(res.getTokens()).isNotNull();
    }

    @DisplayName("로그인 이후 내 정보 조회")
    @Test
    void UserTests_120() throws Exception {
        // Given
        userService.signUp(new UserInfo(
                "123",
                "test",
                "test@example.com",
                "22300012"
        ));

        // When
        MvcResult mvcResult = mvc
                .perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto.UserMe res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.UserMe.class);

        // Then
        assertThat(res.getEmail()).isEqualTo("test@example.com");
        assertThat(res.getName()).isEqualTo("test");
        assertThat(res.getSid()).isEqualTo("22300012");
    }
}
