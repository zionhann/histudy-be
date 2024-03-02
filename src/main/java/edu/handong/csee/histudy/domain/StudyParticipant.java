package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyParticipant extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long studyParticipantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  User participant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_report_id")
  StudyReport studyReport;

  public StudyParticipant(User participant, StudyReport studyReport) {
    this.participant = participant;
    this.studyReport = studyReport;
    studyReport.getParticipants().add(this);
  }

  public static StudyParticipant of(User participant, StudyReport studyReport) {
    return new StudyParticipant(participant, studyReport);
  }
}
