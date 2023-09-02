package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.GroupReport;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.GroupReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final GroupReportRepository groupReportRepository;
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

        GroupReport saved = groupReportRepository.save(
                form.toEntity(user.getStudyGroup(), participants, courses));

        return new ReportDto.ReportInfo(saved);
    }

    public List<ReportDto.ReportInfo> getReports(String email) {
        StudyGroup studyGroup = userRepository.findUserByEmail(email)
                .orElseThrow()
                .getStudyGroup();

        return studyGroup.getReports()
                .stream()
                .map(ReportDto.ReportInfo::new)
                .toList();
    }

    public List<ReportDto.ReportInfo> getAllReports() {
        return groupReportRepository.findAll()
                .stream()
                .map(ReportDto.ReportInfo::new)
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

        return groupReportRepository.findById(reportId)
                .map(report -> report.update(form, participants, courses))
                .orElse(false);
    }

    public Optional<ReportDto.ReportInfo> getReport(Long reportId) {
        return groupReportRepository.findById(reportId)
                .map(ReportDto.ReportInfo::new);
    }

    public boolean deleteReport(Long reportId) {
        Optional<GroupReport> reportOr = groupReportRepository.findById(reportId);

        if (reportOr.isEmpty()) {
            return false;
        } else {
            groupReportRepository.delete(reportOr.get());
            return true;
        }
    }
}
