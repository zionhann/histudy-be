package edu.handong.csee.histudy.report;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ReportServiceTest {
    @Autowired
    ReportService reportService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;

    @DisplayName("보고서 생성시 과목을 선택할 수 있다")
    @Transactional
    @Test
    public void reportServiceTest() {
        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .participants(List.of("22000328"))
                .courses(List.of(1L,2L,3L))
                .build();
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
        ReportDto.Response response = reportService.createReport(form,"1234");
        assertThat(response.getCourses().size()).isEqualTo(3);
        System.out.println("response.getCourses() = " + response.getCourses());
    }
}
