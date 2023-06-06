package edu.handong.csee.histudy.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.TeamController;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.CourseDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
public class TeamControllerTests {

    MockMvc mvc;

    @Autowired
    TeamController teamController;

    @MockBean
    AuthenticationInterceptor interceptor;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void init() throws IOException {
        mvc = MockMvcBuilders
                .standaloneSetup(teamController)
                .addInterceptors(interceptor)
                .build();

        when(interceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("팀(그룹)의 스터디 강의 목록을 가져온다")
    @Test
    void CourseControllerTests_142() throws Exception {
        // given

        User userA = User.builder()
                .sid("201511111")
                .email("userA@test.com")
                .build();
        User userB = User.builder()
                .sid("201611111")
                .email("userB@test.com")
                .build();
        Course courseA = Course.builder()
                .name("courseA")
                .build();
        Course courseB = Course.builder()
                .name("courseB")
                .build();

        User save1 = userRepository.save(userA);
        User save2 = userRepository.save(userB);
        Course savedCourse1 = courseRepository.save(courseA);
        Course savedCourse2 = courseRepository.save(courseB);

        Claims claimsA = Jwts.claims();
        claimsA.put("sub", userA.getEmail());

        Claims claimsB = Jwts.claims();
        claimsB.put("sub", userB.getEmail());

        Team team = new Team(1);

        save1.belongTo(team);
        save2.belongTo(team);

        save2.getTeam()
                .select(List.of(savedCourse1, savedCourse2));
        // when
        MvcResult mvcResult = mvc
                .perform(get("/api/team/courses")
                        .requestAttr("claims", claimsA))
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
