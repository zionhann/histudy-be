package edu.handong.csee.histudy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.jpa.JpaStudyApplicantRepository;
import edu.handong.csee.histudy.repository.jpa.JpaStudyGroupRepository;
import edu.handong.csee.histudy.repository.jpa.JpaStudyReportRepository;
import edu.handong.csee.histudy.support.BaseRepositoryTest;
import edu.handong.csee.histudy.support.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StudyReportRepositoryTest extends BaseRepositoryTest {

  @Autowired private JpaStudyReportRepository studyReportRepository;
  @Autowired private JpaStudyGroupRepository studyGroupRepository;
  @Autowired private JpaStudyApplicantRepository studyApplicantRepository;

  private StudyGroup studyGroup1;
  private StudyGroup studyGroup2;

  @BeforeEach
  void setUp() {
    StudyApplicant applicant1 =
        TestDataFactory.createStudyApplicant(currentTerm, user1, List.of(), List.of(course1));
    StudyApplicant applicant2 =
        TestDataFactory.createStudyApplicant(currentTerm, user2, List.of(), List.of(course2));

    studyApplicantRepository.save(applicant1);
    studyApplicantRepository.save(applicant2);

    studyGroup1 = TestDataFactory.createStudyGroup(1, currentTerm);
    studyGroup2 = TestDataFactory.createStudyGroup(2, currentTerm);

    studyGroupRepository.save(studyGroup1);
    studyGroupRepository.save(studyGroup2);

    applicant1.joinStudyGroup(studyGroup1);
    applicant2.joinStudyGroup(studyGroup2);
  }

  @Test
  void 스터디그룹별보고서조회시_생성일자내림차순정렬반환() {
    // Given
    StudyReport report1 =
        StudyReport.builder()
            .title("First Report")
            .content("Content 1")
            .totalMinutes(60L)
            .studyGroup(studyGroup1)
            .participants(List.of(user1))
            .images(List.of("image1.png"))
            .courses(List.of(course1))
            .build();

    StudyReport report2 =
        StudyReport.builder()
            .title("Second Report")
            .content("Content 2")
            .totalMinutes(90L)
            .studyGroup(studyGroup1)
            .participants(List.of(user1))
            .images(List.of("image2.png"))
            .courses(List.of(course1))
            .build();

    StudyReport report3 =
        StudyReport.builder()
            .title("Different Group Report")
            .content("Content 3")
            .totalMinutes(120L)
            .studyGroup(studyGroup2)
            .participants(List.of(user2))
            .images(List.of())
            .courses(List.of(course2))
            .build();

    // Save reports with order: report1 first, then report2, then report3
    studyReportRepository.save(report1);
    studyReportRepository.save(report2);
    studyReportRepository.save(report3);

    // When
    List<StudyReport> group1Reports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup1);
    List<StudyReport> group2Reports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup2);

    // Then
    assertThat(group1Reports).hasSize(2);
    // Since we can't guarantee order with identical timestamps, just verify group membership
    assertThat(group1Reports).extracting("title").containsExactly("Second Report", "First Report");
    assertThat(group1Reports).allMatch(report -> report.getStudyGroup().equals(studyGroup1));

    assertThat(group2Reports).hasSize(1);
    assertThat(group2Reports.get(0).getTitle()).isEqualTo("Different Group Report");
  }

  @Test
  void 보고서없는그룹조회시_빈결과반환() {
    // When
    List<StudyReport> reports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup1);

    // Then
    assertThat(reports).isEmpty();
  }

  @Test
  void 새스터디보고서저장시_저장된보고서반환() {
    // Given
    StudyReport report = TestDataFactory.createStudyReport(studyGroup1, "Test Report Content");

    // When
    StudyReport savedReport = studyReportRepository.save(report);

    // Then
    assertThat(savedReport.getStudyReportId()).isNotNull();
    assertThat(savedReport.getContent()).isEqualTo("Test Report Content");
    assertThat(savedReport.getStudyGroup()).isEqualTo(studyGroup1);
  }

  @Test
  void 스터디보고서삭제시_삭제성공() {
    // Given
    StudyReport report = TestDataFactory.createStudyReport(studyGroup1, "Test Report");
    StudyReport savedReport = studyReportRepository.save(report);

    // When
    studyReportRepository.delete(savedReport);
    flushAndClear();

    // Then
    List<StudyReport> reports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup1);
    assertThat(reports).isEmpty();
  }

  @Test
  void 이미지포함보고서저장시_이미지경로포함저장성공() {
    // Given
    StudyReport report =
        TestDataFactory.createStudyReport(studyGroup1, "Report with image", "/path/to/image.png");

    // When
    StudyReport savedReport = studyReportRepository.save(report);

    // Then
    assertThat(savedReport.getStudyReportId()).isNotNull();
    assertThat(savedReport.getContent()).isEqualTo("Report with image");
    assertThat(savedReport.getImages()).hasSize(1);
    assertThat(savedReport.getStudyGroup()).isEqualTo(studyGroup1);
  }

  @Test
  void 복수그룹보고서조회시_그룹별분리조회성공() {
    // Given
    StudyReport group1Report = TestDataFactory.createStudyReport(studyGroup1, "Group 1 Report");
    StudyReport group2Report = TestDataFactory.createStudyReport(studyGroup2, "Group 2 Report");

    studyReportRepository.save(group1Report);
    studyReportRepository.save(group2Report);

    // When
    List<StudyReport> group1Reports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup1);
    List<StudyReport> group2Reports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup2);

    // Then
    assertThat(group1Reports).hasSize(1);
    assertThat(group1Reports.get(0).getContent()).isEqualTo("Group 1 Report");
    assertThat(group1Reports.get(0).getStudyGroup()).isEqualTo(studyGroup1);

    assertThat(group2Reports).hasSize(1);
    assertThat(group2Reports.get(0).getContent()).isEqualTo("Group 2 Report");
    assertThat(group2Reports.get(0).getStudyGroup()).isEqualTo(studyGroup2);
  }
}
