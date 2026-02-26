package edu.handong.csee.histudy.service.repository.fake;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.StudyReport;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class FakeRepositoryContractTest {

  @Test
  void 사용자저장시_기존아이디면_업데이트처리한다() {
    FakeUserRepository repository = new FakeUserRepository();
    User user = User.builder().sub("sub-1").sid("22500101").email("a@test.com").name("A").role(Role.USER).build();
    repository.save(user);

    user.edit(UserDto.UserEdit.builder().name("renamed").build());
    repository.save(user);

    assertThat(repository.findById(user.getUserId())).isPresent();
    assertThat(repository.findById(user.getUserId()).get().getName()).isEqualTo("renamed");
    assertThat(repository.findAll(org.springframework.data.domain.Sort.by("userId"))).hasSize(1);
  }

  @Test
  void 신청서조회시_그룹이없는신청자포함되어도_NPE가발생하지않는다() {
    FakeStudyApplicationRepository repository = new FakeStudyApplicationRepository();
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    User user = User.builder().sub("sub-1").sid("22500101").email("a@test.com").name("A").role(Role.USER).build();
    StudyApplicant applicant = StudyApplicant.of(term, user, List.of(), List.of());
    repository.save(applicant);

    List<StudyApplicant> found = repository.findAllByStudyGroup(StudyGroup.of(1, term, List.of()));

    assertThat(found).isEmpty();
  }

  @Test
  void 그룹저장시_기존아이디면_중복없이업데이트된다() {
    FakeStudyGroupRepository repository = new FakeStudyGroupRepository();
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    StudyGroup group = StudyGroup.of(1, term, List.of());
    repository.save(group);
    repository.save(group);

    assertThat(repository.count()).isEqualTo(1);
  }

  @Test
  void 과목일괄저장시_기존아이디면_중복없이업데이트된다() {
    FakeCourseRepository repository = new FakeCourseRepository();
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    Course course = new Course("OS", "CSE101", "Prof", term);
    repository.saveAll(List.of(course));
    repository.saveAll(List.of(course));

    assertThat(repository.findAll()).hasSize(1);
  }

  @Test
  void 보고서저장시_기존아이디면_중복없이업데이트된다() {
    FakeStudyReportRepository repository = new FakeStudyReportRepository();
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    StudyGroup group = StudyGroup.of(1, term, List.of());
    StudyReport report =
        StudyReport.builder()
            .title("r1")
            .content("content")
            .totalMinutes(30L)
            .studyGroup(group)
            .participants(List.of())
            .images(List.of())
            .courses(List.of())
            .build();
    repository.save(report);
    repository.save(report);

    assertThat(repository.count()).isEqualTo(1);
  }
}
