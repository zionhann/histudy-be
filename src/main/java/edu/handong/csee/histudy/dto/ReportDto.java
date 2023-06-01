package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Image;
import edu.handong.csee.histudy.domain.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportDto {

    @Schema(description = "List of reports", type = "array")
    private List<Info> reports;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Info {

        public Info(Report entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.group = entity.getTeam().getTag();
            this.totalMinutes = entity.getTotalMinutes();
            this.participants = entity.getParticipants()
                    .stream()
                    .map(p -> p.getUser().getSid())
                    .toList();
            this.courses = entity.getStudies()
                    .stream()
                    .map(s -> s.getCourse().getName())
                    .toList();
        }

        @Schema(description = "Report ID", type = "number", example = "1")
        private Long id;

        @Schema(description = "Report Title", example = "Week 15 Report")
        private String title;

        @Schema(description = "Group(Team) ID of the report", type = "number", example = "10")
        private Integer group;

        @Schema(description = "Total minutes of the report", type = "number", example = "60")
        private long totalMinutes;

        @Schema(description = "Participant SIDs of the report", type = "array", example = "[\"20200001\", \"20200002\"]")
        private List<String> participants;

        @Schema(description = "Course names of the report", type = "array", example = "[\"OOP\", \"OS\"]")
        private List<String> courses;
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Basic {
        public Basic(Report report) {
            this.id = report.getId();
            this.title = report.getTitle();
            this.regDate = report.getLastModifiedDate().toString();
            this.time = report.getTotalMinutes();

        }

        private long id;
        private String title;
        private String regDate;
        private long time;
    }
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    @Builder
    public static class Detail {
        private String title;
        private List<UserDto.Basic> members;
        private long time;
        private String content;
        private List<ImageDto> img;
    }



}
