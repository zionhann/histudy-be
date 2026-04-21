package edu.handong.csee.histudy.banner.application.port;

import edu.handong.csee.histudy.banner.domain.Banner;
import java.util.List;
import java.util.Optional;

public interface BannerRepository {

  List<Banner> findAllByOrderByDisplayOrderAsc();

  List<Banner> findAllByOrderByDisplayOrderAscForUpdate();

  List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc();

  List<Banner> saveAll(List<Banner> banners);

  Banner save(Banner banner);

  Optional<Banner> findById(Long id);

  Optional<Banner> findTopByOrderByDisplayOrderDesc();

  Optional<Banner> findTopByOrderByDisplayOrderDescForUpdate();

  void delete(Banner banner);
}
