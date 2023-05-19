package edu.handong.csee.histudy.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.controller.ReportController;
import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.ReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
public class ReportControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ReportRepository reportRepository;

    @MockBean
    UserRepository userRepository;

    @DisplayName("스터디 보고서를 생성한다")
    @Test
    public void ReportControllerTests_21() throws Exception {
        // given
        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .participants(List.of("a12"))
                .build();
        User user = User.builder()
                .id("123")
                .name("user")
                .sid("21800123")
                .build();
        user.belongTo(new Team(1));

        when(userRepository.findUserByAccessToken(any()))
                .thenReturn(Optional.of(user));
        when(userRepository.findUserBySid(any())).thenReturn(Optional.of(user));
        when(reportRepository.save(any())).thenReturn(form.toEntity(user.getTeam(), List.of(Optional.of(user))));

        // when
        MvcResult mvcResult = mvc
                .perform(post("/api/v1/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "access_token")
                        .content(mapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        ReportDto.Response data = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ReportDto.Response.class);

        assertEquals("title", data.getTitle());
        assertEquals(60, data.getTotalMinutes());
        assertEquals("21800123", data.getParticipants().get(0));
    }
}
