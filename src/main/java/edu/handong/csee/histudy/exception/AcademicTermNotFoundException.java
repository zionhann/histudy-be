package edu.handong.csee.histudy.exception;

public class AcademicTermNotFoundException extends RuntimeException {
  public AcademicTermNotFoundException() {
    super("학기 ID를 찾을 수 없습니다.");
  }
}
