package edu.handong.csee.histudy.dto;

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

    @Schema(description = "Team members", type = "array")
    private List<UserDto.UserInfo> members;

    @Schema(description = "Number of reports created", type = "number", example = "5")
    private int reports;

    @Schema(description = "Total time studied", type = "number", example = "120")
    private long times; // totalMinutes
}
