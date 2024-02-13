package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Season;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<AcademicTerm, Long> {
  @Query("SELECT at FROM AcademicTerm at WHERE at.year = :year AND at.semester = :sem")
  Optional<AcademicTerm> findByYearAndTerm(@Param("year") int year, @Param("sem") Season sem);
}
