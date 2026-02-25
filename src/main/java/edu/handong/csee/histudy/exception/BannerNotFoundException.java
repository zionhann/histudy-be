package edu.handong.csee.histudy.exception;

public class BannerNotFoundException extends RuntimeException {

  public BannerNotFoundException() {
    super("해당 배너를 찾을 수 없습니다.");
  }
}
