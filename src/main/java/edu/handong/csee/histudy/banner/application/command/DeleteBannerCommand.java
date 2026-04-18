package edu.handong.csee.histudy.banner.application.command;

import edu.handong.csee.histudy.exception.MissingParameterException;

public record DeleteBannerCommand(Long bannerId) {

  public DeleteBannerCommand {
    if (bannerId == null) {
      throw new MissingParameterException("배너 ID는 필수입니다.");
    }
  }
}
