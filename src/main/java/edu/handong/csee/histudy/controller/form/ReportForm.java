package edu.handong.csee.histudy.controller.form;

import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import lombok.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportForm {

    private String title;
    private String content;
    private long totalMinutes;
    private List<String> participants;
    private List<String> images;
    private List<Long> courses; // courseId 넘겨줘야할 것 같아요

    public Report toEntity(Team team, List<Optional<User>> participants) {
        return Report.builder()
                .title(title)
                .content(content)
                .totalMinutes(totalMinutes)
                .team(team)
                .participants(participants.stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList())
                .images(images)
                .build();
    }
}
