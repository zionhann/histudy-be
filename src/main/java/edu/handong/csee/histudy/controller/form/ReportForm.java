package edu.handong.csee.histudy.controller.form;

import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Builder
public class ReportForm {

    private final String title;
    private final String content;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final List<String> participants;

    public Report toEntity(Team team, List<Optional<User>> participants) {
        return Report.builder()
                .title(title)
                .content(content)
                .startTime(startTime)
                .endTime(endTime)
                .team(team)
                .participants(participants.stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList())
                .build();
    }
}
