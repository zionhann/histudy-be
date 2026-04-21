package edu.handong.csee.histudy.banner.adapter.in.request;

import edu.handong.csee.histudy.banner.application.command.UpdateBannerCommand;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateBannerRequest {

  private String label;

  private String redirectUrl;

  private Boolean active;

  private MultipartFile image;

  public UpdateBannerCommand toCommand(Long bannerId) {
    return new UpdateBannerCommand(bannerId, label, redirectUrl, active, image);
  }
}
