package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.service.TeamService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/admin")
public class TeamController {
    private final TeamService teamService;

    @GetMapping("/manageGroup")
    public ResponseEntity<List<TeamDto>> getTeams(@RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.getTeams(claims.getSubject()));
    }
    @DeleteMapping("/group")
    public ResponseEntity<Integer> deleteTeam(@RequestBody TeamIdDto dto, @RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.deleteTeam(dto, claims.getSubject()));
    }
}
