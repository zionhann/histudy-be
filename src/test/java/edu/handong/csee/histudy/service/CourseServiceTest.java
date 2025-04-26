package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.*;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CourseServiceTest {

  private CourseService courseService;

  private CourseRepository courseRepository;
  private UserRepository userRepository;
  private AcademicTermRepository academicTermRepository;
  private StudyGroupRepository studyGroupRepository;
  private StudyApplicantRepository studyApplicantRepository;

  @BeforeEach
  void init() {
    courseRepository = new FakeCourseRepository();
    userRepository = new FakeUserRepository();
    academicTermRepository = new FakeAcademicTermRepository();
    studyGroupRepository = new FakeStudyGroupRepository();
    studyApplicantRepository = new FakeStudyApplicationRepository();

    courseService =
        new CourseService(
            courseRepository,
            userRepository,
            academicTermRepository,
            studyGroupRepository,
            studyApplicantRepository);

    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    userRepository.save(student2);

    Course course1 = new Course("Introduction to Algorithms", "ECE00101", "John Doe", term);
    Course course2 = new Course("Introduction to Data Structures", "ECE00102", "John Doe", term);

    courseRepository.saveAll(List.of(course1, course2));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course1));

    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course2));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1, studyApplicant2));
    studyGroupRepository.save(studyGroup);
  }

  @Test
  void 강의목록_검색_전체() {
    // When
    List<CourseDto.CourseInfo> courses = courseService.getCurrentCourses();

    // Then
    assertThat(courses.size()).isEqualTo(2);
  }

  @Test
  void 강의목록_검색_키워드() {
    // When
    String keyword1 = "algo";
    String keyword2 = "intro";

    List<CourseDto.CourseInfo> res1 = courseService.search(keyword1);
    List<CourseDto.CourseInfo> res2 = courseService.search(keyword2);

    // Then
    assertThat(res1.size()).isEqualTo(1);
    assertThat(res2.size()).isEqualTo(2);
  }

  @Test
  void 스터디그룹_선택과목_목록_조회() {
    // When
    List<CourseDto.CourseInfo> res = courseService.getTeamCourses("user1@test.com");

    // Then
    assertThat(res.size()).isEqualTo(2);
  }
}
