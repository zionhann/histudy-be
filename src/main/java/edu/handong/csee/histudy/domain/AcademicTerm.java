package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicTerm extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "academic_year")
  private Integer year;

  @Enumerated(EnumType.STRING)
  private Season semester = Season.NONE;

  @Builder.Default private Boolean isCurrent = false;
}
