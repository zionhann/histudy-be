package edu.handong.csee.histudy.banner.application;

import edu.handong.csee.histudy.banner.adapter.in.response.AdminBannerResponse;
import edu.handong.csee.histudy.banner.adapter.in.response.PublicBannerResponse;
import edu.handong.csee.histudy.banner.adapter.out.storage.BannerImageStorage;
import edu.handong.csee.histudy.banner.application.command.CreateBannerCommand;
import edu.handong.csee.histudy.banner.application.command.DeleteBannerCommand;
import edu.handong.csee.histudy.banner.application.command.ReorderBannersCommand;
import edu.handong.csee.histudy.banner.application.command.UpdateBannerCommand;
import edu.handong.csee.histudy.banner.application.port.BannerRepository;
import edu.handong.csee.histudy.banner.domain.Banner;
import edu.handong.csee.histudy.exception.BannerNotFoundException;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BannerService {

  private final BannerRepository bannerRepository;
  private final ImagePathMapper imagePathMapper;
  private final BannerImageStorage bannerImageStorage;
  private final BannerDisplayOrderManager bannerDisplayOrderManager;

  @Transactional(readOnly = true)
  public List<AdminBannerResponse> getAdminBanners() {
    return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(this::toAdminBannerResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PublicBannerResponse> getPublicBanners() {
    return bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
        .map(this::toPublicBannerResponse)
        .toList();
  }

  @Transactional
  public AdminBannerResponse createBanner(CreateBannerCommand command) {
    String imagePath = bannerImageStorage.store(command.image(), command.label());
    bannerImageStorage.deleteAfterRollback(imagePath);

    Banner banner =
        Banner.create(
            command.label(),
            imagePath,
            command.redirectUrl(),
            command.active(),
            bannerDisplayOrderManager.getNextDisplayOrder());

    return toAdminBannerResponse(bannerRepository.save(banner));
  }

  @Transactional
  public AdminBannerResponse updateBanner(UpdateBannerCommand command) {
    Banner banner = findBanner(command.bannerId());
    MultipartFile image = command.image();

    banner.update(
        command.resolveLabel(banner.getLabel()),
        command.resolveRedirectUrl(banner.getRedirectUrl()),
        command.resolveActive(banner.isActive()));

    if (command.hasImage()) {
      replaceBannerImage(banner, image);
    }

    return toAdminBannerResponse(banner);
  }

  @Transactional
  public void deleteBanner(DeleteBannerCommand command) {
    Banner banner = findBanner(command.bannerId());
    bannerRepository.delete(banner);
    bannerImageStorage.deleteAfterCommit(banner.getImagePath());
    bannerDisplayOrderManager.normalize();
  }

  @Transactional
  public void reorderBanners(ReorderBannersCommand command) {
    bannerDisplayOrderManager.reorder(command);
  }

  private void replaceBannerImage(Banner banner, MultipartFile image) {
    String oldImagePath = banner.getImagePath();
    String newImagePath = bannerImageStorage.store(image, banner.getLabel());
    bannerImageStorage.deleteAfterRollback(newImagePath);
    banner.replaceImage(newImagePath);
    bannerImageStorage.deleteAfterCommit(oldImagePath);
  }

  private Banner findBanner(Long bannerId) {
    return bannerRepository.findById(bannerId).orElseThrow(BannerNotFoundException::new);
  }

  private AdminBannerResponse toAdminBannerResponse(Banner banner) {
    return new AdminBannerResponse(banner, imagePathMapper.getFullPath(banner.getImagePath()));
  }

  private PublicBannerResponse toPublicBannerResponse(Banner banner) {
    return new PublicBannerResponse(banner, imagePathMapper.getFullPath(banner.getImagePath()));
  }
}
