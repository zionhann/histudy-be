package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "banner_id")
  private Long bannerId;

  @Column(name = "label")
  private String label;

  @Column(name = "image_path")
  private String imagePath;

  @Column(name = "redirect_url")
  private String redirectUrl;

  @Column(name = "active")
  private boolean active;

  @Column(name = "display_order")
  private int displayOrder;

  @Builder
  public Banner(
      String label, String imagePath, String redirectUrl, boolean active, int displayOrder) {
    this.label = label;
    this.imagePath = imagePath;
    this.redirectUrl = redirectUrl;
    this.active = active;
    this.displayOrder = displayOrder;
  }

  public static Banner create(
      String label, String imagePath, String redirectUrl, boolean active, int displayOrder) {
    return Banner.builder()
        .label(label)
        .imagePath(imagePath)
        .redirectUrl(redirectUrl)
        .active(active)
        .displayOrder(displayOrder)
        .build();
  }

  public void update(String label, String redirectUrl, boolean active) {
    this.label = label;
    this.redirectUrl = redirectUrl;
    this.active = active;
  }

  public void replaceImage(String imagePath) {
    this.imagePath = imagePath;
  }

  public void changeDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
