package edu.handong.csee.histudy.choice;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.ChoiceRepository;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ChoiceTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ChoiceRepository choiceRepository;
    @Autowired
    TeamRepository teamRepository;
    @DisplayName("유저는 과목을 선택할 수 있어야 한다")
    @Test
    public void choiceTest() {
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
        List<Long> courseIdxList = List.of(1L,2L);
        List<Course> courses = courseIdxList.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Choice> choices = courses.stream().map(c -> choiceRepository.save(new Choice(user,c))).toList();
        user.getChoices().addAll(choices);

        assertThat(user.getChoices().stream().map(Choice::getCourse).toList()).isNotNull();
        assertThat(user.getChoices().size()).isEqualTo(2);
    }
}
