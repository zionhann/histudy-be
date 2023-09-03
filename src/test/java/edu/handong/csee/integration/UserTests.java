package edu.handong.csee.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.HistudyApplication;
import edu.handong.csee.histudy.controller.UserController;
import edu.handong.csee.histudy.controller.form.UserForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.repository.UserRepository;
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
    @Autowired
    private UserRepository userRepository;

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
        UserForm form = new UserForm(
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
        userService.signUp(new UserForm(
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
        userService.signUp(new UserForm(
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

    @DisplayName("그룹 신청시 유저 검색: 자기 자신은 제외")
    @Test
    void UserTests_152() throws Exception {
        // Given
        User userA = new User("123", "22300012", "test@example.com", "test", Role.USER);
        User userB = new User("124", "22300013", "test2@example.com", "test2", Role.USER);
        User userC = new User("125", "22300014", "test3@example.com", "test3", Role.USER);
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // When
        MvcResult mvcResult = mvc
                .perform(get("/api/users")
                        .queryParam("search", "22300012")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.class);

        // Then
        assertThat(res.getUsers()).isEmpty();
    }

    @DisplayName("그룹 신청시 유저 검색: 검색어가 없는 경우")
    @Test
    void UserTests_177() throws Exception {
        // Given
        User userA = new User("123", "22300012", "test@example.com", "test", Role.USER);
        User userB = new User("124", "22300013", "test2@example.com", "test2", Role.USER);
        User userC = new User("125", "22300014", "test3@example.com", "test3", Role.USER);
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // When
        MvcResult mvcResult = mvc
                .perform(get("/api/users")
                        .queryParam("search", "")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto res = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.class);

        // Then
        assertThat(res.getUsers().get(0).getSid()).isEqualTo("22300013");
        assertThat(res.getUsers()).hasSize(2);
    }
}
