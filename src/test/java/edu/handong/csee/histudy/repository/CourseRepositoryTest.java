package edu.handong.csee.histudy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.repository.jpa.JpaCourseRepository;
import edu.handong.csee.histudy.support.BaseRepositoryTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CourseRepositoryTest extends BaseRepositoryTest {

  @Autowired private JpaCourseRepository courseRepository;

  @Test
  void 강의명키워드로검색시_대소문자무시하고일치강의반환() {
    // Given
    Course course1 =
        Course.builder()
            .name("Introduction to Programming")
            .code("ECE101")
            .professor("John Doe")
            .academicTerm(currentTerm)
            .build();

    Course course2 =
        Course.builder()
            .name("Advanced Programming")
            .code("ECE201")
            .professor("Jane Smith")
            .academicTerm(currentTerm)
            .build();

    Course course3 =
        Course.builder()
            .name("Database Systems")
            .code("ECE301")
            .professor("Bob Johnson")
            .academicTerm(currentTerm)
            .build();

    persistAndFlush(course1);
    persistAndFlush(course2);
    persistAndFlush(course3);

    // When
    List<Course> results = courseRepository.findAllByNameContainingIgnoreCase("programming");

    // Then - BaseRepositoryTest courses might also match, so check if our courses are included
    assertThat(results).hasSizeGreaterThanOrEqualTo(2);
    assertThat(results)
        .extracting("name")
        .contains("Introduction to Programming", "Advanced Programming");
  }

  @Test
  void 일치하지않는강의명검색시_빈결과반환() {
    // Given
    Course course =
        Course.builder()
            .name("Mathematics")
            .code("MAT101")
            .professor("Alice Brown")
            .academicTerm(currentTerm)
            .build();

    persistAndFlush(course);

    // When
    List<Course> results = courseRepository.findAllByNameContainingIgnoreCase("physics");

    // Then
    assertThat(results).isEmpty();
  }

  @Test
  void 현재학기강의조회시_현재학기강의목록반환() {
    // Given
    Course currentCourse =
        Course.builder()
            .name("Current Course")
            .code("CUR101")
            .professor("Current Prof")
            .academicTerm(currentTerm)
            .build();

    Course pastCourse =
        Course.builder()
            .name("Past Course")
            .code("PAS101")
            .professor("Past Prof")
            .academicTerm(pastTerm)
            .build();

    persistAndFlush(currentCourse);
    persistAndFlush(pastCourse);

    // When
    List<Course> results = courseRepository.findAllByAcademicTermIsCurrentTrue();

    // Then - BaseRepositoryTest에서 2개 + 추가로 1개 = 3개
    assertThat(results).hasSize(3);
    assertThat(results).anyMatch(course -> course.getName().equals("Current Course"));
    assertThat(results).allMatch(course -> course.getAcademicTerm().getIsCurrent());
  }

  @Test
  void 강의목록일괄저장시_모든강의저장성공() {
    // Given
    Course course1 =
        Course.builder()
            .name("Course 1")
            .code("C001")
            .professor("Prof 1")
            .academicTerm(currentTerm)
            .build();

    Course course2 =
        Course.builder()
            .name("Course 2")
            .code("C002")
            .professor("Prof 2")
            .academicTerm(currentTerm)
            .build();

    List<Course> courses = List.of(course1, course2);

    // When
    List<Course> savedCourses = courseRepository.saveAll(courses);

    // Then
    assertThat(savedCourses).hasSize(2);
    assertThat(savedCourses).allMatch(course -> course.getCourseId() != null);
  }

  @Test
  void 강의ID로존재확인시_존재여부반환() {
    // Given
    Course course =
        Course.builder()
            .name("Test Course")
            .code("TEST101")
            .professor("Test Prof")
            .academicTerm(currentTerm)
            .build();

    Course savedCourse = persistAndFlush(course);

    // When
    boolean exists = courseRepository.existsById(savedCourse.getCourseId());
    boolean notExists = courseRepository.existsById(999L);

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  void 강의ID로삭제시_강의삭제성공() {
    // Given
    Course course =
        Course.builder()
            .name("To Delete Course")
            .code("DEL101")
            .professor("Delete Prof")
            .academicTerm(currentTerm)
            .build();

    Course savedCourse = persistAndFlush(course);
    Long courseId = savedCourse.getCourseId();

    // When
    courseRepository.deleteById(courseId);
    flushAndClear();

    // Then
    Optional<Course> deletedCourse = courseRepository.findById(courseId);
    assertThat(deletedCourse).isEmpty();
  }

  @Test
  void 유효한강의ID로조회시_강의반환() {
    // Given
    Course course =
        Course.builder()
            .name("Find Course")
            .code("FIND101")
            .professor("Find Prof")
            .academicTerm(currentTerm)
            .build();

    Course savedCourse = persistAndFlush(course);

    // When
    Optional<Course> result = courseRepository.findById(savedCourse.getCourseId());

    // Then
    assertThat(result).isPresent();
    assertCourse(result.get(), "Find Course", "FIND101", "Find Prof");
  }

  @Test
  void 존재하지않는강의ID로조회시_빈결과반환() {
    // When
    Optional<Course> result = courseRepository.findById(999L);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void 빈키워드로검색시_전체강의반환() {
    // Given
    Course course =
        Course.builder()
            .name("Test Course")
            .code("TEST101")
            .professor("Test Prof")
            .academicTerm(currentTerm)
            .build();

    persistAndFlush(course);

    // When
    List<Course> results = courseRepository.findAllByNameContainingIgnoreCase("");

    // Then - 빈 문자열은 모든 강의를 포함할 수 있음 (BaseRepositoryTest 2개 + 추가 1개)
    assertThat(results).hasSize(3);
  }
}
