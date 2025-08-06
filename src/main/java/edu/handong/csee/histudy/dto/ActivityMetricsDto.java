package edu.handong.csee.histudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityMetricsDto {

  @Schema(description = "Total number of study members", example = "50")
  private final long studyMembers;

  @Schema(description = "Total number of study groups", example = "10")
  private final long studyGroups;

  @Schema(description = "Total study hours across all groups", example = "120")
  private final long studyHours;

  @Schema(description = "Total number of study reports submitted", example = "35")
  private final long reports;
}
