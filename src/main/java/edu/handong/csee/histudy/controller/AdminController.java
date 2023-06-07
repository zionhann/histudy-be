package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.dto.TeamReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.service.TeamService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final TeamService teamService;
    private final UserService userService;

    @GetMapping("/manageGroup")
    public ResponseEntity<List<TeamDto>> getTeams(@RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.getTeams(claims.getSubject()));
    }

    @DeleteMapping("/group")
    public ResponseEntity<Integer> deleteTeam(@RequestBody TeamIdDto dto, @RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.deleteTeam(dto, claims.getSubject()));
    }

    @GetMapping("/groupReport/{id}")
    public ResponseEntity<TeamReportDto> getTeamReports(@PathVariable(name = "id") long id,
                                                        @RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.getTeamReports(id, claims.getSubject()));
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<UserDto.UserInfo>> getAppliedUsers() {
        return ResponseEntity.ok(userService.getAppliedUsers());
    }

    @PostMapping("/team-match")
    public ResponseEntity<TeamDto.MatchResults> matchTeam() {
        return ResponseEntity.ok(teamService.matchTeam());
    }

    @GetMapping("unmatched-users")
    public ResponseEntity<List<UserDto.UserInfo>> getUnmatchedUsers() {
        return ResponseEntity.ok(userService.getUnmatchedUsers());
    }

    @DeleteMapping("/form")
    public UserDto.UserInfo deleteForm(@RequestParam String sid) {
        return userService.deleteUserForm(sid);
    }
}
