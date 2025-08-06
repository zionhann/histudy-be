package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository {
  Optional<StudyGroup> findByTagAndAcademicTerm(int tag, AcademicTerm academicTerm);

  void deleteEmptyGroup(AcademicTerm academicTerm);

  Optional<Integer> countMaxTag(AcademicTerm academicTerm);

  List<StudyGroup> findAllByAcademicTerm(AcademicTerm academicTerm);

  Optional<StudyGroup> findByUserAndTerm(User user, AcademicTerm currentTerm);

  boolean existsById(Long id);

  void deleteById(Long id);

  Optional<StudyGroup> findById(Long id);

  StudyGroup save(StudyGroup entity);

  long count();

  long countByAcademicTerm(AcademicTerm academicTerm);
}
