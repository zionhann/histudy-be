package edu.handong.csee.histudy.controller.form;

import edu.handong.csee.histudy.domain.GroupCourse;
import edu.handong.csee.histudy.domain.GroupReport;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportForm {

    @Schema(description = "Report Title", example = "Week 15 Report")
    private String title;

    @Schema(description = "Report Content", example = "This is a report for week 15.")
    private String content;

    @Schema(description = "Total minutes of the report", type = "number", example = "60")
    private Long totalMinutes;

    /**
     * Contains student ID(PK)
     */
    @Schema(description = "Participant ID(PK) of the report", type = "array", example = "[1, 2]")
    private List<Long> participants = new ArrayList<>();

    /**
     * Contains image URL
     */
    @Schema(description = "Image URLs of the report", type = "array", example = "[\"https://histudy.s3.ap-northeast-2.amazonaws.com/1.jpg\", \"https://histudy.s3.ap-northeast-2.amazonaws.com/2.jpg\"]")
    private List<String> images;

    /**
     * Contains course ID
     */
    @Schema(description = "Course IDs of the report", type = "array", example = "[1, 2]")
    private List<Long> courses = new ArrayList<>();

    public GroupReport toEntity(StudyGroup studyGroup, List<User> participants, List<GroupCourse> courses) {
        return GroupReport.builder()
                .title(title)
                .content(content)
                .totalMinutes(totalMinutes)
                .studyGroup(studyGroup)
                .participants(participants)
                .images(images)
                .courses(courses)
                .build();
    }
}
