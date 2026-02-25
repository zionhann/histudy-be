package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Banner;
import java.util.List;
import java.util.Optional;

public interface BannerRepository {

  List<Banner> findAllByOrderByDisplayOrderAsc();

  List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc();

  List<Banner> saveAll(List<Banner> banners);

  Banner save(Banner banner);

  Optional<Banner> findById(Long id);

  Optional<Banner> findTopByOrderByDisplayOrderDesc();

  void delete(Banner banner);
}
