package edu.handong.csee.histudy.exception;

import edu.handong.csee.histudy.domain.TermType;

public class DuplicateAcademicTermException extends RuntimeException {

  public DuplicateAcademicTermException(int year, TermType semester) {
    super("Academic term already exists for year " + year + " and semester " + semester);
  }
}
