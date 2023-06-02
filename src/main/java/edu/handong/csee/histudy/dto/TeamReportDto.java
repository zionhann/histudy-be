package edu.handong.csee.histudy.dto;

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
    private long group;
    private List<UserDto.Basic> members;
    private long totalTime;
    List<ReportDto.Basic> reports;
}
