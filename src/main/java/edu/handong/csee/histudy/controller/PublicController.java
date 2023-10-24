package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.TeamRankDto;
import edu.handong.csee.histudy.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공개 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicController {

  private final TeamService teamService;

  @Operation(summary = "그룹 목록 조회")
  @GetMapping("/teams")
  public TeamRankDto getTeams() {
    return teamService.getAllTeams();
  }
}
