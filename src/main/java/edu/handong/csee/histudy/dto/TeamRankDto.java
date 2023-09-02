package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Image;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamRankDto {

    @Schema(description = "List of teams", type = "array")
    private List<TeamInfo> teams;

    public TeamRankDto(List<TeamInfo> teamInfos) {
        this.teams = teamInfos;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TeamInfo {
        @Schema(description = "Team Tag", example = "1", type = "number")
        private int id;

        @Schema(description = "Team members", type = "array", example = "[\"John Doe\", \"Jane Doe\"]")
        private List<String> members;

        @Schema(description = "Number of reports created", type = "number", example = "5")
        private int reports;

        @Schema(description = "Total time studied", type = "number", example = "120")
        private long totalMinutes;

        @Schema(description = "Team thumbnail(from the latest report)", type = "string", example = "https://i.imgur.com/3QXm2oF.png")
        private String thumbnail;

        public TeamInfo(StudyGroup studyGroup) {
            this.id = studyGroup.getTag();
            this.members = studyGroup.getMembers()
                    .stream()
                    .map(User::getName)
                    .toList();
            this.reports = studyGroup.getReports().size();
            this.totalMinutes = studyGroup.getTotalMinutes();
            this.thumbnail = studyGroup.getReports()
                    .stream()
                    .reduce((first, second) -> second)
                    .flatMap((report -> report.getImages()
                            .stream()
                            .findFirst()
                            .map(Image::getPath)))
                    .orElse(null);
        }
    }
}
