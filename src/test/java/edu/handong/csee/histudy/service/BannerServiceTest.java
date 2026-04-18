package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.banner.adapter.in.request.CreateBannerRequest;
import edu.handong.csee.histudy.banner.adapter.in.request.ReorderBannersRequest;
import edu.handong.csee.histudy.banner.adapter.in.request.UpdateBannerRequest;
import edu.handong.csee.histudy.banner.adapter.in.response.AdminBannerResponse;
import edu.handong.csee.histudy.banner.adapter.in.response.PublicBannerResponse;
import edu.handong.csee.histudy.banner.adapter.out.storage.BannerImageStorage;
import edu.handong.csee.histudy.banner.application.BannerDisplayOrderManager;
import edu.handong.csee.histudy.banner.application.BannerService;
import edu.handong.csee.histudy.banner.application.command.CreateBannerCommand;
import edu.handong.csee.histudy.banner.application.command.DeleteBannerCommand;
import edu.handong.csee.histudy.banner.application.command.ReorderBannersCommand;
import edu.handong.csee.histudy.banner.application.command.UpdateBannerCommand;
import edu.handong.csee.histudy.banner.domain.Banner;
import edu.handong.csee.histudy.exception.BannerNotFoundException;
import edu.handong.csee.histudy.exception.MissingParameterException;
import edu.handong.csee.histudy.service.repository.fake.FakeBannerRepository;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class BannerServiceTest {

  @TempDir Path tempDir;

  private final byte[] bannerPngBytes = createBannerPngBytes();
  private final Banner firstBanner =
      Banner.builder()
          .label("First")
          .imagePath("banner/first.png")
          .redirectUrl("https://example.com/first")
          .active(true)
          .displayOrder(1)
          .build();
  private final Banner secondBanner =
      Banner.builder()
          .label("Second")
          .imagePath("banner/second.png")
          .redirectUrl("https://example.com/second")
          .active(true)
          .displayOrder(2)
          .build();
  private final Banner visibleBanner =
      Banner.builder()
          .label("Visible")
          .imagePath("banner/visible.png")
          .redirectUrl("https://example.com/visible")
          .active(true)
          .displayOrder(1)
          .build();
  private final Banner hiddenBanner =
      Banner.builder()
          .label("Hidden")
          .imagePath("banner/hidden.png")
          .redirectUrl("https://example.com/hidden")
          .active(false)
          .displayOrder(2)
          .build();
  private final Banner existingBanner =
      Banner.builder()
          .label("Existing")
          .imagePath("banner/existing.png")
          .redirectUrl("https://example.com/existing")
          .active(true)
          .displayOrder(1)
          .build();

  private FakeBannerRepository bannerRepository;
  private BannerService bannerService;

  @BeforeEach
  void setUp() {
    bannerRepository = new FakeBannerRepository();
    ImagePathMapper imagePathMapper = new ImagePathMapper();
    ApplicationEventPublisher eventPublisher = event -> {};
    BannerImageStorage bannerImageStorage = new BannerImageStorage(eventPublisher);
    BannerDisplayOrderManager bannerDisplayOrderManager =
        new BannerDisplayOrderManager(bannerRepository);
    ReflectionTestUtils.setField(imagePathMapper, "origin", "https://histudy.handong.edu");
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", "/images");
    ReflectionTestUtils.setField(bannerImageStorage, "imageBaseLocation", tempDir.toString() + "/");
    bannerService =
        new BannerService(
            bannerRepository, imagePathMapper, bannerImageStorage, bannerDisplayOrderManager);
  }

  @Test
  void 등록된_전체_배너_목록을_조회하면_노출순으로_반환한다() {
    // Given
    bannerRepository.save(secondBanner);
    bannerRepository.save(firstBanner);

    // When
    List<AdminBannerResponse> result = bannerService.getAdminBanners();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(AdminBannerResponse::getLabel).containsExactly("First", "Second");
  }

  @Test
  void 공개된_배너_목록을_조회하면_활성_배너만_반환한다() {
    // Given
    bannerRepository.save(visibleBanner);
    bannerRepository.save(hiddenBanner);

    // When
    List<PublicBannerResponse> result = bannerService.getPublicBanners();

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRedirectUrl()).isEqualTo("https://example.com/visible");
  }

  @Test
  void 새로운_배너를_등록하면_다음_노출순서로_저장된다() throws Exception {
    // Given
    bannerRepository.save(existingBanner);
    CreateBannerRequest form =
        new CreateBannerRequest(
            "  Spring Banner  ",
            "https://example.com/banner",
            true,
            new MockMultipartFile("image", "banner.png", "image/png", bannerPngBytes));

    // When
    AdminBannerResponse result = bannerService.createBanner(CreateBannerCommand.from(form));

    // Then
    assertThat(result.getLabel()).isEqualTo("Spring Banner");
    assertThat(result.getDisplayOrder()).isEqualTo(2);
    assertThat(bannerRepository.findAll()).hasSize(2);
    Path storedImage =
        tempDir.resolve(result.getImageUrl().replace("https://histudy.handong.edu/images/", ""));
    assertThat(Files.exists(storedImage)).isTrue();
  }

  @Test
  void 이미지_없이_배너를_등록하면_예외가_발생한다() {
    // Given
    CreateBannerRequest form =
        new CreateBannerRequest("Banner", "https://example.com/banner", true, null);

    // When Then
    assertThatThrownBy(() -> bannerService.createBanner(CreateBannerCommand.from(form)))
        .isInstanceOf(MissingParameterException.class);
  }

  @Test
  void 배너_정보를_업데이트하면_텍스트정보를_변경한다() throws Exception {
    // Given
    AdminBannerResponse created =
        bannerService.createBanner(
            CreateBannerCommand.from(
                new CreateBannerRequest(
                    "Original",
                    "https://example.com/original",
                    true,
                    new MockMultipartFile("image", "banner.png", "image/png", bannerPngBytes))));
    String originalImageUrl = created.getImageUrl();

    // When
    AdminBannerResponse updated =
        bannerService.updateBanner(
            UpdateBannerCommand.from(
                created.getId(),
                new UpdateBannerRequest("Updated", "https://example.com/updated", false, null)));

    // Then
    assertThat(updated.getLabel()).isEqualTo("Updated");
    assertThat(updated.isActive()).isFalse();
    assertThat(updated.getRedirectUrl()).isEqualTo("https://example.com/updated");
    assertThat(updated.getImageUrl()).isEqualTo(originalImageUrl);
    Banner persisted =
        bannerRepository
            .findById(created.getId())
            .orElseThrow(() -> new AssertionError("updated banner should remain in repository"));
    assertThat(persisted.getLabel()).isEqualTo("Updated");
    assertThat(persisted.isActive()).isFalse();
    assertThat(persisted.getRedirectUrl()).isEqualTo("https://example.com/updated");
    assertThat(persisted.getImagePath())
        .isEqualTo(originalImageUrl.replace("https://histudy.handong.edu/images/", ""));
  }

  @Test
  void 배너_이미지를_업데이트하면_이미지경로를_교체한다() throws Exception {
    // Given
    AdminBannerResponse created =
        bannerService.createBanner(
            CreateBannerCommand.from(
                new CreateBannerRequest(
                    "Original",
                    "https://example.com/original",
                    true,
                    new MockMultipartFile("image", "banner.png", "image/png", bannerPngBytes))));
    String originalImageUrl = created.getImageUrl();

    // When
    AdminBannerResponse updated =
        bannerService.updateBanner(
            UpdateBannerCommand.from(
                created.getId(),
                new UpdateBannerRequest(
                    null,
                    null,
                    null,
                    new MockMultipartFile(
                        "image", "banner-next.png", "image/png", bannerPngBytes))));

    // Then
    assertThat(updated.getImageUrl()).isNotEqualTo(originalImageUrl);
    Banner persisted =
        bannerRepository
            .findById(created.getId())
            .orElseThrow(() -> new AssertionError("updated banner should remain in repository"));
    assertThat(persisted.getImagePath())
        .isEqualTo(updated.getImageUrl().replace("https://histudy.handong.edu/images/", ""));
    assertThat(Files.exists(tempDir.resolve(persisted.getImagePath()))).isTrue();
  }

  @Test
  void 배너_순서를_재배치하면_노출순서가_재정렬된다() {
    // Given
    Banner first = bannerRepository.save(firstBanner);
    Banner second = bannerRepository.save(secondBanner);

    // When
    bannerService.reorderBanners(
        ReorderBannersCommand.from(
            new ReorderBannersRequest(List.of(second.getBannerId(), first.getBannerId()))));

    // Then
    assertThat(bannerRepository.findAllByOrderByDisplayOrderAsc())
        .extracting(Banner::getBannerId)
        .containsExactly(second.getBannerId(), first.getBannerId());
    assertThat(bannerRepository.findAllByOrderByDisplayOrderAsc())
        .extracting(Banner::getDisplayOrder)
        .containsExactly(1, 2);
  }

  @Test
  void 유효하지_않은_ID로_배너_순서를_재배치하면_예외가_발생한다() {
    // Given
    Banner first = bannerRepository.save(firstBanner);

    // When Then
    assertThatThrownBy(
            () ->
                bannerService.reorderBanners(
                    ReorderBannersCommand.from(
                        new ReorderBannersRequest(List.of(first.getBannerId(), 999L)))))
        .isInstanceOf(MissingParameterException.class);
  }

  @Test
  void 배너를_삭제하면_남은_배너의_노출순서가_정규화된다() {
    // Given
    Banner first = bannerRepository.save(firstBanner);
    Banner second = bannerRepository.save(secondBanner);

    // When
    bannerService.deleteBanner(new DeleteBannerCommand(first.getBannerId()));

    // Then
    assertThat(bannerRepository.findAll()).hasSize(1);
    assertThat(bannerRepository.findAll().get(0).getBannerId()).isEqualTo(second.getBannerId());
    assertThat(bannerRepository.findAll().get(0).getDisplayOrder()).isEqualTo(1);
  }

  @Test
  void 존재하지_않는_배너를_삭제하면_예외가_발생한다() {
    // Given

    // When Then
    assertThatThrownBy(() -> bannerService.deleteBanner(new DeleteBannerCommand(999L)))
        .isInstanceOf(BannerNotFoundException.class);
  }

  @Test
  void 존재하지_않는_배너를_업데이트하면_예외가_발생한다() {
    // Given
    UpdateBannerCommand command =
        UpdateBannerCommand.from(
            999L, new UpdateBannerRequest("Updated", "https://example.com/updated", true, null));

    // When Then
    assertThatThrownBy(() -> bannerService.updateBanner(command))
        .isInstanceOf(BannerNotFoundException.class);
  }

  private byte[] createBannerPngBytes() {
    try {
      BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "png", outputStream);
      return outputStream.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("failed to create banner image fixture", e);
    }
  }
}
