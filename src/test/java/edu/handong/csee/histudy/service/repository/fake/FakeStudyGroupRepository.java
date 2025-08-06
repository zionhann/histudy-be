package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeStudyGroupRepository implements StudyGroupRepository {

  private final List<StudyGroup> store = new ArrayList<>();
  private Long sequence = 1L;

  @Override
  public Optional<StudyGroup> findByTagAndAcademicTerm(int tag, AcademicTerm academicTerm) {
    return store.stream()
        .filter(e -> e.getTag().equals(tag) && e.getAcademicTerm().equals(academicTerm))
        .findFirst();
  }

  @Override
  public void deleteEmptyGroup(AcademicTerm academicTerm) {
    store.stream()
        .filter(e -> e.getAcademicTerm().equals(academicTerm) && e.getMembers().isEmpty())
        .toList()
        .forEach(store::remove);
  }

  @Override
  public Optional<Integer> countMaxTag(AcademicTerm academicTerm) {
    return store.stream()
        .filter(e -> e.getAcademicTerm().equals(academicTerm))
        .map(StudyGroup::getTag)
        .max(Integer::compareTo);
  }

  @Override
  public List<StudyGroup> findAllByAcademicTerm(AcademicTerm academicTerm) {
    return store.stream().filter(e -> e.getAcademicTerm().equals(academicTerm)).toList();
  }

  @Override
  public Optional<StudyGroup> findByUserAndTerm(User user, AcademicTerm currentTerm) {
    return store.stream()
        .filter(e -> e.getAcademicTerm().equals(currentTerm))
        .filter(e -> e.getMembers().stream().anyMatch(m -> m.getUser().equals(user)))
        .findFirst();
  }

  @Override
  public boolean existsById(Long id) {
    return store.stream().anyMatch(e -> e.getStudyGroupId().equals(id));
  }

  @Override
  public void deleteById(Long id) {
    store.removeIf(e -> e.getStudyGroupId().equals(id));
  }

  @Override
  public Optional<StudyGroup> findById(Long id) {
    return store.stream().filter(e -> e.getStudyGroupId().equals(id)).findFirst();
  }

  @Override
  public StudyGroup save(StudyGroup entity) {
    ReflectionTestUtils.setField(entity, "studyGroupId", sequence++);
    store.add(entity);
    return entity;
  }

  @Override
  public long count() {
    return store.size();
  }

  @Override
  public long countByAcademicTerm(AcademicTerm academicTerm) {
    return store.stream().filter(e -> e.getAcademicTerm().equals(academicTerm)).count();
  }
}
