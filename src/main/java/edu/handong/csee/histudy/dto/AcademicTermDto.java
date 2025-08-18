package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.TermType;
import java.util.List;

public record AcademicTermDto(List<AcademicTermItem> academicTerms) {
  public record AcademicTermItem(long academicTermId, int year, TermType semester) {}
}
