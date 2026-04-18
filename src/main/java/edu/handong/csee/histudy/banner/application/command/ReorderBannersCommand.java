package edu.handong.csee.histudy.banner.application.command;

import edu.handong.csee.histudy.banner.adapter.in.request.ReorderBannersRequest;
import edu.handong.csee.histudy.exception.MissingParameterException;
import java.util.List;

public record ReorderBannersCommand(List<Long> orderedIds) {

  public ReorderBannersCommand {
    if (orderedIds == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
  }

  public static ReorderBannersCommand from(ReorderBannersRequest request) {
    if (request == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
    return new ReorderBannersCommand(
        request.getOrderedIds() == null ? List.of() : List.copyOf(request.getOrderedIds()));
  }
}
