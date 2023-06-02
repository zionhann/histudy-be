package edu.handong.csee.histudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamReportDto {

    @Schema(description = "Team ID", example = "1", type = "number")
    private long group;

    @Schema(description = "Team members", type = "array")
    private List<UserDto.UserBasic> members;

    @Schema(description = "Total time studied", type = "number", example = "300")
    private long totalTime;

    @Schema(description = "Reports information", type = "array", example = "120")
    List<ReportDto.ReportBasic> reports;
}
