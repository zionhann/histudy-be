package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportImage extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reportImageId;

  private String path;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_report_id")
  private StudyReport studyReport;

  @Builder
  public ReportImage(String path, StudyReport studyReport) {
    this.path = path;
    this.studyReport = studyReport;

    studyReport.getImages().add(this);
  }
}
