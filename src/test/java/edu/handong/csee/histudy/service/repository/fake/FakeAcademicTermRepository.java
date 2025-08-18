package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeAcademicTermRepository implements AcademicTermRepository {

  private final List<AcademicTerm> store = new ArrayList<>();
  private Long sequence = 1L;

  @Override
  public Optional<AcademicTerm> findCurrentSemester() {
    return store.stream().filter(AcademicTerm::getIsCurrent).findFirst();
  }

  @Override
  public Optional<AcademicTerm> findByYearAndTerm(int year, TermType sem) {
    return store.stream()
        .filter(cal -> cal.getAcademicYear().equals(year) && cal.getSemester().equals(sem))
        .findFirst();
  }

  @Override
  public AcademicTerm save(AcademicTerm entity) {
    if (entity.getAcademicTermId() == null) {
      ReflectionTestUtils.setField(entity, "academicTermId", sequence++);
    }

    // Remove existing entity if updating
    store.removeIf(existing -> existing.getAcademicTermId().equals(entity.getAcademicTermId()));
    store.add(entity);
    return entity;
  }

  @Override
  public List<AcademicTerm> findAllByYearDescAndSemesterDesc() {
    List<AcademicTerm> result = new ArrayList<>(store);
    result.sort(
        Comparator.comparing(AcademicTerm::getAcademicYear)
            .reversed()
            .thenComparing(AcademicTerm::getSemester, Comparator.reverseOrder()));
    return result;
  }

  public List<AcademicTerm> findAll() {
    return new ArrayList<>(store);
  }

  @Override
  public Optional<AcademicTerm> findById(Long id) {
    return store.stream().filter(term -> term.getAcademicTermId().equals(id)).findFirst();
  }
}
