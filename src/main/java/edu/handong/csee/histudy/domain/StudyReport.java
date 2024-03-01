package edu.handong.csee.histudy.domain;

import static java.util.Objects.requireNonNullElse;

import edu.handong.csee.histudy.controller.form.ReportForm;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyReport extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Column(length = 2000)
  private String content;

  private long totalMinutes;

  @ManyToOne(fetch = FetchType.LAZY)
  private StudyGroup studyGroup;

  @OneToMany(mappedBy = "studyReport", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<StudyParticipant> participants = new ArrayList<>();

  @OneToMany(mappedBy = "studyReport", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReportImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "studyReport", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<StudyCourse> courses = new ArrayList<>();

  @Builder
  public StudyReport(
      String title,
      String content,
      long totalMinutes,
      StudyGroup studyGroup,
      List<User> participants,
      List<String> images,
      List<Course> courses) {
    this.title = title;
    this.content = content;
    this.totalMinutes = totalMinutes;

    this.studyGroup = studyGroup;
    this.add(participants);
    this.insert(images);
    this.study(courses);
  }

  private void study(List<Course> groupCourses) {
    if (!courses.isEmpty()) {
      courses.clear();
    }
    groupCourses.forEach(course -> new StudyCourse(this, course));
  }

  private void add(List<User> users) {
    if (!participants.isEmpty()) {
      participants.clear();
    }
    users.forEach(user -> StudyParticipant.of(user, this));
  }

  private void insert(List<String> images) {
    if (images == null) {
      return;
    } else if (images.isEmpty()) {
      this.images.clear();
      return;
    }
    this.images.clear();
    images.forEach(img -> new ReportImage(img, this));
  }

  public boolean update(
      ReportForm form, List<String> images, List<User> participants, List<Course> courses) {
    this.title = requireNonNullElse(form.getTitle(), this.title);
    this.content = requireNonNullElse(form.getContent(), this.content);
    this.totalMinutes = requireNonNullElse(form.getTotalMinutes(), this.totalMinutes);

    this.add(participants);
    this.insert(images);
    this.study(courses);

    return true;
  }
}
