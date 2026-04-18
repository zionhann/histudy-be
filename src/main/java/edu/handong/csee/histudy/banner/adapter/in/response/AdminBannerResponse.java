package edu.handong.csee.histudy.banner.adapter.in.response;

import edu.handong.csee.histudy.banner.domain.Banner;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminBannerResponse {

  private Long id;

  private String label;

  private String imageUrl;

  private String redirectUrl;

  private boolean active;

  private int displayOrder;

  public AdminBannerResponse(Banner banner, String imageUrl) {
    this.id = banner.getBannerId();
    this.label = banner.getLabel();
    this.imageUrl = imageUrl;
    this.redirectUrl = banner.getRedirectUrl();
    this.active = banner.isActive();
    this.displayOrder = banner.getDisplayOrder();
  }
}
