package edu.handong.csee.histudy.repository.impl;

import edu.handong.csee.histudy.domain.Banner;
import edu.handong.csee.histudy.repository.BannerRepository;
import edu.handong.csee.histudy.repository.jpa.JpaBannerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

  private final JpaBannerRepository repository;

  @Override
  public List<Banner> findAllByOrderByDisplayOrderAsc() {
    return repository.findAllByOrderByDisplayOrderAsc();
  }

  @Override
  public List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc() {
    return repository.findAllByActiveTrueOrderByDisplayOrderAsc();
  }

  @Override
  public List<Banner> saveAll(List<Banner> banners) {
    return repository.saveAll(banners);
  }

  @Override
  public Banner save(Banner banner) {
    return repository.save(banner);
  }

  @Override
  public Optional<Banner> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public Optional<Banner> findTopByOrderByDisplayOrderDesc() {
    return repository.findTopByOrderByDisplayOrderDesc();
  }

  @Override
  public void delete(Banner banner) {
    repository.delete(banner);
  }
}
