package edu.handong.csee.histudy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.Banner;
import edu.handong.csee.histudy.repository.jpa.JpaBannerRepository;
import edu.handong.csee.histudy.support.BaseRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BannerRepositoryTest extends BaseRepositoryTest {

  @Autowired private JpaBannerRepository bannerRepository;

  @Test
  void 활성배너조회시_표시순서대로반환() {
    // Given
    Banner hiddenBanner =
        Banner.builder()
            .label("hidden")
            .imagePath("banner/hidden.png")
            .active(false)
            .displayOrder(1)
            .build();

    Banner firstBanner =
        Banner.builder()
            .label("first")
            .imagePath("banner/first.png")
            .active(true)
            .displayOrder(1)
            .build();

    Banner secondBanner =
        Banner.builder()
            .label("second")
            .imagePath("banner/second.png")
            .active(true)
            .displayOrder(2)
            .build();

    bannerRepository.saveAll(List.of(hiddenBanner, secondBanner, firstBanner));

    // When
    List<Banner> results = bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc();

    // Then
    assertThat(results).hasSize(2);
    assertThat(results).allMatch(Banner::isActive);
    assertThat(results)
        .extracting(Banner::getDisplayOrder)
        .containsExactly(1, 2);
  }

  @Test
  void 전체배너조회시_표시순서대로반환() {
    // Given
    Banner thirdBanner =
        Banner.builder()
            .label("third")
            .imagePath("banner/third.png")
            .active(true)
            .displayOrder(3)
            .build();

    Banner firstBanner =
        Banner.builder()
            .label("first")
            .imagePath("banner/first.png")
            .active(false)
            .displayOrder(1)
            .build();

    Banner secondBanner =
        Banner.builder()
            .label("second")
            .imagePath("banner/second.png")
            .active(true)
            .displayOrder(2)
            .build();

    bannerRepository.saveAll(List.of(secondBanner, thirdBanner, firstBanner));

    // When
    List<Banner> results = bannerRepository.findAllByOrderByDisplayOrderAsc();

    // Then
    assertThat(results)
        .extracting(Banner::getDisplayOrder)
        .containsExactly(1, 2, 3);
  }
}
