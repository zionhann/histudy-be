package edu.handong.csee.histudy.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.ExceptionController;
import edu.handong.csee.histudy.controller.UserController;
import edu.handong.csee.histudy.controller.form.UserForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.UserAlreadyExistsException;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@MockBeans({
        @MockBean(UserService.class),
        @MockBean(JwtService.class),
        @MockBean(AuthenticationInterceptor.class)
})
@WebMvcTest(UserController.class)
public class UserControllerMockTests {

    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserService userService;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserController userController;

    @Autowired
    AuthenticationInterceptor interceptor;

    @BeforeEach
    void init() throws IOException {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .addInterceptors(interceptor)
                .setControllerAdvice(ExceptionController.class)
                .build();
        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("스터디 친구를 검색한다: 이름")
    @Test
    void UserControllerTests_196() throws Exception {
        // given
        when(userService.search(any()))
                .thenReturn(List.of(
                        User.builder()
                                .name("username")
                                .sid("21800123")
                                .email("test@example.com")
                                .role(Role.USER)
                                .build()));

        when(jwtService.validate(any()))
                .thenReturn(Jwts.claims(
                        Collections.singletonMap("sub", "test2@example.com")
                ));

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/api/users")
                        .queryParam("search", "username"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        UserDto res = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.class);

        // then
        assertEquals(1, res.getUsers().size());
        assertEquals("username", res.getUsers().get(0).getName());
        assertEquals("21800123", res.getUsers().get(0).getSid());
        assertEquals("test@example.com", res.getUsers().get(0).getEmail());
    }

    @DisplayName("스터디 친구를 검색한다: 학번")
    @Test
    void UserControllerTests_229() throws Exception {
        // given
        when(userService.search(any()))
                .thenReturn(List.of(
                        User.builder()
                                .name("username")
                                .sid("21800123")
                                .email("test@example.com")
                                .role(Role.USER)
                                .build()));

        when(jwtService.validate(any()))
                .thenReturn(Jwts.claims(
                        Collections.singletonMap("sub", "test2@example.com")
                ));

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/api/users")
                        .queryParam("search", "218"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        UserDto res = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.class);

        //then
        assertEquals(1, res.getUsers().size());
        assertEquals("username", res.getUsers().get(0).getName());
        assertEquals("21800123", res.getUsers().get(0).getSid());
    }

    @DisplayName("신규 유저는 회원가입시 토큰을 받아야 한다.")
    @Test
    void UserControllerTests_254() throws Exception {
        // given
        UserForm userForm = new UserForm("1234", "username", "user@test", "21800123");
        List<String> tokens = List.of("access_token", "refresh_token");
        String form = mapper.writeValueAsString(userForm);

        // when
        when(jwtService.issueToken(any(), any(), any())).thenReturn(new JwtPair(tokens));

        MvcResult mvcResult = mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        UserDto.UserLogin res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserDto.UserLogin.class);

        // then
        assertTrue(res.getIsRegistered());
        assertEquals("Bearer ", res.getTokenType());
        assertEquals("access_token", res.getTokens().getAccessToken());
    }

    @DisplayName("스터디 친구를 검색한다: 이메일")
    @Test
    void UserControllerTests_260() throws Exception {
        // given
        when(userService.search(any()))
                .thenReturn(List.of(
                        User.builder()
                                .name("username")
                                .sid("21800123")
                                .email("test@example.com")
                                .role(Role.USER)
                                .build()));

        when(jwtService.validate(any()))
                .thenReturn(Jwts.claims(
                        Collections.singletonMap("sub", "test2@example.com")
                ));

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/api/users")
                        .queryParam("search", "test"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        UserDto res = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.class);

        // then
        assertEquals(1, res.getUsers().size());
        assertEquals("username", res.getUsers().get(0).getName());
        assertEquals("21800123", res.getUsers().get(0).getSid());
        assertEquals("test@example.com", res.getUsers().get(0).getEmail());
    }

    @DisplayName("기존 유저는 회원가입 요청이 거부되어야 한다.")
    @Test
    void UserControllerTests_206() throws Exception {
        UserForm userForm = new UserForm("1234", "username", "user@test", "21800123");
        String form = mapper.writeValueAsString(userForm);

        doThrow(UserAlreadyExistsException.class)
                .when(userService)
                .signUp(any());

        mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

    @DisplayName("자기 자신은 목록에 없어야 한다")
    @Test
    void UserControllerMockTests_240() throws Exception {
        // given
        when(userService.search(any()))
                .thenReturn(List.of(
                        User.builder()
                                .name("username")
                                .sid("21800123")
                                .email("test@example.com")
                                .role(Role.USER)
                                .build()));

        when(jwtService.validate(any()))
                .thenReturn(Jwts.claims(
                        Collections.singletonMap("sub", "test@example.com")
                ));

        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/api/users")
                        .queryParam("search", "test"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        UserDto res = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDto.class);

        // then
        assertTrue(res.getUsers().isEmpty());
    }
}
