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
public class ManageReportCourseReportTestsGroup {

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

        StudyGroup studyGroup = new StudyGroup(1);
        GroupReport groupReport = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .studyGroup(studyGroup)
                .participants(List.of(user1))
                .courses(List.of(course))
                .build();
        user1.belongTo(studyGroup);
        user2.belongTo(studyGroup);

        // then
        assertEquals(2, groupReport.getStudyGroup().getMembers().size());
        assertEquals(1, groupReport.getParticipants().size());
    }

    @DisplayName("보고서 작성시 활동 시간을 저장할 수 있다.")
    @Test
    public void ManageReportTests_61() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();
        GroupReport groupReport = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .studyGroup(new StudyGroup(1))
                .participants(List.of(User.builder().build()))
                .courses(List.of(course))
                .build();

        // then
        assertEquals(60, groupReport.getTotalMinutes());
    }

    @DisplayName("활동 시간은 그룹 전체 활동 시간에도 집계되어야 한다.")
    @Test
    public void ManageReportTests_76() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();
        StudyGroup studyGroup = new StudyGroup(1);

        GroupReport groupReport1 = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(30)
                .studyGroup(studyGroup)
                .participants(List.of(User.builder().build()))
                .courses(List.of(course))
                .build();
        GroupReport groupReport2 = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(60)
                .studyGroup(studyGroup)
                .participants(List.of(User.builder().build()))
                .courses(List.of(course))
                .build();
        // then
        assertEquals(90, studyGroup.getTotalMinutes());
    }

    @DisplayName("이미지를 1개 이상 첨부할 수 있다.")
    @Test
    public void ManageReportTests_102() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();
        GroupReport groupReport = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(0)
                .studyGroup(new StudyGroup(1))
                .participants(List.of(User.builder().build()))
                .images(List.of("pathA", "pathB", "pathC"))
                .courses(List.of(course))
                .build();

        // then
        assertEquals(3, groupReport.getImages().size());
        assertEquals(Collections.nCopies(3, groupReport), groupReport.getImages()
                .stream()
                .map(Image::getGroupReport)
                .toList());
    }

    @DisplayName("이미지의 순서는 보장되어야 한다.")
    @Test
    public void ManageReportTests_122() {
        // given
        Course course = Course.builder()
                .name("courseName")
                .build();

        GroupReport groupReport = GroupReport.builder()
                .title("title")
                .content("content")
                .totalMinutes(0)
                .studyGroup(new StudyGroup(1))
                .participants(List.of(User.builder().build()))
                .images(List.of("pathA", "pathB", "pathC"))
                .courses(List.of(course))
                .build();

        // then
        assertEquals(3, groupReport.getImages().size());
        assertEquals(List.of("pathA", "pathB", "pathC"),
                groupReport.getImages()
                        .stream()
                        .map(Image::getPath)
                        .toList());
    }
}
