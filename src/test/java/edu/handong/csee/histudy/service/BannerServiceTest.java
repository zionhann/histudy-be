package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.handong.csee.histudy.controller.form.BannerForm;
import edu.handong.csee.histudy.controller.form.BannerReorderForm;
import edu.handong.csee.histudy.domain.Banner;
import edu.handong.csee.histudy.dto.BannerDto;
import edu.handong.csee.histudy.exception.MissingParameterException;
import edu.handong.csee.histudy.repository.BannerRepository;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

  @Mock private BannerRepository bannerRepository;

  private BannerService bannerService;

  @TempDir private File tempDir;

  @BeforeEach
  void init() {
    ImagePathMapper imagePathMapper = new ImagePathMapper();
    ReflectionTestUtils.setField(imagePathMapper, "origin", "http://localhost:8080");
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", "/images/");

    bannerService = new BannerService(bannerRepository, imagePathMapper);
    ReflectionTestUtils.setField(bannerService, "imageBaseLocation", tempDir + File.separator);
  }

  @Test
  void 배너생성시_이미지저장하고_응답반환() {
    // Given
    MockMultipartFile image =
        new MockMultipartFile("image", "banner.png", "image/png", createValidPngBytes());
    BannerForm form = new BannerForm("Main Banner", "https://example.com", true, image);

    when(bannerRepository.findTopByOrderByDisplayOrderDesc()).thenReturn(Optional.empty());
    when(bannerRepository.save(any(Banner.class)))
        .thenAnswer(
            invocation -> {
              Banner banner = invocation.getArgument(0);
              ReflectionTestUtils.setField(banner, "bannerId", 1L);
              return banner;
            });

    // When
    BannerDto.AdminBannerInfo result = bannerService.createBanner(form);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getDisplayOrder()).isEqualTo(1);
    assertThat(result.getImageUrl()).contains("/images/banner/main-banner_");
    assertThat(Path.of(tempDir.getAbsolutePath(), "banner")).exists();
  }

  @Test
  void 이미지타입이아니면_배너생성실패() {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("image", "banner.txt", "text/plain", "plain-text".getBytes());
    BannerForm form = new BannerForm("Main Banner", null, true, file);

    // When & Then
    assertThatThrownBy(() -> bannerService.createBanner(form))
        .isInstanceOf(MissingParameterException.class)
        .hasMessageContaining("이미지 파일만 업로드할 수 있습니다.");
  }

  @Test
  void 파일명에_경로문자포함시_확장자는_안전하게정규화된다() {
    MockMultipartFile image =
        new MockMultipartFile(
            "image", "banner.png/../../outside", "image/png", createValidPngBytes());
    BannerForm form = new BannerForm("Main Banner", "https://example.com", true, image);

    when(bannerRepository.findTopByOrderByDisplayOrderDesc()).thenReturn(Optional.empty());
    when(bannerRepository.save(any(Banner.class)))
        .thenAnswer(
            invocation -> {
              Banner banner = invocation.getArgument(0);
              ReflectionTestUtils.setField(banner, "bannerId", 1L);
              return banner;
            });

    bannerService.createBanner(form);

    ArgumentCaptor<Banner> captor = ArgumentCaptor.forClass(Banner.class);
    verify(bannerRepository).save(captor.capture());
    assertThat(captor.getValue().getImagePath()).startsWith("banner/").endsWith(".img");
    assertThat(captor.getValue().getImagePath()).doesNotContain("..");
  }

  @Test
  void 이미지크기가5MB초과면_배너생성실패() {
    // Given
    MultipartFile file = mock(MultipartFile.class);
    when(file.getSize()).thenReturn(5L * 1024 * 1024 + 1);

    BannerForm form = new BannerForm("Main Banner", null, true, file);

    // When & Then
    assertThatThrownBy(() -> bannerService.createBanner(form))
        .isInstanceOf(MissingParameterException.class)
        .hasMessageContaining("5MB");
  }

  @Test
  void 배너순서일괄변경시_요청순서대로반영() {
    // Given
    Banner first = createBanner(1L, 1, true, "banner/first.png");
    Banner second = createBanner(2L, 2, false, "banner/second.png");

    when(bannerRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(first, second));

    // When
    bannerService.reorderBanners(new BannerReorderForm(List.of(2L, 1L)));

    // Then
    assertThat(first.getDisplayOrder()).isEqualTo(2);
    assertThat(second.getDisplayOrder()).isEqualTo(1);
    verify(bannerRepository).saveAll(anyList());
  }

  @Test
  void 배너순서변경시_중복ID포함하면_실패() {
    // Given
    Banner first = createBanner(1L, 1, true, "banner/first.png");
    Banner second = createBanner(2L, 2, true, "banner/second.png");

    when(bannerRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(first, second));

    // When & Then
    assertThatThrownBy(() -> bannerService.reorderBanners(new BannerReorderForm(List.of(1L, 1L))))
        .isInstanceOf(MissingParameterException.class)
        .hasMessageContaining("중복된 배너 ID");
  }

  @Test
  void 배너삭제시_이미지파일도삭제() throws Exception {
    // Given
    Path imagePath = Path.of(tempDir.getAbsolutePath(), "banner", "to-delete.png");
    Files.createDirectories(imagePath.getParent());
    Files.writeString(imagePath, "image-content");

    Banner banner = createBanner(10L, 1, true, "banner/to-delete.png");

    when(bannerRepository.findById(10L)).thenReturn(Optional.of(banner));
    when(bannerRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of());

    // When
    bannerService.deleteBanner(10L);

    // Then
    assertThat(Files.exists(imagePath)).isFalse();
    verify(bannerRepository).delete(banner);
  }

  @Test
  void 공개배너조회시_활성배너만반환() {
    // Given
    Banner first = createBanner(1L, 1, true, "banner/first.png");
    Banner second = createBanner(2L, 2, true, "banner/second.png");

    when(bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(first, second));

    // When
    List<BannerDto.PublicBannerInfo> result = bannerService.getPublicBanners();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(0).getImageUrl()).contains("/images/banner/");
  }

  private Banner createBanner(Long id, int order, boolean active, String imagePath) {
    Banner banner =
        Banner.builder()
            .label("label-" + id)
            .imagePath(imagePath)
            .redirectUrl("https://example.com/" + id)
            .active(active)
            .displayOrder(order)
            .build();

    ReflectionTestUtils.setField(banner, "bannerId", id);
    return banner;
  }

  private byte[] createValidPngBytes() {
    try {
      BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "png", outputStream);
      return outputStream.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
