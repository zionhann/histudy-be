package edu.handong.csee.histudy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.jpa.JpaStudyGroupRepository;
import edu.handong.csee.histudy.support.BaseRepositoryTest;
import edu.handong.csee.histudy.support.TestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class StudyGroupRepositoryTest extends BaseRepositoryTest {

  @Autowired private JpaStudyGroupRepository studyGroupRepository;

  @Test
  void 태그와학기로조회시_해당스터디그룹반환() {
    // Given
    StudyApplicant applicant1 =
        TestDataFactory.createStudyApplicant(currentTerm, user1, List.of(user2), List.of(course1));
    StudyApplicant applicant2 =
        TestDataFactory.createStudyApplicant(currentTerm, user2, List.of(user1), List.of(course1));

    StudyGroup studyGroup = StudyGroup.of(1, currentTerm, List.of(applicant1, applicant2));
    persistAndFlush(applicant1);
    persistAndFlush(applicant2);
    persistAndFlush(studyGroup);

    // When
    Optional<StudyGroup> result = studyGroupRepository.findByTagAndAcademicTerm(1, currentTerm);

    // Then
    assertThat(result).isPresent();
    assertStudyGroup(result.get(), 1, currentTerm);
  }

  @Test
  void 존재하지않는태그학기조회시_빈결과반환() {
    // When
    Optional<StudyGroup> result = studyGroupRepository.findByTagAndAcademicTerm(1, currentTerm);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void 최대태그번호조회시_가장큰태그번호반환() {
    // Given
    StudyGroup studyGroup1 = TestDataFactory.createStudyGroup(1, currentTerm);
    StudyGroup studyGroup2 = TestDataFactory.createStudyGroup(3, currentTerm);
    StudyGroup studyGroup3 = TestDataFactory.createStudyGroup(2, currentTerm);

    persistAndFlush(studyGroup1);
    persistAndFlush(studyGroup2);
    persistAndFlush(studyGroup3);

    // When
    Optional<Integer> maxTag = studyGroupRepository.countMaxTag(currentTerm);

    // Then
    assertThat(maxTag).isPresent();
    assertThat(maxTag.get()).isEqualTo(3);
  }

  @Test
  void 그룹없는학기최대태그조회시_빈결과반환() {
    // When
    Optional<Integer> maxTag = studyGroupRepository.countMaxTag(currentTerm);

    // Then
    assertThat(maxTag).isEmpty();
  }

  @Test
  void 학기별전체그룹조회시_태그순정렬된그룹목록반환() {
    // Given
    StudyGroup studyGroup3 = TestDataFactory.createStudyGroup(3, currentTerm);
    StudyGroup studyGroup1 = TestDataFactory.createStudyGroup(1, currentTerm);
    StudyGroup studyGroup2 = TestDataFactory.createStudyGroup(2, currentTerm);

    persistAndFlush(studyGroup3);
    persistAndFlush(studyGroup1);
    persistAndFlush(studyGroup2);

    // When
    List<StudyGroup> studyGroups = studyGroupRepository.findAllByAcademicTerm(currentTerm);

    // Then
    assertThat(studyGroups).hasSize(3);
    assertThat(studyGroups).extracting("tag").containsExactly(1, 2, 3);
  }

  @Test
  void 사용자와학기로그룹조회시_해당그룹반환() {
    // Given
    StudyApplicant applicant =
        TestDataFactory.createStudyApplicant(currentTerm, user1, List.of(user2), List.of(course1));
    StudyGroup studyGroup = StudyGroup.of(1, currentTerm, List.of(applicant));

    persistAndFlush(applicant);
    persistAndFlush(studyGroup);

    // When
    Optional<StudyGroup> result = studyGroupRepository.findByUserAndTerm(user1, currentTerm);

    // Then
    assertThat(result).isPresent();
    assertStudyGroup(result.get(), 1, currentTerm);
  }

  @Test
  void 그룹없는사용자학기조회시_빈결과반환() {
    // When
    Optional<StudyGroup> result = studyGroupRepository.findByUserAndTerm(user1, currentTerm);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @Transactional
  void 빈그룹삭제실행시_빈그룹만삭제성공() {
    // Given - 빈 그룹 생성
    StudyGroup emptyGroup = TestDataFactory.createStudyGroup(1, currentTerm);
    persistAndFlush(emptyGroup);

    // 멤버가 있는 그룹 생성
    StudyApplicant applicant =
        TestDataFactory.createStudyApplicant(currentTerm, user1, List.of(user2), List.of(course1));
    StudyGroup groupWithMembers = StudyGroup.of(2, currentTerm, List.of(applicant));
    persistAndFlush(applicant);
    persistAndFlush(groupWithMembers);

    // When
    studyGroupRepository.deleteEmptyGroup(currentTerm);
    flushAndClear();

    // Then
    List<StudyGroup> remainingGroups = studyGroupRepository.findAllByAcademicTerm(currentTerm);
    assertThat(remainingGroups).hasSize(1);
    assertThat(remainingGroups.get(0).getTag()).isEqualTo(2);
  }

  @Test
  void 새스터디그룹저장시_저장된그룹반환() {
    // Given
    StudyGroup studyGroup = TestDataFactory.createStudyGroup(1, currentTerm);

    // When
    StudyGroup savedGroup = studyGroupRepository.save(studyGroup);

    // Then
    assertThat(savedGroup.getStudyGroupId()).isNotNull();
    assertStudyGroup(savedGroup, 1, currentTerm);
  }

  @Test
  void 스터디그룹삭제시_삭제성공() {
    // Given
    StudyGroup studyGroup = TestDataFactory.createStudyGroup(1, currentTerm);
    StudyGroup savedGroup = persistAndFlush(studyGroup);

    // When
    studyGroupRepository.delete(savedGroup);
    flushAndClear();

    // Then
    Optional<StudyGroup> deletedGroup =
        studyGroupRepository.findByTagAndAcademicTerm(1, currentTerm);
    assertThat(deletedGroup).isEmpty();
  }

  @Test
  void 다른학기그룹조회시_학기별분리조회성공() {
    // Given
    StudyGroup currentGroup = TestDataFactory.createStudyGroup(1, currentTerm);
    StudyGroup pastGroup = TestDataFactory.createStudyGroup(1, pastTerm);

    persistAndFlush(currentGroup);
    persistAndFlush(pastGroup);

    // When
    List<StudyGroup> currentGroups = studyGroupRepository.findAllByAcademicTerm(currentTerm);
    List<StudyGroup> pastGroups = studyGroupRepository.findAllByAcademicTerm(pastTerm);

    // Then
    assertThat(currentGroups).hasSize(1);
    assertThat(currentGroups.get(0).getAcademicTerm()).isEqualTo(currentTerm);

    assertThat(pastGroups).hasSize(1);
    assertThat(pastGroups.get(0).getAcademicTerm()).isEqualTo(pastTerm);
  }
}
