package edu.handong.csee.histudy.controller.form;

import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.Team;
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
     * Contains student ID
     */
    @Schema(description = "Participant SIDs of the report", type = "array", example = "[\"20200001\", \"20200002\"]")
    private List<String> participants = new ArrayList<>();

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

    public Report toEntity(Team team, List<User> participants, List<Course> courses) {
        return Report.builder()
                .title(title)
                .content(content)
                .totalMinutes(totalMinutes)
                .team(team)
                .participants(participants)
                .images(images)
                .courses(courses)
                .build();
    }
}
