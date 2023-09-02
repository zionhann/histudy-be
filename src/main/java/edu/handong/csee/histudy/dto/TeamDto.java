package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.GroupReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TeamDto {

    @Schema(description = "Team ID", example = "1", type = "number")
    private Long group; // id

    private int tag;

    @Schema(description = "Team members", type = "array")
    private List<UserDto.UserInfo> members;

    @Schema(description = "Number of reports created", type = "number", example = "5")
    private int reports;

    @Schema(description = "Total time studied", type = "number", example = "120")
    private long times; // totalMinutes

    public TeamDto(StudyGroup studyGroup) {
        this.group = studyGroup.getId();
        this.tag = studyGroup.getTag();
        this.members = studyGroup.getMembers().stream()
                .map(UserDto.UserInfo::new).toList();
        this.reports = studyGroup.getReports().size();
        this.times = studyGroup.getReports().stream()
                .mapToLong(GroupReport::getTotalMinutes).sum();
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MatchResults {

        @Schema(description = "List of matched teams", type = "array")
        private List<TeamMatching> matchedTeams;

        @Schema(description = "List of unmatched users", type = "array")
        private List<UserDto.UserInfo> unmatchedUsers;

        public MatchResults(List<StudyGroup> matchedStudyGroups, List<UserDto.UserInfo> unmatchedUsers) {
            this.matchedTeams = matchedStudyGroups.stream()
                    .map(TeamMatching::new).toList();
            this.unmatchedUsers = unmatchedUsers;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TeamMatching {

        @Schema(description = "Team ID", example = "1", type = "number")
        private Long id;

        @Schema(description = "Team tag", example = "1", type = "number")
        private Integer tag;

        @Schema(description = "Team members", type = "array")
        private List<UserDto.UserMatching> users;

        @Schema(description = "Team courses", type = "array")
        private List<CourseDto.CourseInfo> courses;

        public TeamMatching(StudyGroup studyGroup) {
            this.id = studyGroup.getId();
            this.tag = studyGroup.getTag();
            this.users = studyGroup.getMembers().stream()
                    .map(UserDto.UserMatching::new).toList();
            this.courses = studyGroup.getGroupCourses().stream()
                    .map(enroll -> new CourseDto.CourseInfo(enroll.getCourse()))
                    .toList();
        }
    }
}
