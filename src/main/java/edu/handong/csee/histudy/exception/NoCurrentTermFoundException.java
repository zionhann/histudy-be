package edu.handong.csee.histudy.exception;

public class NoCurrentTermFoundException extends RuntimeException {
  public NoCurrentTermFoundException() {
    super("설정된 현재 학기가 없습니다.");
  }
}
