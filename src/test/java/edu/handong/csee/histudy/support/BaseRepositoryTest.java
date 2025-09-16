package edu.handong.csee.histudy.support;

import static org.assertj.core.api.Assertions.assertThat;

import edu.handong.csee.histudy.config.JpaConfig;
import edu.handong.csee.histudy.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    protected AcademicTerm currentTerm;
    protected AcademicTerm pastTerm;
    protected User user1, user2, user3;
    protected Course course1, course2;

    @BeforeEach
    void baseSetup() {
        setupBaseEntities();
        persistBaseEntities();
    }

    private void setupBaseEntities() {
        currentTerm = TestDataFactory.createCurrentTerm();
        pastTerm = TestDataFactory.createPastTerm();
        user1 = TestDataFactory.createUser("1");
        user2 = TestDataFactory.createUser("2");
        user3 = TestDataFactory.createUser("3");
        course1 = TestDataFactory.createCourse("Introduction to Programming", "ECE101", currentTerm);
        course2 = TestDataFactory.createCourse("Database Systems", "ECE201", currentTerm);
    }

    private void persistBaseEntities() {
        entityManager.persistAndFlush(currentTerm);
        entityManager.persistAndFlush(pastTerm);
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.persistAndFlush(course1);
        entityManager.persistAndFlush(course2);
    }

    protected <T> T persistAndFlush(T entity) {
        return entityManager.persistAndFlush(entity);
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // 공통 검증 헬퍼 메서드들
    protected void assertAcademicTerm(AcademicTerm actual, int expectedYear, TermType expectedSemester, boolean expectedCurrent) {
        assertThat(actual.getAcademicYear()).isEqualTo(expectedYear);
        assertThat(actual.getSemester()).isEqualTo(expectedSemester);
        assertThat(actual.getIsCurrent()).isEqualTo(expectedCurrent);
    }

    protected void assertUser(User actual, String expectedSub, String expectedSid, String expectedEmail, String expectedName) {
        assertThat(actual.getSub()).isEqualTo(expectedSub);
        assertThat(actual.getSid()).isEqualTo(expectedSid);
        assertThat(actual.getEmail()).isEqualTo(expectedEmail);
        assertThat(actual.getName()).isEqualTo(expectedName);
    }

    protected void assertCourse(Course actual, String expectedName, String expectedCode, String expectedProfessor) {
        assertThat(actual.getName()).isEqualTo(expectedName);
        assertThat(actual.getCode()).isEqualTo(expectedCode);
        assertThat(actual.getProfessor()).isEqualTo(expectedProfessor);
    }

    protected void assertStudyGroup(StudyGroup actual, int expectedTag, AcademicTerm expectedTerm) {
        assertThat(actual.getTag()).isEqualTo(expectedTag);
        assertThat(actual.getAcademicTerm()).isEqualTo(expectedTerm);
    }
}