package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import java.util.ArrayList;
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
    ReflectionTestUtils.setField(entity, "academicTermId", sequence++);
    store.add(entity);
    return entity;
  }
}
