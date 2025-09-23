package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyApplicant extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long studyApplicantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "academic_term_id")
  private AcademicTerm academicTerm;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_group_id")
  private StudyGroup studyGroup;

  @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
  private List<StudyPartnerRequest> partnerRequests = new ArrayList<>();

  @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
  private List<PreferredCourse> preferredCourses = new ArrayList<>();

  public static StudyApplicant of(
      AcademicTerm academicTerm, User user, List<User> partners, List<Course> preferredCourses) {
    StudyApplicant applicant = new StudyApplicant(academicTerm, user);
    partners.forEach(partner -> StudyPartnerRequest.of(applicant, partner, RequestStatus.PENDING));

    preferredCourses.stream()
        .collect(Collectors.toUnmodifiableMap(Function.identity(), preferredCourses::indexOf))
        .forEach((course, priority) -> PreferredCourse.of(applicant, course, priority));

    return applicant;
  }

  public void changeStatusIfReceivedBy(
      User user, Function<StudyPartnerRequest, RequestStatus> changeStatus) {
    this.partnerRequests.stream()
        .filter(request -> request.isReceivedBy(user))
        .findFirst()
        .ifPresent(changeStatus::apply);
  }

  private StudyApplicant(AcademicTerm academicTerm, User user) {
    this.academicTerm = academicTerm;
    this.user = user;
    this.studyGroup = null;
  }

  public List<User> getRequestedUsers() {
    return this.partnerRequests.stream().map(StudyPartnerRequest::getReceiver).toList();
  }

  public void joinStudyGroup(StudyGroup studyGroup) {
    if (this.isInSameGroup(studyGroup)) {
      return;
    }
    this.leaveStudyGroup();
    this.studyGroup = studyGroup;

    if (!studyGroup.getMembers().contains(this)) {
      studyGroup.getMembers().add(this);
    }
  }

  public void leaveStudyGroup() {
    if (!this.hasStudyGroup()) {
      return;
    }
    this.studyGroup.getMembers().remove(this);
    this.studyGroup.refreshGroupCourses();
    this.studyGroup = null;
  }

  public boolean hasStudyGroup() {
    return this.studyGroup != null;
  }

  public boolean isInSameGroup(StudyGroup studyGroup) {
    return this.hasStudyGroup() && this.studyGroup.equals(studyGroup);
  }
}
