package edu.handong.csee.histudy.banner.application;

import edu.handong.csee.histudy.banner.application.command.ReorderBannersCommand;
import edu.handong.csee.histudy.banner.application.port.BannerRepository;
import edu.handong.csee.histudy.banner.domain.Banner;
import edu.handong.csee.histudy.exception.MissingParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BannerDisplayOrderManager {

  private static final String MESSAGE_REORDER_REQUIRES_ALL = "모든 배너 ID를 포함한 순서 목록이 필요합니다.";
  private static final String MESSAGE_REORDER_DUPLICATED = "중복된 배너 ID가 포함되어 있습니다.";
  private static final String MESSAGE_REORDER_INVALID_IDS = "유효하지 않은 배너 ID 목록입니다.";
  private static final String MESSAGE_REORDER_EMPTY_BANNERS = "현재 배너가 존재하지 않습니다.";

  private final BannerRepository bannerRepository;

  @Transactional
  public int getNextDisplayOrder() {
    return bannerRepository
            .findTopByOrderByDisplayOrderDescForUpdate()
            .map(Banner::getDisplayOrder)
            .orElse(0)
        + 1;
  }

  @Transactional
  public void reorder(ReorderBannersCommand command) {
    List<Long> orderedIds = command.orderedIds();
    List<Banner> banners = bannerRepository.findAllByOrderByDisplayOrderAscForUpdate();

    if (banners.isEmpty()) {
      if (orderedIds.isEmpty()) {
        return;
      }
      throw new MissingParameterException(MESSAGE_REORDER_EMPTY_BANNERS);
    }

    validateReorderPayload(orderedIds, banners);

    Map<Long, Banner> bannersById =
        banners.stream().collect(Collectors.toMap(Banner::getBannerId, banner -> banner));

    for (int i = 0; i < orderedIds.size(); i++) {
      findBannerById(bannersById, orderedIds.get(i)).changeDisplayOrder(i + 1);
    }

    bannerRepository.saveAll(banners);
  }

  @Transactional
  public void normalize() {
    List<Banner> banners = bannerRepository.findAllByOrderByDisplayOrderAscForUpdate();
    if (banners.isEmpty()) {
      return;
    }

    for (int i = 0; i < banners.size(); i++) {
      banners.get(i).changeDisplayOrder(i + 1);
    }
    bannerRepository.saveAll(banners);
  }

  private void validateReorderPayload(List<Long> orderedIds, List<Banner> banners) {
    if (orderedIds.size() != banners.size()) {
      throw new MissingParameterException(MESSAGE_REORDER_REQUIRES_ALL);
    }

    if (orderedIds.stream().anyMatch(Objects::isNull)) {
      throw new MissingParameterException(MESSAGE_REORDER_INVALID_IDS);
    }

    Set<Long> deduplicated = new HashSet<>(orderedIds);
    if (deduplicated.size() != orderedIds.size()) {
      throw new MissingParameterException(MESSAGE_REORDER_DUPLICATED);
    }

    Set<Long> existingIds = banners.stream().map(Banner::getBannerId).collect(Collectors.toSet());
    if (!existingIds.equals(deduplicated)) {
      throw new MissingParameterException(MESSAGE_REORDER_INVALID_IDS);
    }
  }

  private Banner findBannerById(Map<Long, Banner> bannersById, Long bannerId) {
    Banner banner = bannersById.get(bannerId);
    if (banner == null) {
      throw new MissingParameterException(MESSAGE_REORDER_INVALID_IDS);
    }
    return banner;
  }
}
