package edu.handong.csee.histudy.banner.adapter.in.request;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReorderBannersRequest {

  private List<Long> orderedIds;
}
