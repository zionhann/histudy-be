package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.dto.TeamReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.service.TeamService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 API")
@SecurityRequirement(name = "ADMIN")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final TeamService teamService;
    private final UserService userService;

    @Operation(summary = "매칭된 그룹 목록 조회")
    @GetMapping(value = "/manageGroup")
    public ResponseEntity<List<TeamDto>> getTeams(@RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.getTeams(claims.getSubject()));
    }

    @Operation(summary = "그룹 삭제")
    @DeleteMapping("/group")
    public ResponseEntity<Integer> deleteTeam(@RequestBody TeamIdDto dto, @RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.deleteTeam(dto, claims.getSubject()));
    }

    @Operation(summary = "특정 그룹 보고서 조회")
    @GetMapping("/groupReport/{id}")
    public ResponseEntity<TeamReportDto> getTeamReports(
            @Parameter(description = "그룹 아이디", required = true)
            @PathVariable(name = "id") long id,
            @RequestAttribute Claims claims) {
        return ResponseEntity.ok(teamService.getTeamReports(id, claims.getSubject()));
    }

    @Operation(summary = "신청한 유저 목록 조회")
    @GetMapping("/allUsers")
    public ResponseEntity<List<UserDto.UserInfo>> getAppliedUsers() {
        return ResponseEntity.ok(userService.getAppliedUsers());
    }

    @Operation(summary = "그룹 매칭")
    @PostMapping("/team-match")
    public ResponseEntity<TeamDto.MatchResults> matchTeam() {
        return ResponseEntity.ok(teamService.matchTeam());
    }

    @Operation(summary = "매칭되지 않은 유저 목록 조회")
    @GetMapping("/unmatched-users")
    public ResponseEntity<List<UserDto.UserInfo>> getUnmatchedUsers() {
        return ResponseEntity.ok(userService.getUnmatchedUsers());
    }

    @Operation(summary = "특정 유저 지원폼 삭제")
    @DeleteMapping("/form")
    public UserDto.UserInfo deleteForm(@RequestParam String sid) {
        return userService.deleteUserForm(sid);
    }

    @Operation(summary = "유저 정보 수정")
    @PostMapping("/edit-user")
    public UserDto.UserInfo editUser(@RequestBody UserDto.UserEdit dto) {
        return userService.editUser(dto);
    }
}
