package edu.handong.csee.histudy.course;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.repository.ChoiceRepository;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.CourseService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class CourseServiceTest {
    @Autowired
    CourseService courseService;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ChoiceRepository choiceRepository;

    @DisplayName("팀내에 인원들의 과목들을 불러들여야 한다")
    @Test
    @Transactional
    public void teamCourseTest() {
        User user = User.builder()
                .id("123")
                .sid("22000328")
                .accessToken("1234")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User saved = userRepository.save(user);
        Team team = teamRepository.save(new Team(1));
        saved.belongTo(team);
        List<Long> courseIdxList = List.of(1L);
        List<Course> courses = courseIdxList.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Choice> choices = courses.stream().map(c -> choiceRepository.save(new Choice(saved,c))).toList();
        saved.getChoices().addAll(choices);
        User userB = User.builder()
                .id("124")
                .sid("22000329")
                .accessToken("1235")
                .email("b@b.com")
                .role(Role.USER)
                .build();
        User savedB = userRepository.save(userB);
        savedB.belongTo(team);
        List<Long> courseIdxListB = List.of(1L,2L);
        List<Course> coursesB = courseIdxListB.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Choice> choicesB = coursesB.stream().map(c -> choiceRepository.save(new Choice(savedB,c))).toList();
        savedB.getChoices().addAll(choicesB);
        List<CourseDto> result = courseService.getTeamCourses("1234");
        assertThat(result.size()).isEqualTo(2);
        System.out.println("result = " + result);
    }
}
