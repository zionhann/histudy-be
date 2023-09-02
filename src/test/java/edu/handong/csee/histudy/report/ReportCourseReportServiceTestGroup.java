package edu.handong.csee.histudy.report;

import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@SpringBootTest
public class ReportCourseReportServiceTestGroup {
    @Autowired
    ReportService reportService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudyGroupRepository studyGroupRepository;
    @Autowired
    CourseRepository courseRepository;

    @DisplayName("보고서 생성시 과목을 선택할 수 있다")
    @Transactional
    @Test
    public void reportServiceTest() {
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
        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .totalMinutes(60L)
                .participants(List.of("22000328"))
                .courses(List.of(course.getId(), courseB.getId(), courseC.getId()))
                .build();
        User user = User.builder()
                .sid("22000328")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User saved = userRepository.save(user);
        saved.belongTo(new StudyGroup(1));
        ReportDto.ReportInfo response = reportService.createReport(form, "a@a.com");
        assertThat(response.getCourses().size()).isEqualTo(3);
    }

    @DisplayName("보고서 상세조회를 할 수 있다")
    @Test
    @Transactional
    public void reportDetailTest() {
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
        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .totalMinutes(60L)
                .participants(List.of("22000328"))
                .courses(List.of(1L, 2L, 3L))
                .build();
        User user = User.builder()
                .sid("22000328")
                .email("a@a.com")
                .role(Role.USER)
                .build();
        User saved = userRepository.save(user);
        StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(1));
        saved.belongTo(studyGroup);
        ReportDto.ReportInfo response = reportService.createReport(form, "a@a.com");
        ReportDto.ReportInfo detail = reportService.getReport(response.getId())
                .orElseThrow();
        assertThat(detail.getParticipants().size()).isEqualTo(1);
        assertThat(detail.getTitle()).isEqualTo("title");
        assertThat(detail.getContent()).isEqualTo("content");
    }
}
