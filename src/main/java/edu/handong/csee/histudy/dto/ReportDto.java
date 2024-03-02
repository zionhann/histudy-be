package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.StudyCourse;
import edu.handong.csee.histudy.domain.StudyParticipant;
import edu.handong.csee.histudy.domain.StudyReport;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportDto {

  @Schema(description = "List of reports", type = "array")
  private List<ReportInfo> reports;

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ReportInfo {

    public ReportInfo(StudyReport entity, Map<Long, String> imageFullPaths) {
      this.id = entity.getStudyReportId();
      this.title = entity.getTitle();
      this.content = entity.getContent();
      this.totalMinutes = entity.getTotalMinutes();
      this.participants =
          entity.getParticipants().stream()
              .map(StudyParticipant::getParticipant)
              .map(UserDto.UserBasic::new)
              .toList();
      this.courses =
          entity.getCourses().stream()
              .map(StudyCourse::getCourse)
              .map(CourseDto.BasicCourseInfo::new)
              .toList();
      this.images =
          entity.getImages().stream()
              .map(img -> new ImageDto(img.getId(), imageFullPaths.get(img.getId())))
              .toList();
      this.regDate = entity.getLastModifiedDate().toString();
    }

    @Schema(description = "Report ID", type = "number", example = "1")
    private Long id;

    @Schema(description = "Report Title", example = "Week 15 Report")
    private String title;

    @Schema(description = "Report Content", example = "This is a report for week 15")
    private String content;

    @Schema(description = "Total minutes of the report", type = "number", example = "60")
    private long totalMinutes;

    @Schema(description = "Participant SIDs of the report", type = "array")
    private List<UserDto.UserBasic> participants;

    @Schema(
        description = "Course names of the report",
        type = "array",
        example = "[\"OOP\", \"OS\"]")
    private List<CourseDto.BasicCourseInfo> courses;

    @Schema(description = "Images of the report", type = "array")
    private List<ImageDto> images;

    @Schema(description = "Report Last Modified Date", example = "2021-06-01 00:00:00")
    private String regDate;
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ReportBasic {
    public ReportBasic(StudyReport studyReport, Map<Long, String> imageMap) {
      this.id = studyReport.getStudyReportId();
      this.title = studyReport.getTitle();
      this.regDate = studyReport.getLastModifiedDate().toString();
      this.totalMinutes = studyReport.getTotalMinutes();
      this.thumbnail =
          studyReport.getImages().stream()
              .findFirst()
              .map(image -> imageMap.get(image.getId()))
              .orElse(null);
    }

    @Schema(description = "Report ID", type = "number", example = "1")
    private long id;

    @Schema(description = "Report Title", example = "Week 15 Report")
    private String title;

    @Schema(description = "Report Last Modified Date", example = "2021-06-01 00:00:00")
    private String regDate;

    @Schema(description = "Total minutes of the report", type = "number", example = "60")
    private long totalMinutes;

    @Schema(
        description = "Thumbnail of the report",
        example = "https://histudy.s3.ap-northeast-2.amazonaws.com/2021-06-01-00-00-00-1")
    private String thumbnail;
  }
}
