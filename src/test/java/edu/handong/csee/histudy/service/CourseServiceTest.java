package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.*;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

  private CourseService courseService;

  private CourseRepository courseRepository;
  private UserRepository userRepository;
  private AcademicTermRepository academicTermRepository;
  private StudyGroupRepository studyGroupRepository;
  private StudyApplicantRepository studyApplicationRepository;

  @BeforeEach
  void init() {
    courseRepository = new FakeCourseRepository();
    userRepository = new FakeUserRepository();
    academicTermRepository = new FakeAcademicTermRepository();
    studyGroupRepository = new FakeStudyGroupRepository();
    studyApplicationRepository = new FakeStudyApplicationRepository();

    courseService =
        new CourseService(
            courseRepository,
            userRepository,
            academicTermRepository,
            studyGroupRepository,
            studyApplicationRepository);
  }

  @Test
  void 강의목록_검색_전체() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    Course course1 = new Course("Introduction to Algorithms", "ECE00101", "John Doe", term);
    Course course2 = new Course("Introduction to Data Structures", "ECE00102", "John Doe", term);

    courseRepository.saveAll(List.of(course1, course2));

    // When
    List<CourseDto.CourseInfo> courses = courseService.getCurrentCourses();

    // Then
    assertThat(courses.size()).isEqualTo(2);
  }

  @Test
  void 강의목록_검색_키워드() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    Course course1 = new Course("Introduction to Algorithms", "ECE00101", "John Doe", term);
    Course course2 = new Course("Introduction to Data Structures", "ECE00102", "John Doe", term);

    courseRepository.saveAll(List.of(course1, course2));

    // When
    String keyword1 = "algo";
    String keyword2 = "intro";

    List<CourseDto.CourseInfo> res1 = courseService.search(keyword1);
    List<CourseDto.CourseInfo> res2 = courseService.search(keyword2);

    // Then
    assertThat(res1.size()).isEqualTo(1);
    assertThat(res2.size()).isEqualTo(2);
  }
}
