package edu.handong.csee.histudy.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.UserController;
import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserControllerTests {

    MockMvc mvc;

    @Autowired
    UserController userController;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    AuthenticationInterceptor interceptor;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void init() throws IOException {
        mvc = MockMvcBuilders
                .standaloneSetup(userController)
                .addInterceptors(interceptor)
                .build();
        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("스터디 신청 내역을 확인할 수 있다")
    @Test
    void UserControllerTests_16() throws Exception {
        // given
        User saved = userRepository.save(User.builder()
                .sid("123")
                .name("test")
                .email("test@example.com")
                .build());
        User friend = userRepository.save(User.builder()
                .sid("234")
                .name("test2")
                .email("test2@example.com")
                .build());
        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());

        userService.apply(
                ApplyForm.builder()
                        .friendIds(List.of(friend.getSid()))
                        .courseIds(List.of(course.getId()))
                        .build(),
                saved.getEmail());

        Claims claims = Jwts.claims(
                Collections.singletonMap("sub", saved.getEmail()));

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/users/me/forms")
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApplyFormDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ApplyFormDto.class);

        // then
        assertEquals(1, res.getCourses().size());
        assertEquals(1, res.getFriends().size());
    }

    @DisplayName("스터디 지원서를 수정할 수 있다")
    @Test
    void UserControllerTests_121() throws Exception {
        // given
        User saved = userRepository.save(User.builder()
                .sid("123")
                .name("test")
                .email("test@example.com")
                .build());
        User friend = userRepository.save(User.builder()
                .sid("234")
                .name("test2")
                .email("test2@example.com")
                .build());
        User friend2 = userRepository.save(User.builder()
                .sid("345")
                .name("newFriend")
                .email("test3@example.com")
                .build());
        Course course = courseRepository.save(Course.builder()
                .name("courseName")
                .build());
        Course course2 = courseRepository.save(Course.builder()
                .name("newCourse")
                .build());

        userService.apply(
                ApplyForm.builder()
                        .friendIds(List.of(friend.getSid()))
                        .courseIds(List.of(course.getId()))
                        .build(),
                saved.getEmail());

        userService.apply(
                ApplyForm.builder()
                        .friendIds(List.of(friend2.getSid()))
                        .courseIds(List.of(course2.getId()))
                        .build(),
                saved.getEmail());

        Claims claims = Jwts.claims(
                Collections.singletonMap("sub", saved.getEmail()));

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/users/me/forms")
                        .requestAttr("claims", claims))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ApplyFormDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ApplyFormDto.class);

        // then
        assertEquals(1, res.getCourses().size());
        assertEquals(1, res.getFriends().size());
        assertEquals(course2.getName(), res.getCourses().get(0).getName());
        assertEquals(friend2.getName(), res.getFriends().get(0).getName());
    }
}
