package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Course extends BaseTime {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;

  private String name;

  private String professor;

  private int courseYear;

  private int semester;

  @Builder
  public Course(String name, String code, String professor, int courseYear, int semester) {
    this.name = name;
    this.code = code;
    this.professor = professor;
    this.courseYear = courseYear;
    this.semester = semester;
  }
}
