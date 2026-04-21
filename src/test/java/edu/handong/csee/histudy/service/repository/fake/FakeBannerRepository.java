package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.banner.application.port.BannerRepository;
import edu.handong.csee.histudy.banner.domain.Banner;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeBannerRepository implements BannerRepository {

  private final List<Banner> store = new ArrayList<>();
  private long sequence = 1L;

  @Override
  public List<Banner> findAllByOrderByDisplayOrderAsc() {
    return store.stream().sorted(Comparator.comparingInt(Banner::getDisplayOrder)).toList();
  }

  @Override
  public List<Banner> findAllByOrderByDisplayOrderAscForUpdate() {
    return findAllByOrderByDisplayOrderAsc();
  }

  @Override
  public List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc() {
    return store.stream()
        .filter(Banner::isActive)
        .sorted(Comparator.comparingInt(Banner::getDisplayOrder))
        .toList();
  }

  @Override
  public List<Banner> saveAll(List<Banner> banners) {
    return banners.stream().map(this::save).toList();
  }

  @Override
  public Banner save(Banner banner) {
    if (banner.getBannerId() == null) {
      ReflectionTestUtils.setField(banner, "bannerId", sequence++);
      ReflectionTestUtils.setField(banner, "createdDate", LocalDateTime.now());
    }
    ReflectionTestUtils.setField(banner, "lastModifiedDate", LocalDateTime.now());
    store.removeIf(existing -> existing.getBannerId().equals(banner.getBannerId()));
    store.add(banner);
    return banner;
  }

  @Override
  public Optional<Banner> findById(Long id) {
    return store.stream().filter(banner -> banner.getBannerId().equals(id)).findFirst();
  }

  @Override
  public Optional<Banner> findTopByOrderByDisplayOrderDesc() {
    return store.stream().max(Comparator.comparingInt(Banner::getDisplayOrder));
  }

  @Override
  public Optional<Banner> findTopByOrderByDisplayOrderDescForUpdate() {
    return findTopByOrderByDisplayOrderDesc();
  }

  @Override
  public void delete(Banner banner) {
    store.removeIf(existing -> existing.getBannerId().equals(banner.getBannerId()));
  }

  public List<Banner> findAll() {
    return new ArrayList<>(store);
  }
}
