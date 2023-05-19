package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.ReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @PostMapping("/api/v1/report")
    public ReportDto.Response createReport(@RequestBody ReportForm form,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken) {
        User user = userRepository.findUserByAccessToken(accessToken).orElseThrow();
        List<Optional<User>> participants = form.getParticipants().stream()
                .map(userRepository::findUserBySid)
                .toList();
        Report report = form.toEntity(user.getGroup(), participants);

        return new ReportDto.Response(reportRepository.save(report));
    }
}
