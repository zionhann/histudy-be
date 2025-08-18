package edu.handong.csee.histudy.repository.impl;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import edu.handong.csee.histudy.repository.jpa.JpaAcademicTermRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AcademicTermRepositoryImpl implements AcademicTermRepository {
  private final JpaAcademicTermRepository repository;

  @Override
  public Optional<AcademicTerm> findCurrentSemester() {
    return repository.findCurrentSemester();
  }

  @Override
  public Optional<AcademicTerm> findByYearAndTerm(int year, TermType sem) {
    return repository.findByYearAndTerm(year, sem);
  }

  @Override
  public AcademicTerm save(AcademicTerm academicTerm) {
    return repository.save(academicTerm);
  }

  @Override
  public List<AcademicTerm> findAllByYearDesc() {
    return repository.findAllByYearDesc();
  }

  @Override
  public Optional<AcademicTerm> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  @Transactional
  public void setAllCurrentToFalse() {
    repository.setAllCurrentToFalse();
  }
}
