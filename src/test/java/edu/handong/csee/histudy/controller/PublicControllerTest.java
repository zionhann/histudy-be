package edu.handong.csee.histudy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.handong.csee.histudy.dto.ActivityMetricsDto;
import edu.handong.csee.histudy.dto.TeamRankDto;
import edu.handong.csee.histudy.jwt.JwtProperties;
import edu.handong.csee.histudy.service.ActivityMetricsService;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.TeamService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({PublicController.class, ExceptionController.class})
class PublicControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TeamService teamService;

  @MockBean private ActivityMetricsService activityMetricsService;

  @MockBean private JwtService jwtService;

  @MockBean private JwtProperties jwtProperties;

  @Test
  void 공개그룹목록조회시_성공() throws Exception {
    TeamRankDto teamRankDto = new TeamRankDto(List.of());
    when(teamService.getAllTeams()).thenReturn(teamRankDto);

    mockMvc
        .perform(get("/api/public/teams"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"));
  }

  @Test
  void 활동지표조회_전체_성공() throws Exception {
    ActivityMetricsDto expectedDto =
        ActivityMetricsDto.builder()
            .studyMembers(100)
            .studyGroups(50)
            .studyHours(200)
            .reports(300)
            .build();
    when(activityMetricsService.getActivityMetrics("all")).thenReturn(expectedDto);

    mockMvc
        .perform(get("/api/public/activity"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.studyMembers").value(100))
        .andExpect(jsonPath("$.studyGroups").value(50))
        .andExpect(jsonPath("$.studyHours").value(200))
        .andExpect(jsonPath("$.reports").value(300));
  }

  @Test
  void 활동지표조회_현재학기_성공() throws Exception {
    ActivityMetricsDto expectedDto =
        ActivityMetricsDto.builder()
            .studyMembers(20)
            .studyGroups(10)
            .studyHours(50)
            .reports(80)
            .build();
    when(activityMetricsService.getActivityMetrics("current")).thenReturn(expectedDto);

    mockMvc
        .perform(get("/api/public/activity").param("term", "current"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.studyMembers").value(20))
        .andExpect(jsonPath("$.studyGroups").value(10))
        .andExpect(jsonPath("$.studyHours").value(50))
        .andExpect(jsonPath("$.reports").value(80));
  }
}
