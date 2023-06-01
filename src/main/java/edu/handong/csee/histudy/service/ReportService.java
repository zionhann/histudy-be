package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ImageDto;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.ReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ReportDto.Response createReport(ReportForm form, String accessToken) {
        User user = userRepository.findUserByAccessToken(accessToken).orElseThrow();
        List<Optional<User>> participants = form.getParticipants().stream()
                .map(userRepository::findUserBySid)
                .toList();
        List<Optional<Course>> courses = form.getCourses().stream()
                .map(courseRepository::findById)
                .toList();
        Report report = form.toEntity(user.getTeam(), participants);
        Report saved = reportRepository.save(report);
        List<Study> studies = courses.stream()
                .filter(Optional::isPresent)
                .map(c -> new Study(saved,c.get()))
                .toList();
        saved.setStudies(studies);
        return new ReportDto.Response(saved);
    }
    public ReportDto.Detail getReportDetail(long id, String email) {
        Report report = reportRepository.findById(id).orElseThrow();
        List<UserDto.Basic> users = report.getParticipants()
                .stream()
                .map(Participates::getUser)
                .map(UserDto.Basic::new)
                .toList();
        List<ImageDto> images = report.getImages()
                .stream()
                .map(Image::toDto)
                .toList();
        return ReportDto.Detail.builder()
                .title(report.getTitle())
                .time(report.getTotalMinutes())
                .content(report.getContent())
                .members(users)
                .img(images)
                .build();
    }
}
