package edu.handong.csee.histudy.banner.adapter.in.response;

import edu.handong.csee.histudy.banner.domain.Banner;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicBannerResponse {

  private Long id;

  private String imageUrl;

  private String redirectUrl;

  public PublicBannerResponse(Banner banner, String imageUrl) {
    this.id = banner.getBannerId();
    this.imageUrl = imageUrl;
    this.redirectUrl = banner.getRedirectUrl();
  }
}
