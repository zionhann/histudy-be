package edu.handong.csee.histudy.choice;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class UserCourseTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    UserCourseRepository userCourseRepository;
    @Autowired
    StudyGroupRepository studyGroupRepository;

    @DisplayName("유저는 과목을 선택할 수 있어야 한다")
    @Test
    public void choiceTest() {
        Course course = Course.builder()
                .name("기초전자공학실험")
                .code("ECE20007")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(course);
        courseRepository.flush();
        Course courseB = Course.builder()
                .name("데이타구조")
                .code("ECE20010")
                .professor("김호준")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(courseB);
        courseRepository.flush();
        Course courseC = Course.builder()
                .name("자바프로그래밍언어")
                .code("ECE20017")
                .professor("남재창")
                .courseYear(2023)
                .semester(1)
                .build();
        courseRepository.save(courseC);
        User user = User.builder()
                .sid("22000328")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User saved = userRepository.save(user);
        StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(1, List.of(saved)));
        List<Long> courseIdxList = List.of(1L, 2L);
        List<Course> courses = courseIdxList.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<PreferredCourse> preferredCours = courses.stream().map(c -> userCourseRepository.save(new PreferredCourse(c, 0))).toList();
        user.getCourseSelections().addAll(preferredCours);

        assertThat(user.getCourseSelections().stream().map(PreferredCourse::getCourse).toList()).isNotNull();
//        assertThat(user.getChoices().size()).isEqualTo(2);
    }
}
