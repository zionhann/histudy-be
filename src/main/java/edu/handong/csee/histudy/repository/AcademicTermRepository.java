package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.AcademicTerm;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long> {
  @Query("select a from AcademicTerm a where a.isCurrent = true")
  Optional<AcademicTerm> findCurrentSemester();
}
