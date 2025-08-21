package edu.handong.csee.histudy.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.controller.form.AcademicTermForm;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.exception.DuplicateAcademicTermException;
import edu.handong.csee.histudy.service.AcademicTermService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DuplicateAcademicTermIntegrationTest {

  @Autowired private AcademicTermService academicTermService;

  @Test
  void 중복_학기_생성시_DuplicateAcademicTermException_발생() {
    // Given - 첫 번째 학기 생성
    AcademicTermForm form1 = new AcademicTermForm(2024, TermType.SPRING);
    academicTermService.createAcademicTerm(form1);

    // When & Then - 동일한 학기 재생성 시도
    AcademicTermForm duplicateForm = new AcademicTermForm(2024, TermType.SPRING);

    assertThatThrownBy(() -> academicTermService.createAcademicTerm(duplicateForm))
        .isInstanceOf(DuplicateAcademicTermException.class)
        .hasMessage("Academic term already exists for year 2024 and semester SPRING");
  }

  @Test
  void 다른_학기_생성시_정상_동작() {
    // Given - 첫 번째 학기 생성
    AcademicTermForm form1 = new AcademicTermForm(2024, TermType.SPRING);
    academicTermService.createAcademicTerm(form1);

    // When - 다른 학기 생성 (같은 연도, 다른 학기)
    AcademicTermForm form2 = new AcademicTermForm(2024, TermType.FALL);

    // Then - 예외 발생하지 않음
    academicTermService.createAcademicTerm(form2);

    // 두 학기 모두 생성됨을 확인
    var allTerms = academicTermService.getAllAcademicTerms();
    assertThat(allTerms.academicTerms()).hasSize(2);
  }

  @Test
  void 같은_학기_다른_연도_생성시_정상_동작() {
    // Given - 첫 번째 학기 생성
    AcademicTermForm form1 = new AcademicTermForm(2024, TermType.SPRING);
    academicTermService.createAcademicTerm(form1);

    // When - 다른 연도, 같은 학기 생성
    AcademicTermForm form2 = new AcademicTermForm(2025, TermType.SPRING);

    // Then - 예외 발생하지 않음
    academicTermService.createAcademicTerm(form2);

    // 두 학기 모두 생성됨을 확인
    var allTerms = academicTermService.getAllAcademicTerms();
    assertThat(allTerms.academicTerms()).hasSize(2);
  }
}
