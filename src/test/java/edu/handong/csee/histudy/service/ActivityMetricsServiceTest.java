package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.StudyReport;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ActivityMetricsDto;
import edu.handong.csee.histudy.dto.ActivityTerm;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
import edu.handong.csee.histudy.service.repository.fake.FakeAcademicTermRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeStudyApplicationRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeStudyGroupRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeStudyReportRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeUserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActivityMetricsServiceTest {

  private final AcademicTerm currentTerm =
      AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
  private final AcademicTerm previousTerm =
      AcademicTerm.builder().academicYear(2024).semester(TermType.FALL).isCurrent(false).build();
  private final User memberUser =
      User.builder()
          .sub("sub-1")
          .sid("22230001")
          .email("member@histudy.com")
          .name("Member")
          .role(Role.USER)
          .build();
  private final User adminUser =
      User.builder()
          .sub("sub-2")
          .sid("22230002")
          .email("admin@histudy.com")
          .name("Admin")
          .role(Role.ADMIN)
          .build();
  private final User currentMemberUser =
      User.builder()
          .sub("sub-3")
          .sid("22230003")
          .email("current@histudy.com")
          .name("Current")
          .role(Role.USER)
          .build();
  private final User previousMemberUser =
      User.builder()
          .sub("sub-4")
          .sid("22230004")
          .email("previous@histudy.com")
          .name("Previous")
          .role(Role.USER)
          .build();
  private final Course currentCourse =
      Course.builder()
          .name("자료구조")
          .code("CSEE201")
          .professor("Kim")
          .academicTerm(currentTerm)
          .build();
  private final Course previousCourse =
      Course.builder()
          .name("운영체제")
          .code("CSEE301")
          .professor("Lee")
          .academicTerm(previousTerm)
          .build();

  private FakeUserRepository userRepository;
  private FakeStudyApplicationRepository studyApplicantRepository;
  private FakeStudyGroupRepository studyGroupRepository;
  private FakeStudyReportRepository studyReportRepository;
  private FakeAcademicTermRepository academicTermRepository;
  private ActivityMetricsService activityMetricsService;

  @BeforeEach
  void setUp() {
    userRepository = new FakeUserRepository();
    studyApplicantRepository = new FakeStudyApplicationRepository();
    studyGroupRepository = new FakeStudyGroupRepository();
    studyReportRepository = new FakeStudyReportRepository();
    academicTermRepository = new FakeAcademicTermRepository();
    activityMetricsService =
        new ActivityMetricsService(
            userRepository,
            studyApplicantRepository,
            studyGroupRepository,
            studyReportRepository,
            academicTermRepository);
  }

  @Test
  void 전체_학기_활동_지표를_조회하면_전체_집계값을_반환한다() {
    // Given
    academicTermRepository.save(currentTerm);
    User member = userRepository.save(memberUser);
    userRepository.save(adminUser);
    StudyApplicant applicant =
        StudyApplicant.of(currentTerm, member, List.of(), List.of(currentCourse));
    studyApplicantRepository.save(applicant);
    StudyGroup group = studyGroupRepository.save(StudyGroup.of(1, currentTerm, List.of(applicant)));
    studyReportRepository.save(
        StudyReport.builder()
            .title("1주차")
            .content("첫 모임")
            .totalMinutes(180)
            .studyGroup(group)
            .participants(List.of(member))
            .images(List.of("reports/report.png"))
            .courses(List.of(currentCourse))
            .build());

    // When
    ActivityMetricsDto result = activityMetricsService.getActivityMetrics(ActivityTerm.ALL);

    // Then
    assertThat(result.getStudyMembers()).isEqualTo(1);
    assertThat(result.getStudyGroups()).isEqualTo(1);
    assertThat(result.getStudyHours()).isEqualTo(3);
    assertThat(result.getReports()).isEqualTo(1);
  }

  @Test
  void 현재_학기_활동_지표를_조회하면_현재_학기_집계값을_반환한다() {
    // Given
    academicTermRepository.save(currentTerm);
    academicTermRepository.save(previousTerm);
    User currentMember = userRepository.save(currentMemberUser);
    User previousMember = userRepository.save(previousMemberUser);
    StudyApplicant currentApplicant =
        StudyApplicant.of(currentTerm, currentMember, List.of(), List.of(currentCourse));
    StudyApplicant previousApplicant =
        StudyApplicant.of(previousTerm, previousMember, List.of(), List.of(previousCourse));
    studyApplicantRepository.saveAll(List.of(currentApplicant, previousApplicant));
    StudyGroup currentGroup =
        studyGroupRepository.save(StudyGroup.of(1, currentTerm, List.of(currentApplicant)));
    StudyGroup previousGroup =
        studyGroupRepository.save(StudyGroup.of(2, previousTerm, List.of(previousApplicant)));
    studyReportRepository.save(
        StudyReport.builder()
            .title("2주차")
            .content("두번째 모임")
            .totalMinutes(120)
            .studyGroup(currentGroup)
            .participants(List.of(currentMember))
            .images(List.of("reports/current.png"))
            .courses(List.of(currentCourse))
            .build());
    studyReportRepository.save(
        StudyReport.builder()
            .title("1주차")
            .content("첫모임")
            .totalMinutes(300)
            .studyGroup(previousGroup)
            .participants(List.of(previousMember))
            .images(List.of("reports/previous.png"))
            .courses(List.of(previousCourse))
            .build());

    // When
    ActivityMetricsDto result = activityMetricsService.getActivityMetrics(ActivityTerm.CURRENT);

    // Then
    assertThat(result.getStudyMembers()).isEqualTo(1);
    assertThat(result.getStudyGroups()).isEqualTo(1);
    assertThat(result.getStudyHours()).isEqualTo(2);
    assertThat(result.getReports()).isEqualTo(1);
  }

  @Test
  void 현재_학기_없이_활동_지표를_조회하면_예외가_발생한다() {
    // Given
    ActivityTerm term = ActivityTerm.CURRENT;

    // When Then
    assertThatThrownBy(() -> activityMetricsService.getActivityMetrics(term))
        .isInstanceOf(NoCurrentTermFoundException.class);
  }
}
