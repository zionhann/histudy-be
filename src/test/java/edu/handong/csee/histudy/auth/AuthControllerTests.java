package edu.handong.csee.histudy.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.form.TokenForm;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.jwt.GrantType;
import edu.handong.csee.histudy.jwt.TokenInfo;
import edu.handong.csee.histudy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class AuthControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper mapper;

    @DisplayName("유저 정보가 존재하지 않는 경우: 404 Not Found")
    @Test
    void AuthControllerTests_17() throws Exception {
        mvc
                .perform(get("/api/auth/login")
                        .queryParam("sub", "1234"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("유저 정보가 존재하는 경우: 200 OK with Tokens")
    @Test
    void AuthControllerTests_29() throws Exception {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("username")
                .sub("1234")
                .build();
        userRepository.save(user);

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/auth/login")
                        .queryParam("sub", "1234"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto.UserLogin res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserDto.UserLogin.class);

        // then
        assertNotNull(res);
        assertEquals("Bearer ", res.getTokenType());
        assertTrue(res.getIsRegistered());
    }

    @DisplayName("토큰 재발급: 유효하지 않은 Refresh token")
    @Test
    void AuthControllerTests_76() throws Exception {
        // given
        String invalidJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        TokenForm tokenForm = new TokenForm(GrantType.REFRESH_TOKEN.name(), invalidJwt);
        String form = mapper.writeValueAsString(tokenForm);

        // when
        mvc
                .perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @DisplayName("토큰 재발급: 페이로드가 없음")
    @Test
    void AuthControllerTests_86() throws Exception {
        mvc
                .perform(post("/api/auth/token"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("토큰 재발급: 유효한 요청")
    @Test
    void AuthControllerTests_96() throws Exception {
        // given
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwibmFtZSI6InVzZXIifQ." +
                "ZB9yuKUvuufMfbXkzB647PoSCw-3rc2FA-PoHzKLYgM";
        TokenForm tokenForm = new TokenForm(GrantType.REFRESH_TOKEN.name(), jwt);
        String form = mapper.writeValueAsString(tokenForm);

        // when
        MvcResult mvcResult = mvc
                .perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        TokenInfo res = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                TokenInfo.class);

        // then
        assertEquals(GrantType.ACCESS_TOKEN, res.getGrantType());
    }
}
