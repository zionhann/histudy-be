package edu.handong.csee.histudy.banner.adapter.out;

import edu.handong.csee.histudy.banner.domain.Banner;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaBannerRepository extends JpaRepository<Banner, Long> {

  List<Banner> findAllByOrderByDisplayOrderAsc();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Banner b order by b.displayOrder asc, b.bannerId asc")
  List<Banner> findAllForUpdateOrderByDisplayOrderAsc();

  List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc();

  Optional<Banner> findTopByOrderByDisplayOrderDesc();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Banner b order by b.displayOrder desc, b.bannerId desc")
  List<Banner> findAllForUpdateOrderByDisplayOrderDesc();
}
