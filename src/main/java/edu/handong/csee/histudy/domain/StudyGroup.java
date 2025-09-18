package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import java.util.*;
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

  public static StudyGroup of(Integer tag, AcademicTerm current, List<StudyApplicant> applicants) {
    return new StudyGroup(tag, current, applicants.toArray(StudyApplicant[]::new));
  }

  protected StudyGroup(Integer tag, AcademicTerm academicTerm, StudyApplicant... applicants) {
    this.tag = tag;
    this.academicTerm = academicTerm;
    addMember(applicants);
  }

  public void addMember(StudyApplicant... applicants) {
    Arrays.stream(applicants).forEach(applicant -> applicant.joinStudyGroup(this));
    this.addCourse(this.members);
  }

  protected void addCourse(List<StudyApplicant> members) {
    if (!this.courses.isEmpty()) {
      this.courses.clear();
    }
    this.findCommonCourses(members).forEach(course -> new GroupCourse(course, this));
  }

  protected List<Course> findCommonCourses(List<StudyApplicant> members) {
    Map<Course, Long> courseCounts =
        members.stream()
            .flatMap(member -> member.getPreferredCourses().stream())
            .collect(Collectors.groupingBy(PreferredCourse::getCourse, Collectors.counting()));

    List<Course> commonCourses =
        courseCounts.entrySet().stream()
            .filter(entry -> entry.getValue() >= COMMON_COURSE_THRESHOLD)
            .map(Map.Entry::getKey)
            .toList();

    return commonCourses.isEmpty() ? new ArrayList<>(courseCounts.keySet()) : commonCourses;
  }
}
