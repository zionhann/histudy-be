package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.service.CourseService;
import edu.handong.csee.histudy.service.ReportService;
import edu.handong.csee.histudy.service.TeamService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "스터디 그룹 API")
@SecurityRequirement(name = "MEMBER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

    private final ReportService reportService;
    private final CourseService courseService;
    private final TeamService teamService;

    @Operation(summary = "그룹 스터디 보고서 생성")
    @PostMapping("/reports")
    public ReportDto.ReportInfo createReport(
            @RequestBody ReportForm form,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            return reportService.createReport(form, claims.getSubject());
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 보고서 목록 조회")
    @GetMapping("/reports")
    public ReportDto getMyGroupReports(
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            List<ReportDto.ReportInfo> reports = reportService.getReports(claims.getSubject());
            return new ReportDto(reports);
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 특정 보고서 조회")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportDto.ReportInfo> getReport(
            @PathVariable Long reportId,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            Optional<ReportDto.ReportInfo> reportsOr = reportService.getReport(reportId);
            return reportsOr
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 특정 보고서 수정")
    @Parameter(name = "reportId", in = ParameterIn.PATH, example = "1")
    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<String> updateReport(
            @PathVariable Long reportId,
            @RequestBody ReportForm form,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            return (reportService.updateReport(reportId, form))
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 특정 보고서 삭제")
    @Parameter(name = "reportId", in = ParameterIn.PATH, example = "1")
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<String> deleteReport(
            @PathVariable Long reportId,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            return (reportService.deleteReport(reportId))
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 선택 강의 목록 조회")
    @GetMapping("/courses")
    public ResponseEntity<CourseDto> getTeamCourses(
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            return ResponseEntity.ok(
                    new CourseDto(
                            courseService.getTeamCourses(claims.getSubject())));
        }
        throw new ForbiddenException();
    }

    @Operation(summary = "그룹 팀원 목록 조회")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto.UserMe>> getTeamUsers(
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.MEMBER)) {
            return ResponseEntity.ok(teamService.getTeamUsers(claims.getSubject()));
        }
        throw new ForbiddenException();
    }
}
