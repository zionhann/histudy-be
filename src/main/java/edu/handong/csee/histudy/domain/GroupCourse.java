package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupCourse extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long groupCourseId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_group_id")
  private StudyGroup studyGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  public GroupCourse(Course course, StudyGroup studyGroup) {
    this.course = course;
    this.studyGroup = studyGroup;

    if (!studyGroup.getCourses().contains(this)) {
      studyGroup.getCourses().add(this);
    }
  }
}
