package edu.handong.csee.histudy.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.ApplyFormController;
import edu.handong.csee.histudy.controller.ExceptionController;
import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class AppControllerTests {

    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ApplyFormController applyFormController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseRepository courseRepository;

    @MockBean
    AuthenticationInterceptor interceptor;

    @BeforeEach
    void init() throws Exception {
        mvc = MockMvcBuilders
                .standaloneSetup(applyFormController)
                .addInterceptors(interceptor)
                .setControllerAdvice(ExceptionController.class)
                .build();
        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("유효한 신청폼을 작성하여 스터디를 신청한다")
    @Test
    void AppControllerTests_26() throws Exception {
        // given
        User saved = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .role(Role.USER)
                        .build());

        courseRepository.save(
                Course.builder()
                        .name("test1")
                        .build());
        courseRepository.save(
                Course.builder()
                        .name("test2")
                        .build());

        ApplyForm applyForm = ApplyForm.builder()
                .friendIds(List.of("sidA", "sidB"))
                .courseIds(List.of(1L, 2L))
                .build();
        String form = mapper.writeValueAsString(applyForm);

        Claims claims = Jwts.claims();
        claims.put("sub", saved.getEmail());
        claims.put("rol", saved.getRole().name());

        // when
        mvc.perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @DisplayName("유효하지 않은 신청폼: 유효하지 않는 작성자")
    @Test
    void AppControllerTests_104() throws Exception {
        // given
        userRepository.save(
                User.builder()
                        .email("subA")
                        .build());

        courseRepository.save(
                Course.builder()
                        .name("test1")
                        .build());

        ApplyForm applyForm = ApplyForm.builder()
                .friendIds(List.of("sidA", "sidB"))
                .courseIds(List.of(1L, 2L))
                .build();
        String form = mapper.writeValueAsString(applyForm);

        Claims claims = Jwts.claims();
        claims.put("sub", "subB");
        claims.put("rol", Role.USER.name());

        // when
        mvc.perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
    }

    @DisplayName("유효하지 않은 신청폼: 강의 목록이 없음")
    @Test
    void AppControllerTests_137() throws Exception {
        // given
        userRepository.save(
                User.builder()
                        .sub("subA")
                        .role(Role.USER)
                        .build());

        ApplyForm applyForm = ApplyForm.builder()
                .friendIds(List.of("sidA", "sidB"))
                .courseIds(List.of(1L, 2L))
                .build();
        String form = mapper.writeValueAsString(applyForm);

        Claims claims = Jwts.claims();
        claims.put("sub", "subA");
        claims.put("rol", Role.USER.name());

        // when
        mvc
                .perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
    }

    @DisplayName("신청폼을 업데이트 할 수 있다")
    @Test
    void AppControllerTests_166() throws Exception {
        // given
        User saved = userRepository.save(
                User.builder()
                        .sid("sidA")
                        .email("test@example.com")
                        .role(Role.USER)
                        .build());
        userRepository.save(
                User.builder()
                        .sid("sidB")
                        .email("test2@example.com")
                        .build());
        userRepository.save(
                User.builder()
                        .sid("sidC")
                        .email("test3@example.com")
                        .build());
        Course course1 = courseRepository.save(
                Course.builder()
                        .name("test1")
                        .build());
        Course course2 = courseRepository.save(
                Course.builder()
                        .name("test2")
                        .build());

        ApplyForm applyForm = ApplyForm.builder()
                .friendIds(List.of("sidB", "sidC"))
                .courseIds(List.of(course1.getId(), course2.getId()))
                .build();
        String form = mapper.writeValueAsString(applyForm);

        Claims claims = Jwts.claims();
        claims.put("sub", saved.getEmail());
        claims.put("rol", saved.getRole().name());

        // when
        mvc.perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ApplyForm applyForm2 = ApplyForm.builder()
                .friendIds(List.of("sidB"))
                .courseIds(List.of(course1.getId()))
                .build();
        String updatedForm = mapper.writeValueAsString(applyForm2);

        MvcResult mvcResult = mvc.perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedForm))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ApplyFormDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ApplyFormDto.class);

        assertThat(res.getCourses()).hasSize(1);
        assertThat(res.getFriends()).hasSize(1);
        assertThat(res.getCourses().get(0).getName()).isEqualTo(course1.getName());
        assertThat(res.getFriends().get(0).getSid()).isEqualTo("sidB");
    }
}
