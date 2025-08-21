package edu.handong.csee.histudy.service;

import static edu.handong.csee.histudy.dto.AcademicTermDto.*;

import edu.handong.csee.histudy.controller.form.AcademicTermForm;
import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.dto.AcademicTermDto;
import edu.handong.csee.histudy.exception.DuplicateAcademicTermException;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademicTermService {

  private final AcademicTermRepository academicTermRepository;

  @Transactional
  public void createAcademicTerm(AcademicTermForm form) {
    academicTermRepository
        .findByYearAndTerm(form.getYear(), form.getSemester())
        .ifPresent(
            existing -> {
              throw new DuplicateAcademicTermException(form.getYear(), form.getSemester());
            });

    AcademicTerm academicTerm =
        AcademicTerm.builder()
            .academicYear(form.getYear())
            .semester(form.getSemester())
            .isCurrent(false)
            .build();

    academicTermRepository.save(academicTerm);
  }

  @Transactional(readOnly = true)
  public AcademicTermDto getAllAcademicTerms() {
    List<AcademicTerm> terms = academicTermRepository.findAllByYearDescAndSemesterDesc();
    List<AcademicTermItem> items =
        terms.stream()
            .map(
                term ->
                    new AcademicTermItem(
                        term.getAcademicTermId(),
                        term.getAcademicYear(),
                        term.getSemester(),
                        term.getIsCurrent()))
            .toList();

    return new AcademicTermDto(items);
  }

  @Transactional
  public void setCurrentTerm(Long id) {
    academicTermRepository.findCurrentSemester().ifPresent(term -> term.setCurrent(false));

    academicTermRepository
        .findById(id)
        .orElseThrow(NoCurrentTermFoundException::new)
        .setCurrent(true);
  }
}
