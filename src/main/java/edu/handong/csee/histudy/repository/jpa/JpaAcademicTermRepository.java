package edu.handong.csee.histudy.repository.jpa;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaAcademicTermRepository extends JpaRepository<AcademicTerm, Long> {

  @Query("select a from AcademicTerm a where a.isCurrent = true")
  Optional<AcademicTerm> findCurrentSemester();

  @Query("SELECT at FROM AcademicTerm at WHERE at.academicYear = :year AND at.semester = :sem")
  Optional<AcademicTerm> findByYearAndTerm(@Param("year") int year, @Param("sem") TermType sem);

  @Query("SELECT at FROM AcademicTerm at ORDER BY at.academicYear DESC, at.semester DESC")
  List<AcademicTerm> findAllByYearAndSemesterDesc();
}
