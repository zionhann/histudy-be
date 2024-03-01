package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
  Optional<StudyGroup> findByTagAndAcademicTerm(int tag, AcademicTerm academicTerm);

  @Modifying
  @Query("delete from StudyGroup s where s.tag = -1")
  void deleteEmptyGroup();

  @Query("select max(s.tag) from StudyGroup s where s.academicTerm = :academicTerm")
  Optional<Integer> countMaxTag(@Param("academicTerm") AcademicTerm academicTerm);

  @Query("select s from StudyGroup s where s.academicTerm = :academicTerm order by s.tag asc")
  List<StudyGroup> findAllByAcademicTerm(@Param("academicTerm") AcademicTerm academicTerm);

  @Query(
      "select s from StudyGroup s where :user member of s.members and s.academicTerm = :currentTerm")
  Optional<StudyGroup> findByUserAndTerm(
      @Param("user") User user, @Param("currentTerm") AcademicTerm currentTerm);
}
