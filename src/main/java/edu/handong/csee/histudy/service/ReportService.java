package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.exception.ReportNotFoundException;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.GroupCourseRepository;
import edu.handong.csee.histudy.repository.GroupReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
  private final GroupReportRepository groupReportRepository;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final GroupCourseRepository groupCourseRepository;

  private final ImagePathMapper imagePathMapper;

  public ReportDto.ReportInfo createReport(ReportForm form, String email) {
    User user = userRepository.findUserByEmail(email).orElseThrow(UserNotFoundException::new);

    List<User> participants =
        form.getParticipants().stream()
            .map(userRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    List<Course> courses =
        form.getCourses().stream()
            .map(courseRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    // filter groupCourses by form.getCourses()
    List<GroupCourse> groupCourses =
        groupCourseRepository.findAllByStudyGroup(user.getStudyGroup());
    groupCourses.removeIf(gc -> !courses.contains(gc.getCourse()));

    // parse image path to filename
    // /path/to/image.png -> image.png
    List<String> imageFilenames = imagePathMapper.extractFilename(form.getImages());

    GroupReport report =
        GroupReport.builder()
            .title(form.getTitle())
            .content(form.getContent())
            .totalMinutes(form.getTotalMinutes())
            .studyGroup(user.getStudyGroup())
            .participants(participants)
            .images(imageFilenames)
            .courses(groupCourses)
            .build();

    GroupReport saved = groupReportRepository.save(report);
    Map<Long, String> imgFullPaths = imagePathMapper.parseImageToMapWithFullPath(saved.getImages());

    return new ReportDto.ReportInfo(saved, imgFullPaths);
  }

  public List<ReportDto.ReportInfo> getReports(String email) {
    StudyGroup studyGroup = userRepository.findUserByEmail(email).orElseThrow().getStudyGroup();

    return studyGroup.getReports().stream()
        .map(
            report -> {
              Map<Long, String> imgFullPaths =
                  imagePathMapper.parseImageToMapWithFullPath(report.getImages());
              return new ReportDto.ReportInfo(report, imgFullPaths);
            })
        .toList();
  }

  public boolean updateReport(Long reportId, ReportForm form) {
    List<User> participants =
        form.getParticipants().stream()
            .map(userRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    List<Course> courses =
        form.getCourses().stream()
            .map(courseRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    GroupReport targetReport =
        groupReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);

    // filter groupCourses by form.getCourses()
    List<GroupCourse> groupCourses =
        groupCourseRepository.findAllByStudyGroup(targetReport.getStudyGroup());
    groupCourses.removeIf(gc -> !courses.contains(gc.getCourse()));

    // parse image path to filename
    // /path/to/image.png -> image.png
    List<String> imageFilenames = imagePathMapper.extractFilename(form.getImages());
    targetReport.update(form, imageFilenames, participants, groupCourses);

    return true;
  }

  public Optional<ReportDto.ReportInfo> getReport(Long reportId) {
    return groupReportRepository
        .findById(reportId)
        .map(
            report -> {
              Map<Long, String> imgFullPaths =
                  imagePathMapper.parseImageToMapWithFullPath(report.getImages());
              return new ReportDto.ReportInfo(report, imgFullPaths);
            });
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
