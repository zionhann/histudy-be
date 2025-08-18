package edu.handong.csee.histudy.service;

import static edu.handong.csee.histudy.dto.AcademicTermDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.controller.form.AcademicTermForm;
import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.dto.AcademicTermDto;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
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

    List<AcademicTermItem> termForms = result.academicTerms();
    assertThat(termForms).extracting(AcademicTermItem::year).containsExactlyInAnyOrder(2024, 2025);

    assertThat(termForms)
        .extracting(AcademicTermItem::semester)
        .containsExactlyInAnyOrder(TermType.FALL, TermType.SPRING);
  }

  @Test
  void 학기_목록_연도_내림차순_학기_내림차순_정렬_조회() {
    // Given - 같은 연도에 여러 학기가 있는 경우 포함
    academicTermRepository.save(
        AcademicTerm.builder()
            .academicYear(2024)
            .semester(TermType.SPRING)
            .isCurrent(false)
            .build());

    academicTermRepository.save(
        AcademicTerm.builder()
            .academicYear(2024)
            .semester(TermType.WINTER)
            .isCurrent(false)
            .build());

    academicTermRepository.save(
        AcademicTerm.builder().academicYear(2025).semester(TermType.FALL).isCurrent(false).build());

    academicTermRepository.save(
        AcademicTerm.builder().academicYear(2024).semester(TermType.FALL).isCurrent(false).build());

    academicTermRepository.save(
        AcademicTerm.builder()
            .academicYear(2023)
            .semester(TermType.SUMMER)
            .isCurrent(true)
            .build());

    // When
    AcademicTermDto result = academicTermService.getAllAcademicTerms();

    // Then
    assertThat(result.academicTerms()).hasSize(5);

    List<AcademicTermItem> termForms = result.academicTerms();

    // 연도 내림차순, 같은 연도에서는 학기 내림차순
    // 2025 FALL, 2024 WINTER, 2024 FALL, 2024 SPRING, 2023 SUMMER
    assertThat(termForms.get(0).year()).isEqualTo(2025);
    assertThat(termForms.get(0).semester()).isEqualTo(TermType.FALL);

    assertThat(termForms.get(1).year()).isEqualTo(2024);
    assertThat(termForms.get(1).semester()).isEqualTo(TermType.WINTER);

    assertThat(termForms.get(2).year()).isEqualTo(2024);
    assertThat(termForms.get(2).semester()).isEqualTo(TermType.FALL);

    assertThat(termForms.get(3).year()).isEqualTo(2024);
    assertThat(termForms.get(3).semester()).isEqualTo(TermType.SPRING);

    assertThat(termForms.get(4).year()).isEqualTo(2023);
    assertThat(termForms.get(4).semester()).isEqualTo(TermType.SUMMER);

    // 연도가 내림차순인지 확인
    List<Integer> years = termForms.stream().map(AcademicTermItem::year).toList();
    for (int i = 0; i < years.size() - 1; i++) {
      assertThat(years.get(i)).isGreaterThanOrEqualTo(years.get(i + 1));
    }
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
        .isInstanceOf(NoCurrentTermFoundException.class);
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

  @Test
  void 현재_학기_변경시_이전_현재학기만_false로_변경() {
    // Given - 여러 학기 중 하나가 현재 학기인 상황
    AcademicTerm term1 =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2023)
                .semester(TermType.FALL)
                .isCurrent(false)
                .build());

    AcademicTerm term2 =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2024)
                .semester(TermType.SPRING)
                .isCurrent(true) // 현재 학기
                .build());

    AcademicTerm term3 =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2024)
                .semester(TermType.FALL)
                .isCurrent(false)
                .build());

    AcademicTerm term4 =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2025)
                .semester(TermType.SPRING)
                .isCurrent(false)
                .build());

    // When - term4를 새로운 현재 학기로 설정
    academicTermService.setCurrentTerm(term4.getAcademicTermId());

    // Then - term2만 false로 변경되고, term4가 true로 변경됨
    AcademicTerm updatedTerm1 = academicTermRepository.findById(term1.getAcademicTermId()).get();
    AcademicTerm updatedTerm2 = academicTermRepository.findById(term2.getAcademicTermId()).get();
    AcademicTerm updatedTerm3 = academicTermRepository.findById(term3.getAcademicTermId()).get();
    AcademicTerm updatedTerm4 = academicTermRepository.findById(term4.getAcademicTermId()).get();

    // term1과 term3는 원래 false였으므로 그대로 유지
    assertThat(updatedTerm1.getIsCurrent()).isFalse();
    assertThat(updatedTerm3.getIsCurrent()).isFalse();

    // term2는 현재 학기였으므로 false로 변경됨
    assertThat(updatedTerm2.getIsCurrent()).isFalse();

    // term4가 새로운 현재 학기로 설정됨
    assertThat(updatedTerm4.getIsCurrent()).isTrue();

    // 현재 학기 조회시 term4가 반환됨
    AcademicTerm currentTerm = academicTermRepository.findCurrentSemester().get();
    assertThat(currentTerm.getAcademicTermId()).isEqualTo(term4.getAcademicTermId());
  }

  @Test
  void 같은_학기를_다시_현재학기로_설정시_정상작동() {
    // Given - 이미 현재 학기인 학기가 있는 상황
    AcademicTerm currentTerm =
        academicTermRepository.save(
            AcademicTerm.builder()
                .academicYear(2024)
                .semester(TermType.SPRING)
                .isCurrent(true)
                .build());

    // When - 같은 학기를 다시 현재 학기로 설정
    academicTermService.setCurrentTerm(currentTerm.getAcademicTermId());

    // Then - 여전히 현재 학기로 유지됨
    AcademicTerm result = academicTermRepository.findById(currentTerm.getAcademicTermId()).get();
    assertThat(result.getIsCurrent()).isTrue();

    AcademicTerm foundCurrentTerm = academicTermRepository.findCurrentSemester().get();
    assertThat(foundCurrentTerm.getAcademicTermId()).isEqualTo(currentTerm.getAcademicTermId());
  }
}
