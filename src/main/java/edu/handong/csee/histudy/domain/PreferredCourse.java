package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreferredCourse extends BaseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long preferredCourseId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_applicant_id")
  private StudyApplicant applicant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  private Integer priority;

  @Builder
  public PreferredCourse(StudyApplicant applicant, Course course, Integer priority) {
    this.applicant = applicant;
    this.course = course;
    this.priority = priority;
  }

  public static PreferredCourse of(StudyApplicant applicant, Course course, Integer priority) {
    PreferredCourse preferredCourse = new PreferredCourse(applicant, course, priority);

    preferredCourse.applicant = applicant;
    applicant.getPreferredCourses().add(preferredCourse);
    return preferredCourse;
  }
}
