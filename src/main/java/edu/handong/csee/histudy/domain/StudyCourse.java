package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StudyCourse extends BaseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private StudyReport studyReport;

  @ManyToOne(fetch = FetchType.LAZY)
  private Course course;

  @Builder
  public StudyCourse(StudyReport studyReport, Course course) {
    this.studyReport = studyReport;
    this.course = course;

    studyReport.getCourses().add(this);
  }
}
