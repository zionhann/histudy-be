package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Banner;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BannerDto {

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class AdminBannerInfo {

    private Long id;

    private String label;

    private String imageUrl;

    private String redirectUrl;

    private boolean active;

    private int displayOrder;

    public AdminBannerInfo(Banner banner, String imageUrl) {
      this.id = banner.getBannerId();
      this.label = banner.getLabel();
      this.imageUrl = imageUrl;
      this.redirectUrl = banner.getRedirectUrl();
      this.active = banner.isActive();
      this.displayOrder = banner.getDisplayOrder();
    }
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class PublicBannerInfo {

    private Long id;

    private String imageUrl;

    private String redirectUrl;

    public PublicBannerInfo(Banner banner, String imageUrl) {
      this.id = banner.getBannerId();
      this.imageUrl = imageUrl;
      this.redirectUrl = banner.getRedirectUrl();
    }
  }
}
