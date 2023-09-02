package edu.handong.csee.histudy.course;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.CourseIdDto;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserCourseRepository;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.CourseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
public class CourseServiceTest {
    @Autowired
    CourseService courseService;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    StudyGroupRepository studyGroupRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserCourseRepository userCourseRepository;

    @BeforeEach
    void setup() {
        Course course = Course.builder()
                .name("기초전자공학실험")
                .code("ECE20007")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(course);
        Course courseB = Course.builder()
                .name("데이타구조")
                .code("ECE20010")
                .professor("김호준")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(courseB);
        Course courseC = Course.builder()
                .name("자바프로그래밍언어")
                .code("ECE20017")
                .professor("남재창")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(courseC);
    }

    @AfterEach
    void cleanup() {
        courseRepository.deleteAll();
    }

    @DisplayName("팀내에 인원들의 과목들을 불러들여야 한다")
    @Test
    public void teamCourseTest() {
        User user = User.builder()
                .sid("22000328")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User saved = userRepository.save(user);
        StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(1));
        saved.belongTo(studyGroup);
        List<Long> courseIdxList = List.of(1L);
        List<Course> courses = courseIdxList.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<UserCourse> preferredCours = courses.stream().map(c -> userCourseRepository.save(new UserCourse(saved, c))).toList();
        saved.getCourseSelections().addAll(preferredCours);
        User userB = User.builder()
                .sid("22000329")
                .email("b@b.com")
                .role(Role.USER)
                .build();
        User savedB = userRepository.save(userB);
        savedB.belongTo(studyGroup);
        List<Long> courseIdxListB = List.of(1L, 2L);
        List<Course> coursesB = courseIdxListB.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<UserCourse> choicesB = coursesB.stream().map(c -> userCourseRepository.save(new UserCourse(savedB, c))).toList();
        savedB.getCourseSelections().addAll(choicesB);
        List<CourseDto.CourseInfo> result = courseService.getTeamCourses("1234");
//        assertThat(result.size()).isEqualTo(2);
        System.out.println("result = " + result);
    }

    @DisplayName("수업을 삭제할 수 있어야 한다")
    @Test
    public void deleteCourseTest() {
        int result = courseService.deleteCourse(new CourseIdDto(6L));
        int result2 = courseService.deleteCourse(new CourseIdDto(20L));
//        assertThat(result).isEqualTo(1);
//        assertThat(result2).isEqualTo(0);
//        assertThat(courseRepository.findAll().size()).isEqualTo(2);
    }
}
