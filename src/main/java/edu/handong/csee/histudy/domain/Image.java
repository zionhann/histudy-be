package edu.handong.csee.histudy.domain;

import edu.handong.csee.histudy.dto.ImageDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    private Report report;

    @Builder
    public Image(String path, Report report) {
        this.path = path;
        this.report = report;
    }
    public ImageDto toDto() {
        ImageDto dto = new ImageDto();
        dto.setId(this.id);
        dto.setUrl(this.path);
        return dto;
    }
}
