package edu.handong.csee.histudy.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.ApplyFormController;
import edu.handong.csee.histudy.controller.CourseController;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.repository.CourseRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class CourseControllerTests {

    MockMvc mvc;

    @Autowired
    CourseController courseController;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ApplyFormController applyFormController;

    @MockBean
    AuthenticationInterceptor interceptor;

    @BeforeEach
    void setup() throws Exception {
        mvc = MockMvcBuilders
                .standaloneSetup(courseController)
                .addInterceptors(interceptor)
                .build();

        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("강의를 이름으로 검색한다")
    @Test
    void CourseControllerTests_19() throws Exception {
        // given
        Course c = Course.builder()
                .name("Software Engineering")
                .professor("JC")
                .code("ITP40002")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(c);

        Claims claims = Jwts.claims();
        claims.put("rol", Role.USER.name());

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/courses")
                        .queryParam("search", "soft")
                        .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        CourseDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CourseDto.class);
        // then
        assertEquals(1, res.getCourses().size());
        assertEquals("Software Engineering", res.getCourses().get(0).getName());
    }

    @DisplayName("해당하는 목록이 없는 경우 빈 배열을 응답한다")
    @Test
    void CourseControllerTests_88() throws Exception {
        Claims claims = Jwts.claims();
        claims.put("rol", Role.USER.name());

        MvcResult mvcResult = mvc
                .perform(get("/api/courses")
                        .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        CourseDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CourseDto.class);
        // then
        assertTrue(res.getCourses().isEmpty());
    }

    @DisplayName("검색 키워드가 주어지지 않은 경우 전체 목록을 가져온다")
    @Test
    void CourseControllerTests_105() throws Exception {
        // given
        Course c1 = Course.builder()
                .name("Software Engineering")
                .professor("JC")
                .code("ITP40002")
                .courseYear(2023)
                .semester(1)
                .build();
        Course c2 = Course.builder()
                .name("Java Programming")
                .professor("JC")
                .code("ITP20003")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(c1);
        courseRepository.save(c2);

        Claims claims = Jwts.claims();
        claims.put("rol", Role.USER.name());

        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/courses")
                        .requestAttr("claims", claims))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        CourseDto res = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CourseDto.class);
        // then
        assertEquals(2, res.getCourses().size());
    }
}
