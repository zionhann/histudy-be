package edu.handong.csee.histudy.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .id("1234")
                .email("test@example.com")
                .name("username")
                .build();
        userRepository.save(user);

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/auth/login")
                        .queryParam("sub", "1234"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto.Login res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserDto.Login.class);

        // then
        assertNotNull(res);
        assertEquals("Bearer ", res.getTokenType());
        assertTrue(res.getIsRegistered());
    }
}
