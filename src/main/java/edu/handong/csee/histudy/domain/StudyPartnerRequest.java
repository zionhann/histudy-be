package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPartnerRequest extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private StudyApplicant sender;

  @ManyToOne(fetch = FetchType.LAZY)
  private User receiver;

  @Enumerated(EnumType.STRING)
  private RequestStatus requestStatus;

  public StudyPartnerRequest(StudyApplicant sender, User receiver, RequestStatus requestStatus) {
    this.sender = sender;
    this.receiver = receiver;
    this.requestStatus = requestStatus;

    sender.getPartnerRequests().add(this);
  }

  public static StudyPartnerRequest of(StudyApplicant sender, User receiver, RequestStatus status) {
    return new StudyPartnerRequest(sender, receiver, status);
  }

  public RequestStatus accept() {
    if (this.requestStatus == RequestStatus.PENDING) {
      this.requestStatus = RequestStatus.ACCEPTED;
    }
    return this.requestStatus;
  }

  public RequestStatus unfriend() {
    if (this.requestStatus == RequestStatus.ACCEPTED) {
      this.requestStatus = RequestStatus.PENDING;
    }
    return this.requestStatus;
  }

  public boolean isAccepted() {
    return requestStatus.equals(RequestStatus.ACCEPTED);
  }

  public boolean isPending() {
    return requestStatus.equals(RequestStatus.PENDING);
  }

  public boolean isReceivedBy(User user) {
    return this.receiver.equals(user);
  }
}
