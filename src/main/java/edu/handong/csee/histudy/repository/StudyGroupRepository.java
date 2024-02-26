package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
  Optional<StudyGroup> findByTag(int tag);

  @Modifying
  @Query("delete from StudyGroup s where s.tag = -1")
  void deleteEmptyGroup();

  @Query("select max(s.tag) from StudyGroup s where s.academicTerm = :academicTerm")
  Optional<Integer> countMaxTag(@Param("academicTerm") AcademicTerm academicTerm);

  @Query(
      "select s from StudyGroup s where s.academicTerm = :academicTerm order by s.totalMinutes desc")
  List<StudyGroup> findAllByAcademicTermOrderByDesc(@Param("academicTerm") AcademicTerm academicTerm);
}
