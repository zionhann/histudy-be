package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.BannerReorderForm;
import edu.handong.csee.histudy.exception.MissingParameterException;
import java.util.List;

public record ReorderBannersCommand(List<Long> orderedIds) {

  public ReorderBannersCommand {
    if (orderedIds == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
  }

  public static ReorderBannersCommand from(BannerReorderForm form) {
    if (form == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
    return new ReorderBannersCommand(
        form.getOrderedIds() == null ? List.of() : List.copyOf(form.getOrderedIds()));
  }
}
