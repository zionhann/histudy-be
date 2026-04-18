package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.Banner;
import edu.handong.csee.histudy.dto.BannerDto;
import edu.handong.csee.histudy.exception.BannerNotFoundException;
import edu.handong.csee.histudy.repository.BannerRepository;
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
  public List<BannerDto.AdminBannerInfo> getAdminBanners() {
    return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(this::toAdminBannerInfo)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<BannerDto.PublicBannerInfo> getPublicBanners() {
    return bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
        .map(this::toPublicBannerInfo)
        .toList();
  }

  @Transactional
  public BannerDto.AdminBannerInfo createBanner(CreateBannerCommand command) {
    String imagePath = bannerImageStorage.store(command.image(), command.label());
    bannerImageStorage.deleteAfterRollback(imagePath);

    Banner banner =
        Banner.create(
            command.label(),
            imagePath,
            command.redirectUrl(),
            command.active(),
            bannerDisplayOrderManager.getNextDisplayOrder());

    return toAdminBannerInfo(bannerRepository.save(banner));
  }

  @Transactional
  public BannerDto.AdminBannerInfo updateBanner(UpdateBannerCommand command) {
    Banner banner = findBanner(command.bannerId());
    MultipartFile image = command.image();

    banner.update(
        command.resolveLabel(banner.getLabel()),
        command.resolveRedirectUrl(banner.getRedirectUrl()),
        command.resolveActive(banner.isActive()));

    if (command.hasImage()) {
      replaceBannerImage(banner, image);
    }

    return toAdminBannerInfo(banner);
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

  private BannerDto.AdminBannerInfo toAdminBannerInfo(Banner banner) {
    return new BannerDto.AdminBannerInfo(
        banner, imagePathMapper.getFullPath(banner.getImagePath()));
  }

  private BannerDto.PublicBannerInfo toPublicBannerInfo(Banner banner) {
    return new BannerDto.PublicBannerInfo(
        banner, imagePathMapper.getFullPath(banner.getImagePath()));
  }
}
