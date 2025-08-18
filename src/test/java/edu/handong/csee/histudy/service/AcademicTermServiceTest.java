package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.controller.form.AcademicTermForm;
import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.dto.AcademicTermDto;
import edu.handong.csee.histudy.service.repository.fake.FakeAcademicTermRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AcademicTermServiceTest {

  private AcademicTermService academicTermService;
  private FakeAcademicTermRepository academicTermRepository;

  @BeforeEach
  void init() {
    academicTermRepository = new FakeAcademicTermRepository();
    academicTermService = new AcademicTermService(academicTermRepository);
  }

  @Test
  void 학기_생성_성공() {
    // Given
    AcademicTermForm form = new AcademicTermForm(2025, TermType.SPRING);

    // When
    academicTermService.createAcademicTerm(form);

    // Then
    List<AcademicTerm> terms = academicTermRepository.findAll();
    assertThat(terms).hasSize(1);

    AcademicTerm savedTerm = terms.get(0);
    assertThat(savedTerm.getAcademicYear()).isEqualTo(2025);
    assertThat(savedTerm.getSemester()).isEqualTo(TermType.SPRING);
    assertThat(savedTerm.getIsCurrent()).isFalse();
  }

  @Test
  void 전체_학기_목록_조회_성공() {
    // Given
    academicTermRepository.save(
        AcademicTerm.builder().academicYear(2024).semester(TermType.FALL).isCurrent(false).build());

    academicTermRepository.save(
        AcademicTerm.builder()
            .academicYear(2025)
            .semester(TermType.SPRING)
            .isCurrent(true)
            .build());

    // When
    AcademicTermDto result = academicTermService.getAllAcademicTerms();

    // Then
    assertThat(result.academicTerms()).hasSize(2);

    List<AcademicTermForm> termForms = result.academicTerms();
    assertThat(termForms)
        .extracting(AcademicTermForm::getYear)
        .containsExactlyInAnyOrder(2024, 2025);

    assertThat(termForms)
        .extracting(AcademicTermForm::getTerm)
        .containsExactlyInAnyOrder(TermType.FALL, TermType.SPRING);
  }

  @Test
  void 학기_목록_연도_내림차순_정렬_조회() {
    // Given
    academicTermRepository.save(
        AcademicTerm.builder()
            .academicYear(2023)
            .semester(TermType.SPRING)
            .isCurrent(false)
            .build());

    academicTermRepository.save(
        AcademicTerm.builder().academicYear(2025).semester(TermType.FALL).isCurrent(false).build());

    academicTermRepository.save(
        AcademicTerm.builder()
            .academicYear(2024)
            .semester(TermType.SPRING)
            .isCurrent(true)
            .build());

    // When
    AcademicTermDto result = academicTermService.getAllAcademicTerms();

    // Then
    assertThat(result.academicTerms()).hasSize(3);

    List<AcademicTermForm> termForms = result.academicTerms();
    // 연도 내림차순으로 정렬되어야 함: 2025, 2024, 2023
    assertThat(termForms).extracting(AcademicTermForm::getYear).containsExactly(2025, 2024, 2023);
  }

  @Test
  void 빈_학기_목록_조회_성공() {
    // Given - 빈 저장소

    // When
    AcademicTermDto result = academicTermService.getAllAcademicTerms();

    // Then
    assertThat(result.academicTerms()).isEmpty();
  }

  @Test
  void 현재_학기_설정_성공() {
    // Given
    AcademicTerm term1 =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2024)
                .semester(TermType.FALL)
                .isCurrent(true)
                .build());

    AcademicTerm term2 =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2025)
                .semester(TermType.SPRING)
                .isCurrent(false)
                .build());

    // When
    academicTermService.setCurrentTerm(term2.getAcademicTermId());

    // Then
    List<AcademicTerm> allTerms = academicTermRepository.findAll();

    // term1은 더 이상 current가 아님
    AcademicTerm updatedTerm1 = academicTermRepository.findById(term1.getAcademicTermId()).get();
    assertThat(updatedTerm1.getIsCurrent()).isFalse();

    // term2가 current로 설정됨
    AcademicTerm updatedTerm2 = academicTermRepository.findById(term2.getAcademicTermId()).get();
    assertThat(updatedTerm2.getIsCurrent()).isTrue();

    // 현재 학기가 정확히 설정됨
    AcademicTerm currentTerm = academicTermRepository.findCurrentSemester().get();
    assertThat(currentTerm.getAcademicTermId()).isEqualTo(term2.getAcademicTermId());
  }

  @Test
  void 존재하지_않는_학기_설정시_예외발생() {
    // Given
    Long nonExistentId = 999L;

    // When & Then
    assertThatThrownBy(() -> academicTermService.setCurrentTerm(nonExistentId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Academic term not found");
  }

  @Test
  void 현재_학기가_없을때_설정_성공() {
    // Given
    AcademicTerm term =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2025)
                .semester(TermType.SPRING)
                .isCurrent(false)
                .build());

    // When
    academicTermService.setCurrentTerm(term.getAcademicTermId());

    // Then
    AcademicTerm currentTerm = academicTermRepository.findCurrentSemester().get();
    assertThat(currentTerm.getAcademicTermId()).isEqualTo(term.getAcademicTermId());
    assertThat(currentTerm.getIsCurrent()).isTrue();
  }
}
