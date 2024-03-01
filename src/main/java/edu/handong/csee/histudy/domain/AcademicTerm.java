package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicTerm extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "academic_year")
  private Integer year;

  @Enumerated(EnumType.STRING)
  private TermType semester = TermType.NONE;

  @Builder.Default private Boolean isCurrent = false;
}
