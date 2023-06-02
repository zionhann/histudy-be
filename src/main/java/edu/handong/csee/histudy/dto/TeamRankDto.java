package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamRankDto {

    @Schema(description = "Number of teams", type = "number", example = "5")
    private int count;

    @Schema(description = "List of teams", type = "array")
    private List<TeamInfo> teams;

    public TeamRankDto(List<TeamInfo> teamInfos) {
        this.count = teamInfos.size();
        this.teams = teamInfos;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TeamInfo {
        @Schema(description = "Team ID", example = "1", type = "number")
        private Long id;

        @Schema(description = "Team members", type = "array")
        private List<UserDto.UserBasic> members;

        @Schema(description = "Number of reports created", type = "number", example = "5")
        private int reports;

        @Schema(description = "Total time studied", type = "number", example = "120")
        private long totalMinutes;

        public TeamInfo(Team team) {
            this.id = team.getId();
            this.members = team.getUsers()
                    .stream()
                    .map(UserDto.UserBasic::new)
                    .toList();
            this.reports = team.getReports().size();
            this.totalMinutes = team.getTotalMinutes();
        }
    }
}
