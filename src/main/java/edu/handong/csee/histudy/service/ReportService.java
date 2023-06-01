package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
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

    public ReportDto.Info createReport(ReportForm form, String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow();

        List<User> participants = form.getParticipants()
                .stream()
                .map(userRepository::findUserBySid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Course> courses = form.getCourses()
                .stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        Report saved = reportRepository.save(
                form.toEntity(user.getTeam(), participants, courses));

        return new ReportDto.Info(saved);
    }

    public List<ReportDto.Info> getReports(String email) {
        Team team = userRepository.findUserByEmail(email)
                .orElseThrow()
                .getTeam();

        return team.getReports()
                .stream()
                .map(ReportDto.Info::new)
                .toList();
    }

    public List<ReportDto.Info> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(ReportDto.Info::new)
                .toList();
    }

    public boolean updateReport(Long reportId, ReportForm form) {
        List<User> participants = form.getParticipants()
                .stream()
                .map(userRepository::findUserBySid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Course> courses = form.getCourses()
                .stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return reportRepository.findById(reportId)
                .map(report -> report.update(form, participants, courses))
                .orElse(false);
    }

    public Optional<ReportDto.Info> getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .map(ReportDto.Info::new);
    }

    public boolean deleteReport(Long reportId) {
        Optional<Report> reportOr = reportRepository.findById(reportId);

        if (reportOr.isEmpty()) {
            return false;
        } else {
            reportRepository.delete(reportOr.get());
            return true;
        }
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
