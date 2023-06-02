package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.ReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ReportDto.ReportInfo createReport(ReportForm form, String email) {
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

        return new ReportDto.ReportInfo(saved);
    }

    public List<ReportDto.ReportBasic> getReports(String email) {
        Team team = userRepository.findUserByEmail(email)
                .orElseThrow()
                .getTeam();

        return team.getReports()
                .stream()
                .map(ReportDto.ReportBasic::new)
                .toList();
    }

    public List<ReportDto.ReportBasic> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(ReportDto.ReportBasic::new)
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

    public Optional<ReportDto.ReportInfo> getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .map(ReportDto.ReportInfo::new);
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
}
