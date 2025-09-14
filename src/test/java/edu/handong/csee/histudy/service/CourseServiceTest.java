package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.*;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.support.TestDataFactory;
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
            courseRepository, userRepository, academicTermRepository, studyGroupRepository);

    AcademicTerm term = TestDataFactory.createCurrentTerm();
    academicTermRepository.save(term);

    User student1 = TestDataFactory.createUser("1", "22500101", "user1@test.com", "Foo", Role.USER);
    User student2 = TestDataFactory.createUser("2", "22500102", "user2@test.com", "Bar", Role.USER);

    userRepository.save(student1);
    userRepository.save(student2);

    Course course1 =
        TestDataFactory.createCourse("Introduction to Algorithms", "ECE00101", "John Doe", term);
    Course course2 =
        TestDataFactory.createCourse(
            "Introduction to Data Structures", "ECE00102", "John Doe", term);

    courseRepository.saveAll(List.of(course1, course2));

    StudyApplicant studyApplicant1 =
        TestDataFactory.createStudyApplicant(term, student1, List.of(student2), List.of(course1));
    StudyApplicant studyApplicant2 =
        TestDataFactory.createStudyApplicant(term, student2, List.of(student1), List.of(course2));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1, studyApplicant2));
    studyGroupRepository.save(studyGroup);
  }

  @Test
  void 호출시_전체강의목록반환() {
    // When
    List<CourseDto.CourseInfo> courses = courseService.getCurrentCourses();

    // Then
    assertThat(courses.size()).isEqualTo(2);
  }

  @Test
  void 키워드제공시_일치하는강의반환() {
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
  void 대소문자_키워드제공시_대소문자무관_검색() {
    // When
    String keyword1 = "ALGO"; // uppercase
    String keyword2 = "Algorithm"; // mixed case
    String keyword3 = "algorithms"; // lowercase with plural

    List<CourseDto.CourseInfo> res1 = courseService.search(keyword1);
    List<CourseDto.CourseInfo> res2 = courseService.search(keyword2);
    List<CourseDto.CourseInfo> res3 = courseService.search(keyword3);

    // Then
    assertThat(res1.size()).isEqualTo(1); // Should match "Introduction to Algorithms"
    assertThat(res2.size()).isEqualTo(1); // Should match "Introduction to Algorithms"
    assertThat(res3.size()).isEqualTo(1); // Should match "Introduction to Algorithms"
  }

  @Test
  void 다양한대소문자_패턴검색() {
    // When
    String keyword1 = "data"; // lowercase
    String keyword2 = "DATA"; // uppercase
    String keyword3 = "Data"; // title case

    List<CourseDto.CourseInfo> res1 = courseService.search(keyword1);
    List<CourseDto.CourseInfo> res2 = courseService.search(keyword2);
    List<CourseDto.CourseInfo> res3 = courseService.search(keyword3);

    // Then - All should match "Introduction to Data Structures"
    assertThat(res1.size()).isEqualTo(1);
    assertThat(res2.size()).isEqualTo(1);
    assertThat(res3.size()).isEqualTo(1);
  }

  @Test
  void 사용자이메일시_팀강의목록반환() {
    // When
    List<CourseDto.CourseInfo> res = courseService.getTeamCourses("user1@test.com");

    // Then
    assertThat(res.size()).isEqualTo(2);
  }

  @Test
  void 유효한CSV파일시_강의저장성공() throws IOException {
    // Given
    String csvContent =
        """
    title,code,prof
    Advanced Programming,CSE30201,Dr. Kim
    Database Systems,CSE30301,Prof. Lee
    Operating Systems,CSE30401,Dr. Park
    """;
    InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
    MockMultipartFile file =
        new MockMultipartFile("courses.csv", "courses.csv", "text/csv", stream);

    // When
    courseService.readCourseCSV(file);

    // Then
    List<Course> savedCourses = ((FakeCourseRepository) courseRepository).findAll();
    assertThat(savedCourses).hasSize(3); // 3 new courses (existing current courses deleted)
  }

  @Test
  void IO예외발생시_예외전파() {
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

  @Test
  void 빈필드포함CSV업로드시_검증예외발생() throws IOException {
    // Given
    String csvContent =
        """
    title,code,prof
    Advanced Programming,CSE30201,Dr. Kim
    ,CSE30301,Prof. Lee
    Operating Systems,,Dr. Park
    """;
    InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
    MockMultipartFile file =
        new MockMultipartFile("courses.csv", "courses.csv", "text/csv", stream);

    // When & Then
    assertThatThrownBy(() -> courseService.readCourseCSV(file))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing or empty required field");
  }

  @Test
  void 공백필드포함CSV업로드시_검증예외발생() throws IOException {
    // Given
    String csvContent =
        """
    title,code,prof
    Advanced Programming,CSE30201,Dr. Kim
    Database Systems,   ,Prof. Lee
    """;
    InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
    MockMultipartFile file =
        new MockMultipartFile("courses.csv", "courses.csv", "text/csv", stream);

    // When & Then
    assertThatThrownBy(() -> courseService.readCourseCSV(file))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing or empty required field 'code'");
  }

  @Test
  void 앞뒤공백포함CSV업로드시_트림처리후_저장성공() throws IOException {
    // Given
    String csvContent =
        """
    title,code,prof
      Advanced Programming  ,  CSE30201  ,  Dr. Kim
    """;
    InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
    MockMultipartFile file =
        new MockMultipartFile("courses.csv", "courses.csv", "text/csv", stream);

    // When
    courseService.readCourseCSV(file);

    // Then
    List<Course> savedCourses = ((FakeCourseRepository) courseRepository).findAll();
    assertThat(savedCourses).hasSize(1);
    assertThat(savedCourses.get(0).getName()).isEqualTo("Advanced Programming");
    assertThat(savedCourses.get(0).getCode()).isEqualTo("CSE30201");
    assertThat(savedCourses.get(0).getProfessor()).isEqualTo("Dr. Kim");
  }
}
