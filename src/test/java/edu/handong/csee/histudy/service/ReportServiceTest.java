package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

  private ReportService reportService;

  @BeforeEach
  void init() {
    StudyGroupRepository studyGroupRepository = new FakeStudyGroupRepository();
    UserRepository userRepository = new FakeUserRepository();
    AcademicTermRepository academicTermRepository = new FakeAcademicTermRepository();
    StudyApplicantRepository studyApplicantRepository = new FakeStudyApplicationRepository();
    StudyReportRepository studyReportRepository = new FakeStudyReportRepository();
    CourseRepository courseRepository = new FakeCourseRepository();
    ImagePathMapper imagePathMapper = new ImagePathMapper();

    reportService =
        new ReportService(
            studyReportRepository,
            userRepository,
            courseRepository,
            studyGroupRepository,
            academicTermRepository,
            imagePathMapper);

    // Setup
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    userRepository.save(student2);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));

    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1, studyApplicant2));
    studyGroupRepository.save(studyGroup);

    StudyReport report =
        StudyReport.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .courses(List.of(course))
            .participants(List.of(student1))
            .images(List.of("/path/to/image1.png"))
            .studyGroup(studyGroup)
            .build();
    studyReportRepository.save(report);

    IntStream.range(0, report.getImages().size())
        .forEach(
            i ->
                ReflectionTestUtils.setField(
                    report.getImages().get(i), "reportImageId", (long) i + 1));
  }

  @Test
  void 스터디보고서_작성() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("title2")
            .content("content2")
            .totalMinutes(120L)
            .participants(List.of())
            .courses(List.of())
            .build();

    // When
    ReportDto.ReportInfo report = reportService.createReport(form, "user1@test.com");

    // Then
    assertThat(report.getTitle()).isEqualTo("title2");
  }

  @Test
  void 스터디보고서_목록_조회() {
    // When
    List<ReportDto.ReportInfo> reports = reportService.getReports("user1@test.com");

    // Then
    assertThat(reports.size()).isEqualTo(1);
  }

  @Test
  void 스터디보고서_상세_조회() {
    // When
    Optional<ReportDto.ReportInfo> reportOr1 = reportService.getReport(1L);
    Optional<ReportDto.ReportInfo> reportOr2 = reportService.getReport(2L);

    // Then
    assertThat(reportOr1.isPresent()).isTrue();
    assertThat(reportOr2.isPresent()).isFalse();
  }

  @Test
  void 스터디보고서_수정() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("modifiedTitle")
            .participants(List.of())
            .courses(List.of())
            .build();

    // When
    reportService.updateReport(1L, form);
    Optional<ReportDto.ReportInfo> report = reportService.getReport(1L);

    // Then
    assertThat(report.isPresent()).isTrue();
    assertThat(report.get().getTitle()).isEqualTo("modifiedTitle");
  }

  @Test
  void 스터디보고서_삭제() {
    // When
    Optional<ReportDto.ReportInfo> reportOrBefore = reportService.getReport(1L);
    reportService.deleteReport(1L);
    Optional<ReportDto.ReportInfo> reportOrAfter = reportService.getReport(1L);

    // Then
    assertThat(reportOrBefore.isPresent()).isTrue();
    assertThat(reportOrAfter.isPresent()).isFalse();
  }
}
