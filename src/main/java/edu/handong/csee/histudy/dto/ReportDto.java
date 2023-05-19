package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class ReportDto {

    @AllArgsConstructor
    @Getter
    public static class Response {

        public Response(Report entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.group = entity.getGroup().getTag();
            this.totalMinutes = entity.getTotalMinutes();
            this.participants = entity.getParticipants()
                    .stream()
                    .map(p -> p.getUser().getSid())
                    .toList();
        }

        private Long id;
        private String title;
        private Integer group;
        private long totalMinutes;
        private List<String> participants;
    }
}
