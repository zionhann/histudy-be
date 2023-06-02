package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Image;
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

    private long id;
    private String url;
}
