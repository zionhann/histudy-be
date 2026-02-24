package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.domain.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
class TeamServiceMatchingBasicTest extends TeamServiceTestSupport {

  @Test
  void 친구선호시_그룹매칭() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();
    User student3 =
        User.builder().sub("3").sid("22500103").email("user3@test.com").name("baz").build();
    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));
    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));
    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    teamService.matchTeam();

    assertThat(studyApplicant1.getStudyGroup()).isEqualTo(studyApplicant2.getStudyGroup());
  }

  @Test
  void 과목동일시_그룹매칭() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();
    User student3 =
        User.builder().sub("3").sid("22500103").email("user3@test.com").name("baz").build();
    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 = StudyApplicant.of(term, student1, List.of(), List.of(course));
    StudyApplicant studyApplicant2 = StudyApplicant.of(term, student2, List.of(), List.of(course));
    StudyApplicant studyApplicant3 = StudyApplicant.of(term, student3, List.of(), List.of(course));
    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);
    studyApplicantRepository.save(studyApplicant3);

    teamService.matchTeam();

    assertThat(studyApplicant1.getStudyGroup()).isEqualTo(studyApplicant2.getStudyGroup());
    assertThat(studyApplicant2.getStudyGroup()).isEqualTo(studyApplicant3.getStudyGroup());
  }

  @Test
  void 친구와과목모두일치시_그룹매칭() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();
    User student3 =
        User.builder().sub("3").sid("22500103").email("user3@test.com").name("baz").build();
    userRepository.save(student1);
    userRepository.save(student2);
    userRepository.save(student3);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));
    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));
    StudyApplicant studyApplicant3 = StudyApplicant.of(term, student3, List.of(), List.of(course));
    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);
    studyApplicantRepository.save(studyApplicant3);

    teamService.matchTeam();

    assertThat(studyApplicant1.getStudyGroup()).isEqualTo(studyApplicant2.getStudyGroup());
    assertThat(studyApplicant2.getStudyGroup()).isEqualTo(studyApplicant3.getStudyGroup());
  }

  @Test
  void 빈신청목록시_오류없이처리() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    teamService.matchTeam();
  }

  @Test
  void 다섯명이상시_그룹분할() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    for (int i = 1; i <= 7; i++) {
      User user =
          User.builder()
              .sub(String.valueOf(i))
              .sid("2250010" + i)
              .email("user" + i + "@test.com")
              .name("User" + i)
              .build();
      userRepository.save(user);
      studyApplicantRepository.save(StudyApplicant.of(term, user, List.of(), List.of(course)));
    }

    teamService.matchTeam();

    List<StudyGroup> groups = studyGroupRepository.findAllByAcademicTerm(term);
    assertThat(groups).isNotEmpty();
    int totalMembers = groups.stream().mapToInt(g -> g.getMembers().size()).sum();
    assertThat(totalMembers).isLessThanOrEqualTo(7);
    boolean allGroupsHaveMinMembers = groups.stream().allMatch(g -> g.getMembers().size() >= 3);
    assertThat(allGroupsHaveMinMembers).isTrue();
  }
}
