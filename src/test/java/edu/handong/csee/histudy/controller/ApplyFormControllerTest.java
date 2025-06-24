package edu.handong.csee.histudy.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.ApplyFormV2;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.interceptor.AuthenticationInterceptor;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(ApplyFormController.class)
class ApplyFormControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private AuthenticationInterceptor authenticationInterceptor;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ApplyFormController(userService))
                .setControllerAdvice(ExceptionController.class)
                .addInterceptors(authenticationInterceptor)
                .build();
    }

    @Test
    void 사용자가_스터디신청시_성공_V1() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        ApplyForm form = ApplyForm.builder()
                .friendIds(List.of("22500101"))
                .courseIds(List.of(1L))
                .build();

        ApplyFormDto result = mock(ApplyFormDto.class);
        when(userService.apply(any(ApplyForm.class), anyString())).thenReturn(result);

        mockMvc.perform(post("/api/forms")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 사용자가_스터디신청시_성공_V2() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.USER.name());

        ApplyFormV2 form = ApplyFormV2.builder()
                .friendIds(List.of(1L, 2L))
                .courseIds(List.of(1L, 2L))
                .build();

        StudyApplicant applicant = mock(StudyApplicant.class);
        when(applicant.getPartnerRequests()).thenReturn(List.of());
        when(applicant.getPreferredCourses()).thenReturn(List.of());

        when(userService.apply(anyList(), anyList(), anyString())).thenReturn(applicant);

        mockMvc.perform(post("/api/v2/forms")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void 권한없는사용자가_스터디신청시_실패_V1() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        ApplyForm form = ApplyForm.builder()
                .friendIds(List.of())
                .courseIds(List.of())
                .build();

        mockMvc.perform(post("/api/forms")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 권한없는사용자가_스터디신청시_실패_V2() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("rol", String.class)).thenReturn(Role.MEMBER.name());

        ApplyFormV2 form = ApplyFormV2.builder()
                .friendIds(List.of())
                .courseIds(List.of())
                .build();

        mockMvc.perform(post("/api/v2/forms")
                .requestAttr("claims", claims)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isForbidden());
    }
}