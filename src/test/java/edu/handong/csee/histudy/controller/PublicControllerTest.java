package edu.handong.csee.histudy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.handong.csee.histudy.dto.TeamRankDto;
import edu.handong.csee.histudy.jwt.JwtProperties;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.TeamService;
import edu.handong.csee.histudy.controller.ExceptionController;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({PublicController.class, ExceptionController.class})
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    void 공개그룹목록조회시_성공() throws Exception {
        TeamRankDto teamRankDto = new TeamRankDto(List.of());
        when(teamService.getAllTeams()).thenReturn(teamRankDto);

        mockMvc.perform(get("/api/public/teams"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }
}