package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Banner extends BaseTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bannerId;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private String imagePath;

  private String redirectUrl;

  @Column(nullable = false)
  private boolean active;

  @Column(nullable = false)
  private int displayOrder;

  @Builder
  public Banner(
      String label,
      String imagePath,
      String redirectUrl,
      boolean active,
      int displayOrder) {
    this.label = label;
    this.imagePath = imagePath;
    this.redirectUrl = redirectUrl;
    this.active = active;
    this.displayOrder = displayOrder;
  }

  public void updateLabel(String label) {
    this.label = label;
  }

  public void updateImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public void updateRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public void updateActive(boolean active) {
    this.active = active;
  }

  public void updateDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
