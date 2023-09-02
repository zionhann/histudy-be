package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.exception.ReportNotFoundException;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.GroupCourseRepository;
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
    private final GroupCourseRepository groupCourseRepository;

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

        // filter groupCourses by form.getCourses()
        List<GroupCourse> groupCourses = groupCourseRepository
                .findAllByStudyGroup(user.getStudyGroup());
        groupCourses.removeIf(gc -> !courses.contains(gc.getCourse()));

        GroupReport saved = groupReportRepository.save(
                form.toEntity(user.getStudyGroup(), participants, groupCourses));

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

        GroupReport targetReport = groupReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // filter groupCourses by form.getCourses()
        List<GroupCourse> groupCourses = groupCourseRepository
                .findAllByStudyGroup(targetReport.getStudyGroup());
        groupCourses.removeIf(gc -> !courses.contains(gc.getCourse()));
        targetReport.update(form, participants, groupCourses);

        return true;
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
