package edu.handong.csee.histudy.repository.impl;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.jpa.JpaStudyGroupRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyGroupRepositoryImpl implements StudyGroupRepository {
  private final JpaStudyGroupRepository repository;

  @Override
  public Optional<StudyGroup> findByTagAndAcademicTerm(int tag, AcademicTerm academicTerm) {
    return repository.findByTagAndAcademicTerm(tag, academicTerm);
  }

  @Override
  public void deleteEmptyGroup(AcademicTerm academicTerm) {
    repository.deleteEmptyGroup(academicTerm);
  }

  @Override
  public Optional<Integer> countMaxTag(AcademicTerm academicTerm) {
    return repository.countMaxTag(academicTerm);
  }

  @Override
  public List<StudyGroup> findAllByAcademicTerm(AcademicTerm academicTerm) {
    return repository.findAllByAcademicTerm(academicTerm);
  }

  @Override
  public Optional<StudyGroup> findByUserAndTerm(User user, AcademicTerm currentTerm) {
    return repository.findByUserAndTerm(user, currentTerm);
  }

  @Override
  public boolean existsById(Long id) {
    return repository.existsById(id);
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  @Override
  public Optional<StudyGroup> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public StudyGroup save(StudyGroup studyGroup) {
    return repository.save(studyGroup);
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public long countByAcademicTerm(AcademicTerm academicTerm) {
    return repository.countByAcademicTerm(academicTerm);
  }
}
