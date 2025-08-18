package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.AcademicTermForm;
import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.dto.AcademicTermDto;
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
    AcademicTerm academicTerm =
        AcademicTerm.builder()
            .academicYear(form.getYear())
            .semester(form.getTerm())
            .isCurrent(false)
            .build();

    academicTermRepository.save(academicTerm);
  }

  @Transactional(readOnly = true)
  public AcademicTermDto getAllAcademicTerms() {
    List<AcademicTerm> terms = academicTermRepository.findAllByYearDesc();

    List<AcademicTermForm> termForms =
        terms.stream()
            .map(term -> new AcademicTermForm(term.getAcademicYear(), term.getSemester()))
            .toList();

    return new AcademicTermDto(termForms);
  }

  @Transactional
  public void setCurrentTerm(Long id) {
    AcademicTerm targetTerm =
        academicTermRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Academic term not found"));
    academicTermRepository.setAllCurrentToFalse();
    targetTerm.setCurrent(true);
  }
}
