package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long groupMemberId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_group_id")
  private StudyGroup studyGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  public GroupMember(StudyGroup studyGroup, User user) {
    this.studyGroup = studyGroup;
    this.user = user;
  }

  public static GroupMember of(StudyGroup studyGroup, StudyApplicant applicant) {
    applicant.markAsGrouped(studyGroup);
    GroupMember groupMember = new GroupMember(studyGroup, applicant.getUser());
    groupMember.studyGroup.getMembers().add(groupMember);
    return groupMember;
  }

  public void remove() {
    if (this.studyGroup == null) {
      return;
    }
    this.studyGroup.getMembers().remove(this);
    this.studyGroup = null;
  }
}
