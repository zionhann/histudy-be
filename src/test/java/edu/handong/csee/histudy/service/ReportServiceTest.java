package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.exception.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.support.TestDataFactory;
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
    AcademicTerm term = TestDataFactory.createCurrentTerm();
    academicTermRepository.save(term);

    User student1 = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    User student2 = TestDataFactory.createUser("2", "22500102", "user2@test.com", "Bar", Role.USER);

    userRepository.save(student1);
    userRepository.save(student2);

    Course course = TestDataFactory.createCourse("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        TestDataFactory.createStudyApplicant(term, student1, List.of(student2), List.of(course));
    StudyApplicant studyApplicant2 =
        TestDataFactory.createStudyApplicant(term, student2, List.of(student1), List.of(course));

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
  void 보고서폼제공시_보고서생성() {
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
  void 사용자이메일시_보고서목록반환() {
    // When
    List<ReportDto.ReportInfo> reports = reportService.getReports("user1@test.com");

    // Then
    assertThat(reports.size()).isEqualTo(1);
  }

  @Test
  void 보고서ID시_상세정보반환() {
    // When
    Optional<ReportDto.ReportInfo> reportOr1 = reportService.getReport(1L);
    Optional<ReportDto.ReportInfo> reportOr2 = reportService.getReport(2L);

    // Then
    assertThat(reportOr1.isPresent()).isTrue();
    assertThat(reportOr2.isPresent()).isFalse();
  }

  @Test
  void 수정폼제공시_보고서업데이트() {
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
  void 보고서ID시_삭제성공() {
    // When
    Optional<ReportDto.ReportInfo> reportOrBefore = reportService.getReport(1L);
    reportService.deleteReport(1L);
    Optional<ReportDto.ReportInfo> reportOrAfter = reportService.getReport(1L);

    // Then
    assertThat(reportOrBefore.isPresent()).isTrue();
    assertThat(reportOrAfter.isPresent()).isFalse();
  }

  @Test
  void 존재하지않는보고서삭제시_false반환() {
    // When
    boolean result = reportService.deleteReport(999L);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void 존재하지않는사용자시_예외발생() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .participants(List.of())
            .courses(List.of())
            .build();

    // When & Then
    assertThatThrownBy(() -> reportService.createReport(form, "nonexistent@test.com"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 현재학기없을시_예외발생() {
    // Given
    ReportService testReportService = createReportServiceWithoutCurrentTerm();
    ReportForm form =
        ReportForm.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .participants(List.of())
            .courses(List.of())
            .build();

    // When & Then
    assertThatThrownBy(() -> testReportService.createReport(form, "user1@test.com"))
        .isInstanceOf(NoCurrentTermFoundException.class);
  }

  @Test
  void 목록조회시존재하지않는사용자_예외발생() {
    // When & Then
    assertThatThrownBy(() -> reportService.getReports("nonexistent@test.com"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 목록조회시현재학기없음_예외발생() {
    // Given
    ReportService testReportService = createReportServiceWithoutCurrentTerm();

    // When & Then
    assertThatThrownBy(() -> testReportService.getReports("user1@test.com"))
        .isInstanceOf(NoCurrentTermFoundException.class);
  }

  @Test
  void 존재하지않는보고서수정시_예외발생() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("modifiedTitle")
            .participants(List.of())
            .courses(List.of())
            .build();

    // When & Then
    assertThatThrownBy(() -> reportService.updateReport(999L, form))
        .isInstanceOf(ReportNotFoundException.class);
  }

  @Test
  void 참가자와과목포함시_보고서생성() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("title with participants")
            .content("content")
            .totalMinutes(90L)
            .participants(List.of(1L, 2L))
            .courses(List.of(1L))
            .build();

    // When
    ReportDto.ReportInfo report = reportService.createReport(form, "user1@test.com");

    // Then
    assertThat(report.getTitle()).isEqualTo("title with participants");
    assertThat(report.getTotalMinutes()).isEqualTo(90L);
    assertThat(report.getParticipants()).hasSize(2);
    assertThat(report.getCourses()).hasSize(1);
  }

  @Test
  void 존재하지않는참가자시_필터링() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .participants(List.of(999L))
            .courses(List.of())
            .build();

    // When
    ReportDto.ReportInfo report = reportService.createReport(form, "user1@test.com");

    // Then - 존재하지 않는 참가자는 필터링됨
    assertThat(report.getParticipants()).hasSize(0);
  }

  @Test
  void 존재하지않는과목시_필터링() {
    // Given
    ReportForm form =
        ReportForm.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .participants(List.of())
            .courses(List.of(999L))
            .build();

    // When
    ReportDto.ReportInfo report = reportService.createReport(form, "user1@test.com");

    // Then - 존재하지 않는 과목은 필터링됨
    assertThat(report.getCourses()).hasSize(0);
  }

  private ReportService createReportServiceWithoutCurrentTerm() {
    StudyGroupRepository studyGroupRepository = new FakeStudyGroupRepository();
    UserRepository userRepository = new FakeUserRepository();
    AcademicTermRepository academicTermRepository = new FakeAcademicTermRepository();
    StudyReportRepository studyReportRepository = new FakeStudyReportRepository();
    CourseRepository courseRepository = new FakeCourseRepository();
    ImagePathMapper imagePathMapper = new ImagePathMapper();

    User student1 = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    userRepository.save(student1);

    return new ReportService(
        studyReportRepository,
        userRepository,
        courseRepository,
        studyGroupRepository,
        academicTermRepository,
        imagePathMapper);
  }
}
