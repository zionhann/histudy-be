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
  private Long studyCourseId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_report_id")
  private StudyReport studyReport;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  @Builder
  public StudyCourse(StudyReport studyReport, Course course) {
    this.studyReport = studyReport;
    this.course = course;

    studyReport.getCourses().add(this);
  }
}
