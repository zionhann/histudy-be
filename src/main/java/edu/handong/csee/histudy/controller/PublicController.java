package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.TeamRankDto;
import edu.handong.csee.histudy.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicController {

    private final TeamService teamService;

    @GetMapping("/teams")
    public TeamRankDto getTeams() {
        return teamService.getAllTeams();
    }
}
