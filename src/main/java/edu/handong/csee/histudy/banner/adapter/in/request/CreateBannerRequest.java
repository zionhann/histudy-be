package edu.handong.csee.histudy.banner.adapter.in.request;

import edu.handong.csee.histudy.banner.application.command.CreateBannerCommand;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateBannerRequest {

  private String label;

  private String redirectUrl;

  private Boolean active;

  private MultipartFile image;

  public CreateBannerCommand toCommand() {
    return new CreateBannerCommand(label, redirectUrl, active == null || active, image);
  }
}
