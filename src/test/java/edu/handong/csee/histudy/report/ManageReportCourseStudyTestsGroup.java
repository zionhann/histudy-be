package edu.handong.csee.histudy.report;

import edu.handong.csee.histudy.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("dev")
@Transactional
public class ManageReportCourseStudyTestsGroup {

    @DisplayName("보고서 작성시 참여한 인원을 선택할 수 있다.")
    @Test
    public void ManageReportTests_16() {
        // given
        User user1 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user1")
                .role(Role.USER)
                .build();
        User user2 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user2")
                .role(Role.USER)
                .build();
        Course course = Course.builder()
                .name("courseName")
                .build();

        user1.selectCourse(List.of(course));
        user2.selectCourse(List.of(course));
        StudyGroup studyGroup = new StudyGroup(1, List.of(user1, user2));

        StudyReport studyReport = StudyReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .studyGroup(studyGroup)
                .participants(List.of(user1))
                .courses(studyGroup.getCourses())
                .build();

        // then
        assertEquals(2, studyReport.getStudyGroup().getMembers().size());
        assertEquals(1, studyReport.getParticipants().size());
    }

    @DisplayName("보고서 작성시 활동 시간을 저장할 수 있다.")
    @Test
    public void ManageReportTests_61() {
        // given
        User user1 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user1")
                .role(Role.USER)
                .build();
        Course course = Course.builder()
                .name("courseName")
                .build();

        user1.selectCourse(List.of(course));
        StudyGroup studyGroup = new StudyGroup(1, List.of(user1));
        StudyReport studyReport = StudyReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .studyGroup(studyGroup)
                .participants(List.of(User.builder().build()))
                .courses(studyGroup.getCourses())
                .build();

        // then
        assertEquals(60, studyReport.getTotalMinutes());
    }

    @DisplayName("활동 시간은 그룹 전체 활동 시간에도 집계되어야 한다.")
    @Test
    public void ManageReportTests_76() {
        // given
        User user1 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user1")
                .role(Role.USER)
                .build();
        Course course = Course.builder()
                .name("courseName")
                .build();

        user1.selectCourse(List.of(course));
        StudyGroup studyGroup = new StudyGroup(1, List.of(user1));

        StudyReport studyReport1 = StudyReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(30)
                .studyGroup(studyGroup)
                .participants(studyGroup.getMembers())
                .courses(studyGroup.getCourses())
                .build();
        StudyReport studyReport2 = StudyReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .studyGroup(studyGroup)
                .participants(studyGroup.getMembers())
                .courses(studyGroup.getCourses())
                .build();
        // then
        assertEquals(90, studyGroup.getTotalMinutes());
    }

    @DisplayName("이미지를 1개 이상 첨부할 수 있다.")
    @Test
    public void ManageReportTests_102() {
        // given
        User user1 = User.builder()
                .sid("22300000")
                .email("test@histudy.com")
                .name("user1")
                .role(Role.USER)
                .build();
        Course course = Course.builder()
                .name("courseName")
                .build();

        user1.selectCourse(List.of(course));
        StudyGroup studyGroup = new StudyGroup(1, List.of(user1));
        StudyReport studyReport = StudyReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(0)
                .studyGroup(studyGroup)
                .participants(studyGroup.getMembers())
                .images(List.of("pathA", "pathB", "pathC"))
                .courses(studyGroup.getCourses())
                .build();

        // then
        assertEquals(3, studyReport.getImages().size());
        assertEquals(Collections.nCopies(3, studyReport), studyReport.getImages()
                .stream()
                .map(ReportImage::getStudyReport)
                .toList());
    }

    @DisplayName("이미지의 순서는 보장되어야 한다.")
    @Test
    public void ManageReportTests_122() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();

        StudyGroup studyGroup = new StudyGroup(1, List.of());
        StudyReport studyReport = StudyReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(0)
                .studyGroup(studyGroup)
                .participants(studyGroup.getMembers())
                .images(List.of("pathA", "pathB", "pathC"))
                .courses(studyGroup.getCourses())
                .build();

        // then
        assertEquals(3, studyReport.getImages().size());
        assertEquals(List.of("pathA", "pathB", "pathC"),
                studyReport.getImages()
                        .stream()
                        .map(ReportImage::getPath)
                        .toList());
    }
}
