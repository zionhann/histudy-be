package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.service.ReportService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/api/v1/report")
    public ReportDto.Response createReportCourse(@RequestBody ReportForm form,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken) {
        return reportService.createReport(form,accessToken);
    }
    @GetMapping("/api/report/detailReport/{id}")
    public ReportDto.Detail getDetailReport(@PathVariable long id,
                                            @RequestAttribute Claims claims) {
        return reportService.getReportDetail(id,claims.getSubject());
    }
}
