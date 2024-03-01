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
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  User participant;

  @ManyToOne(fetch = FetchType.LAZY)
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
