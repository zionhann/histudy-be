package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.service.ReportService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

    private final ReportService reportService;

    @Parameters({
            @Parameter(in = ParameterIn.HEADER, name = "Authorization",
                    required = true, example = "Bearer access_token")
    })
    @PostMapping("/reports")
    public ReportDto.Info createReport(@RequestBody ReportForm form,
                                       @RequestAttribute Claims claims) {
        return reportService.createReport(form, claims.getSubject());
    }

    @Parameter(in = ParameterIn.HEADER, name = "Authorization",
            example = "Bearer access_token", required = true)
    @GetMapping("/reports")
    public ReportDto getMyGroupReports(@RequestAttribute Claims claims) {
        List<ReportDto.Basic> reports = reportService.getReports(claims.getSubject());

        return new ReportDto(reports);
    }

    @Parameter(in = ParameterIn.HEADER, name = "Authorization",
            example = "Bearer access_token", required = true)
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportDto.Info> getReport(@PathVariable Long reportId) {
        Optional<ReportDto.Info> reportsOr = reportService.getReport(reportId);

        return reportsOr
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Parameters({
            @Parameter(name = "Authorization", in = ParameterIn.HEADER, example = "Bearer access_token"),
            @Parameter(name = "reportId", in = ParameterIn.PATH, example = "1")
    })
    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<String> updateReport(
            @PathVariable Long reportId,
            @RequestBody ReportForm form) {
        return (reportService.updateReport(reportId, form))
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @Parameters({
            @Parameter(name = "Authorization", in = ParameterIn.HEADER, example = "Bearer access_token"),
            @Parameter(name = "reportId", in = ParameterIn.PATH, example = "1")
    })
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<String> deleteReport(@PathVariable Long reportId) {
        return (reportService.deleteReport(reportId))
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
