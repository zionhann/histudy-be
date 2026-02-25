package edu.handong.csee.histudy.repository.jpa;

import edu.handong.csee.histudy.domain.Banner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBannerRepository extends JpaRepository<Banner, Long> {

  List<Banner> findAllByOrderByDisplayOrderAsc();

  List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc();

  Optional<Banner> findTopByOrderByDisplayOrderDesc();
}
