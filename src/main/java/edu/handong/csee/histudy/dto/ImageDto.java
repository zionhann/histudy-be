package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.ReportImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageDto {

  public ImageDto(ReportImage entity) {
    this.id = entity.getId();
    this.url = entity.getPath();
  }

  @Schema(description = "Image ID", example = "1", type = "number")
  private long id;

  @Schema(description = "Image URL", example = "/path/to/image.png")
  private String url;
}
