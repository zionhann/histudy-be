package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Image;
import edu.handong.csee.histudy.domain.Report;
import lombok.*;

import java.util.List;

public class ReportDto {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Response {

        public Response(Report entity) {
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

        private Long id;
        private String title;
        private Integer group;
        private long totalMinutes;
        private List<String> participants;
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
