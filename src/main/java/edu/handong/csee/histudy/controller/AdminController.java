package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.dto.TeamReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.ForbiddenException;
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

    @Operation(summary = "그룹별 활동 조회")
    @GetMapping(value = "/manageGroup")
    public ResponseEntity<List<TeamDto>> getTeams(@RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            String email = claims.getSubject();
            return ResponseEntity.ok(teamService.getTeams(email));
        }
        throw new ForbiddenException();
    }

    @Deprecated
    @Operation(summary = "그룹 삭제")
    @DeleteMapping("/group")
    public ResponseEntity<Integer> deleteTeam(
            @RequestBody TeamIdDto dto,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return ResponseEntity.ok(teamService.deleteTeam(dto, claims.getSubject()));
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "특정 그룹 보고서 조회")
    @GetMapping("/groupReport/{id}")
    public ResponseEntity<TeamReportDto> getTeamReports(
            @Parameter(description = "그룹 아이디", required = true)
            @PathVariable(name = "id") long id,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return ResponseEntity.ok(teamService.getTeamReports(id, claims.getSubject()));
        }
        throw new ForbiddenException();
    }

    /**
     * 스터디 신청한 유저 목록 조회(신청O 그룹?)
     *
     * <p>그룹 배정 여부와 관계 없이
     * 스터디를 신청한 유저 목록을 표시한다</p>
     *
     * @param claims 토큰 페이로드
     * @return 스터디 신청한 유저 목록
     */
    @Operation(summary = "그룹 배정 여부와 관계 없이 스터디 신청한 유저 목록 조회")
    @GetMapping("/allUsers")
    public ResponseEntity<List<UserDto.UserInfo>> getAppliedUsers(@RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return ResponseEntity.ok(userService.getAppliedUsers());
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 매칭")
    @PostMapping("/team-match")
    public ResponseEntity<TeamDto.MatchResults> matchTeam(@RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return ResponseEntity.ok(teamService.matchTeam());
        }
        throw new ForbiddenException();
    }

    /**
     * 그룹 미배정 학생 목록 조회(신청? 그룹X)
     *
     * <p>스터디 신청 여부와 관계 없이
     * 가입된 유저 중에서 그룹이 배정되지 않은 유저 목록을 표시한다</p>
     *
     * @param claims 토큰 페이로드
     * @return 그룹 미배정 학생 목록
     */
    @Operation(summary = "매칭되지 않은 유저 목록 조회")
    @GetMapping("/unmatched-users")
    public ResponseEntity<List<UserDto.UserInfo>> getUnmatchedUsers(@RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return ResponseEntity.ok(userService.getUnmatchedUsers());
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "특정 유저 지원폼 삭제")
    @DeleteMapping("/form")
    public UserDto.UserInfo deleteForm(
            @RequestParam String sid,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return userService.deleteUserForm(sid);
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "유저 정보 수정")
    @PostMapping("/edit-user")
    public UserDto.UserInfo editUser(
            @RequestBody UserDto.UserEdit form,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return userService.editUser(form);
        }
        throw new ForbiddenException();
    }

    /**
     * 스터디 신청한 유저 목록 조회(신청O 그룹X)
     *
     * <p>스터디를 신청했으나
     * 그룹이 배정되지 않은 유저 목록을 조회한다
     * 이 목록은 그룹 매칭 대상자 목록과 같다</p>
     *
     * @param claims 토큰 페이로드
     * @return 스터디 신청했으나 그룹이 배정되지 않은 유저 목록
     */
    @Operation(summary = "스터디를 신청했으나 그룹이 배정되지 않은 유저 목록 조회")
    @GetMapping("/users/unassigned")
    public ResponseEntity<List<UserDto.UserInfo>> unassignedUser(@RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.ADMIN)) {
            return ResponseEntity.ok(userService.getAppliedWithoutGroup());
        }
        throw new ForbiddenException();
    }
}
