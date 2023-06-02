package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicController {

    private final ReportService reportService;

    @GetMapping("/reports")
    public ReportDto getReportList() {
        List<ReportDto.ReportBasic> reports = reportService.getAllReports();

        return new ReportDto(reports);
    }
}
