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
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private AcademicTerm academicTerm;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
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

  public void changeStatusIfPartnerRequested(
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

  public void markAsGrouped(StudyGroup studyGroup) {
    this.studyGroup = studyGroup;
    this.user.changeRole(Role.MEMBER);
  }

  public void leaveGroup() {
    if (isNotMarkedAsGrouped()) {
      return;
    }
    this.studyGroup.removeMember(this.user);
    this.studyGroup = null;
    this.user.changeRole(Role.USER);
  }

  public boolean isNotMarkedAsGrouped() {
    return this.studyGroup == null;
  }

  public boolean isMarkedAsGrouped() {
    return this.studyGroup != null;
  }
}
