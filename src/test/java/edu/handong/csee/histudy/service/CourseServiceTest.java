package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.*;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

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

  @Test
  void 강의목록_업로드() throws IOException {
    // Given
    String content =
        """
    class,code,professor,year,semester
    Introduction to Test,ECE00103,John,2025,1
    """;
    InputStream stream = new ByteArrayInputStream(content.getBytes());
    MockMultipartFile file = new MockMultipartFile("file", stream);

    // When
    courseService.readCourseCSV(file);

    // Then
    List<Course> list = courseRepository.findAllByAcademicTermIsCurrentTrue();
    assertThat(list.size()).isEqualTo(3);
  }

  @Test
  void CSV파일로_강의목록_읽기_성공() throws IOException {
    // Given
    String csvContent =
        """
    class,code,professor,year,semester
    Advanced Programming,CSE30201,Dr. Kim,2025,1
    Database Systems,CSE30301,Prof. Lee,2025,1
    Operating Systems,CSE30401,Dr. Park,2025,2
    """;
    InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
    MockMultipartFile file =
        new MockMultipartFile("courses.csv", "courses.csv", "text/csv", stream);

    // When
    courseService.readCourseCSV(file);

    // Then
    List<Course> savedCourses = ((FakeCourseRepository) courseRepository).findAll();
    assertThat(savedCourses).hasSize(5); // 2 existing + 3 new

    Course newCourse1 =
        savedCourses.stream().filter(c -> c.getCode().equals("CSE30201")).findFirst().orElseThrow();
    assertThat(newCourse1.getName()).isEqualTo("Advanced Programming");
    assertThat(newCourse1.getProfessor()).isEqualTo("Dr. Kim");
    assertThat(newCourse1.getAcademicTerm().getAcademicYear()).isEqualTo(2025);
  }

  @Test
  void CSV파일로_새로운_학기_생성() throws IOException {
    // Given
    String csvContent =
        """
    class,code,professor,year,semester
    New Course,NEW001,New Prof,2026,1
    """;
    InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
    MockMultipartFile file =
        new MockMultipartFile("courses.csv", "courses.csv", "text/csv", stream);

    // When
    courseService.readCourseCSV(file);

    // Then
    List<AcademicTerm> terms = ((FakeAcademicTermRepository) academicTermRepository).findAll();
    assertThat(terms).hasSize(2); // 1 existing + 1 new

    AcademicTerm newTerm =
        terms.stream().filter(t -> t.getAcademicYear() == 2026).findFirst().orElseThrow();
    assertThat(newTerm.getSemester()).isEqualTo(TermType.SPRING);
  }

  @Test
  void CSV파일_읽기_중_IOException_발생() {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("invalid.csv", "invalid.csv", "text/csv", (byte[]) null) {
          @Override
          public InputStream getInputStream() throws IOException {
            throw new IOException("File read error");
          }
        };

    // When & Then
    assertThatThrownBy(() -> courseService.readCourseCSV(file))
        .isInstanceOf(IOException.class)
        .hasMessage("File read error");
  }
}
