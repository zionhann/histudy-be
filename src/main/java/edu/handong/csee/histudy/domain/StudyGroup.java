package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroup extends BaseTime {

  private static final int COMMON_COURSE_THRESHOLD = 2;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long studyGroupId;

  private Integer tag;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "academic_term_id")
  private AcademicTerm academicTerm;

  @OneToMany(
      mappedBy = "studyGroup",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<StudyApplicant> members = new ArrayList<>();

  @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GroupCourse> courses = new ArrayList<>();

  public static StudyGroup of(
      AtomicInteger tag, AcademicTerm current, StudyApplicant sender, StudyApplicant receiver) {
    if (sender.getStudyGroup() != null && receiver.getStudyGroup() != null) {
      // a -> b or a <- b
      assert sender.getStudyGroup().equals(receiver.getStudyGroup());
      return sender.getStudyGroup();
    } else if (sender.getStudyGroup() != null) {
      // (a <-> b) -> c
      return sender.getStudyGroup().assignMembers(receiver);
    } else if (receiver.getStudyGroup() != null) {
      // (a <-> b) <- c
      return receiver.getStudyGroup().assignMembers(sender);
    }
    return new StudyGroup(tag.getAndIncrement(), current, sender, receiver);
  }

  public static StudyGroup of(Integer tag, AcademicTerm current, List<StudyApplicant> applicants) {
    return new StudyGroup(tag, current, applicants.toArray(StudyApplicant[]::new));
  }

  protected StudyGroup(Integer tag, AcademicTerm academicTerm, StudyApplicant... applicants) {
    this.tag = tag;
    this.academicTerm = academicTerm;
    assignMembers(applicants);
  }

  public StudyGroup assignMembers(StudyApplicant... applicants) {
    Arrays.stream(applicants)
        .forEach(
            applicant -> {
              if (isInSameGroup(applicant)) {
                return;
              } else if (isAlreadyInOtherGroup(applicant)) {
                applicant.leaveStudyGroup();
              }
              applicant.joinStudyGroup(this);
            });
    assignGroupCourses();
    return this;
  }

  private boolean isAlreadyInOtherGroup(StudyApplicant applicant) {
    return applicant.getStudyGroup() != null && !applicant.getStudyGroup().equals(this);
  }

  public boolean isInSameGroup(StudyApplicant applicant) {
    return applicant.getStudyGroup() != null && applicant.getStudyGroup().equals(this);
  }

  private void assignGroupCourses() {
    if (!this.courses.isEmpty()) {
      this.courses.clear();
    }

    Map<Course, Long> courseCounts =
        this.members.stream()
            .flatMap(member -> member.getPreferredCourses().stream())
            .collect(Collectors.groupingBy(PreferredCourse::getCourse, Collectors.counting()));

    List<Course> commonCourses =
        courseCounts.entrySet().stream()
            .filter(entry -> entry.getValue() >= COMMON_COURSE_THRESHOLD)
            .map(Map.Entry::getKey)
            .toList();

    List<Course> courseCandidates =
        commonCourses.isEmpty() ? new ArrayList<>(courseCounts.keySet()) : commonCourses;

    courseCandidates.forEach(
        course -> {
          GroupCourse groupCourse = new GroupCourse(this, course);

          if (!this.courses.contains(groupCourse)) {
            this.courses.add(groupCourse);
          }
        });
  }
}
