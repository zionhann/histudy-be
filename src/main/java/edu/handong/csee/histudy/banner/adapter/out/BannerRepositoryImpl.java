package edu.handong.csee.histudy.banner.adapter.out;

import edu.handong.csee.histudy.banner.application.port.BannerRepository;
import edu.handong.csee.histudy.banner.domain.Banner;
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
  public List<Banner> findAllByOrderByDisplayOrderAscForUpdate() {
    return repository.findAllForUpdateOrderByDisplayOrderAsc();
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
  public Optional<Banner> findTopByOrderByDisplayOrderDescForUpdate() {
    return repository.findAllForUpdateOrderByDisplayOrderDesc().stream().findFirst();
  }

  @Override
  public void delete(Banner banner) {
    repository.delete(banner);
  }
}
