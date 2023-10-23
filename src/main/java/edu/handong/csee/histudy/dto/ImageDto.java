package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Image;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageDto {

    public ImageDto(Image entity) {
        this.id = entity.getId();
        this.url = entity.getPath();
    }

    @Schema(description = "Image ID", example = "1", type = "number")
    private long id;

    @Schema(description = "Image URL", example = "/path/to/image.png")
    private String url;

    public void addPathToFilename(String imageBasePath) {
        this.url = imageBasePath + this.url;
    }
}
