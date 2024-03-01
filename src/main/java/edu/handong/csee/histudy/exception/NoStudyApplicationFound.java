package edu.handong.csee.histudy.exception;

public class NoStudyApplicationFound extends RuntimeException {

  public NoStudyApplicationFound() {
    super("스터디 신청 정보를 찾을 수 없습니다.");
  }
}
