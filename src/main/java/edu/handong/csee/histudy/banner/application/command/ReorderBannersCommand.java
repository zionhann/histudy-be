package edu.handong.csee.histudy.banner.application.command;

import edu.handong.csee.histudy.exception.MissingParameterException;
import java.util.List;

public record ReorderBannersCommand(List<Long> orderedIds) {

  public ReorderBannersCommand {
    if (orderedIds == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
  }
}
