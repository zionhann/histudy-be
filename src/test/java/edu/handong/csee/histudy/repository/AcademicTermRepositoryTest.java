package edu.handong.csee.histudy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.repository.jpa.JpaAcademicTermRepository;
import edu.handong.csee.histudy.support.BaseRepositoryTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AcademicTermRepositoryTest extends BaseRepositoryTest {

  @Autowired private JpaAcademicTermRepository academicTermRepository;

  @Test
  void 현재학기조회시_현재학기반환() {
    // When - 이미 BaseRepositoryTest에서 currentTerm이 생성됨
    Optional<AcademicTerm> result = academicTermRepository.findCurrentSemester();

    // Then
    assertThat(result).isPresent();
    assertAcademicTerm(result.get(), 2025, TermType.SPRING, true);
  }

  @Test
  void 특정년도학기조회시_해당학기반환() {
    // Given - 별도의 과거 학기 데이터 생성
    AcademicTerm testTerm =
        AcademicTerm.builder()
            .academicYear(2020)
            .semester(TermType.WINTER)
            .isCurrent(false)
            .build();
    persistAndFlush(testTerm);

    // When
    Optional<AcademicTerm> result = academicTermRepository.findByYearAndTerm(2020, TermType.WINTER);

    // Then - 해당 학기는 현재 학기가 아님
    assertThat(result).isPresent();
    assertAcademicTerm(result.get(), 2020, TermType.WINTER, false);
  }

  @Test
  void 년도와학기로조회시_해당학기반환() {
    // When - 이미 BaseRepositoryTest에서 currentTerm이 생성됨
    Optional<AcademicTerm> result = academicTermRepository.findByYearAndTerm(2025, TermType.SPRING);

    // Then
    assertThat(result).isPresent();
    assertAcademicTerm(result.get(), 2025, TermType.SPRING, true);
  }

  @Test
  void 존재하지않는년도학기조회시_빈결과반환() {
    // When - 존재하지 않는 학기로 조회
    Optional<AcademicTerm> result = academicTermRepository.findByYearAndTerm(2023, TermType.WINTER);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void 새학기저장시_저장된학기반환() {
    // Given
    AcademicTerm term =
        AcademicTerm.builder()
            .academicYear(2025)
            .semester(TermType.SUMMER)
            .isCurrent(false)
            .build();

    // When
    AcademicTerm savedTerm = academicTermRepository.save(term);

    // Then
    assertThat(savedTerm.getAcademicTermId()).isNotNull();
    assertAcademicTerm(savedTerm, 2025, TermType.SUMMER, false);
  }
}
