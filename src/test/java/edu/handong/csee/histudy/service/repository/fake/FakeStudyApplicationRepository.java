package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.StudyApplicantRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeStudyApplicationRepository implements StudyApplicantRepository {

  private final List<StudyApplicant> store = new ArrayList<>();

  @Override
  public Optional<StudyApplicant> findByUserAndTerm(User applicant, AcademicTerm currentTerm) {
    return store.stream()
        .filter(e -> e.getUser().equals(applicant) && e.getAcademicTerm().equals(currentTerm))
        .findFirst();
  }

  @Override
  public List<StudyApplicant> findUnassignedApplicants(AcademicTerm currentTerm) {
    return store.stream()
        .filter(e -> e.getAcademicTerm().equals(currentTerm) && e.isNotMarkedAsGrouped())
        .toList();
  }

  @Override
  public List<StudyApplicant> findAssignedApplicants(AcademicTerm currentTerm) {
    return store.stream()
        .filter(e -> e.getAcademicTerm().equals(currentTerm) && e.isMarkedAsGrouped())
        .toList();
  }

  @Override
  public List<StudyApplicant> findAllByTerm(AcademicTerm currentTerm) {
    return store.stream().filter(e -> e.getAcademicTerm().equals(currentTerm)).toList();
  }

  @Override
  public List<StudyApplicant> findAllByStudyGroup(StudyGroup group) {
    return store.stream().filter(e -> e.getStudyGroup().equals(group)).toList();
  }

  @Override
  public StudyApplicant save(StudyApplicant entity) {
    store.add(entity);
    return entity;
  }

  @Override
  public void delete(StudyApplicant applicant) {
    store.remove(applicant);
  }
}
