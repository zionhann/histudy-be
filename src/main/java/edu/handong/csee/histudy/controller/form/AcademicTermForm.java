package edu.handong.csee.histudy.controller.form;

import edu.handong.csee.histudy.domain.TermType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicTermForm {

  private Integer year;

  private TermType semester;
}
