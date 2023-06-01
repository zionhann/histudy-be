package edu.handong.csee.histudy.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.ApplyFormController;
import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.User;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .build();
        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("유효한 신청폼을 작성하여 스터디를 신청한다")
    @Test
    void AppControllerTests_26() throws Exception {
        // given
        userRepository.save(
                User.builder()
                        .id("subA")
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
        claims.put("sub", "subA");

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
                        .id("subA")
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

        // when
        mvc.perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
    }

    @DisplayName("유효하지 않은 신청폼: 강의 목록이 없음")
    @Test
    void AppControllerTests_137() throws Exception {
        // given
        userRepository.save(
                User.builder()
                        .id("subA")
                        .build());

        ApplyForm applyForm = ApplyForm.builder()
                .friendIds(List.of("sidA", "sidB"))
                .courseIds(List.of(1L, 2L))
                .build();
        String form = mapper.writeValueAsString(applyForm);

        Claims claims = Jwts.claims();
        claims.put("sub", "subA");

        // when
        mvc
                .perform(post("/api/forms")
                        .requestAttr("claims", claims)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(form))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
    }
}
