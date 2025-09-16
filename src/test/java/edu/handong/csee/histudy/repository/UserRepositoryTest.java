package edu.handong.csee.histudy.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.jpa.JpaUserRepository;
import edu.handong.csee.histudy.support.BaseRepositoryTest;
import edu.handong.csee.histudy.support.TestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

class UserRepositoryTest extends BaseRepositoryTest {

  @Autowired private JpaUserRepository userRepository;

  @Test
  void 유효한학번으로조회시_사용자반환() {
    // Given
    User user =
        TestDataFactory.createUser(
            "google-sub-901", "22500901", "test901@example.com", "Test User", Role.USER);
    userRepository.save(user);

    // When
    Optional<User> result = userRepository.findUserBySid("22500901");

    // Then
    assertThat(result).isPresent();
    assertUser(result.get(), "google-sub-901", "22500901", "test901@example.com", "Test User");
  }

  @Test
  void 존재하지않는학번으로조회시_빈결과반환() {
    // When
    Optional<User> result = userRepository.findUserBySid("99999999");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void 유효한이메일로조회시_사용자반환() {
    // Given
    User user =
        TestDataFactory.createUser(
            "google-sub-902", "22500902", "test902@example.com", "Test User", Role.USER);
    userRepository.save(user);

    // When
    Optional<User> result = userRepository.findUserByEmail("test902@example.com");

    // Then
    assertThat(result).isPresent();
    assertUser(result.get(), "google-sub-902", "22500902", "test902@example.com", "Test User");
  }

  @Test
  void 존재하지않는이메일로조회시_빈결과반환() {
    // When
    Optional<User> result = userRepository.findUserByEmail("nonexistent@example.com");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void 유효한SUB로조회시_사용자반환() {
    // Given
    User user =
        TestDataFactory.createUser(
            "google-sub-903", "22500903", "test903@example.com", "Test User", Role.USER);
    userRepository.save(user);

    // When
    Optional<User> result = userRepository.findUserBySub("google-sub-903");

    // Then
    assertThat(result).isPresent();
    assertUser(result.get(), "google-sub-903", "22500903", "test903@example.com", "Test User");
  }

  @Test
  void 키워드로검색시_일치하는사용자목록반환() {
    // Given
    User johnUser =
        TestDataFactory.createUser("sub-john", "22500904", "john@example.com", "John Doe", Role.USER);
    User janeUser =
        TestDataFactory.createUser(
            "sub-jane", "22500905", "jane@example.com", "Jane Smith", Role.USER);
    User bobUser =
        TestDataFactory.createUser("sub-bob", "22500906", "bob@test.com", "Bob Johnson", Role.USER);

    userRepository.save(johnUser);
    userRepository.save(janeUser);
    userRepository.save(bobUser);

    // When - 이름으로 검색 (부분 매칭으로 "Doe"만 검색)
    List<User> nameResults = userRepository.findUserByNameOrSidOrEmail("Doe");
    // When - 학번으로 검색
    List<User> sidResults = userRepository.findUserByNameOrSidOrEmail("22500905");
    // When - 이메일로 검색
    List<User> emailResults = userRepository.findUserByNameOrSidOrEmail("bob@test.com");

    // Then
    assertThat(nameResults).hasSize(1);
    assertThat(nameResults.get(0).getName()).isEqualTo("John Doe");

    assertThat(sidResults).hasSize(1);
    assertThat(sidResults.get(0).getSid()).isEqualTo("22500905");

    assertThat(emailResults).hasSize(1);
    assertThat(emailResults.get(0).getEmail()).isEqualTo("bob@test.com");
  }

  @Test
  void 일치하지않는키워드로검색시_빈결과반환() {
    // Given
    User user =
        TestDataFactory.createUser("sub-test", "22500907", "test907@example.com", "Test User", Role.USER);
    userRepository.save(user);

    // When
    List<User> results = userRepository.findUserByNameOrSidOrEmail("nonexistent");

    // Then
    assertThat(results).isEmpty();
  }

  @Test
  void 유효한ID로조회시_사용자반환() {
    // Given
    User user =
        TestDataFactory.createUser("sub-id", "22500908", "test908@example.com", "Test User", Role.USER);
    User savedUser = userRepository.save(user);

    // When
    Optional<User> result = userRepository.findById(savedUser.getUserId());

    // Then
    assertThat(result).isPresent();
    assertUser(result.get(), "sub-id", "22500908", "test908@example.com", "Test User");
  }

  @Test
  void 이름순정렬조회시_정렬된사용자목록반환() {
    // Given
    User charlieUser =
        TestDataFactory.createUser(
            "sub-charlie", "22500909", "charlie@example.com", "Charlie", Role.USER);
    User aliceUser =
        TestDataFactory.createUser("sub-alice", "22500910", "alice@example.com", "Alice", Role.USER);
    User bobUser =
        TestDataFactory.createUser("sub-bobby", "22500911", "bobby@example.com", "Bobby", Role.USER);

    userRepository.save(charlieUser);
    userRepository.save(aliceUser);
    userRepository.save(bobUser);

    // When - 이름으로 오름차순 정렬
    List<User> results = userRepository.findAll(Sort.by("name"));

    // Then
    assertThat(results).hasSize(6); // 3 + base users from BaseRepositoryTest
    // Check that our test users are sorted correctly
    List<User> testUsers =
        results.stream()
            .filter(
                u ->
                    u.getName().equals("Alice")
                        || u.getName().equals("Bobby")
                        || u.getName().equals("Charlie"))
            .toList();
    assertThat(testUsers).hasSize(3);
    assertThat(testUsers.get(0).getName()).isEqualTo("Alice");
    assertThat(testUsers.get(1).getName()).isEqualTo("Bobby");
    assertThat(testUsers.get(2).getName()).isEqualTo("Charlie");
  }

  @Test
  void 새사용자저장시_저장된사용자반환() {
    // Given
    User user =
        TestDataFactory.createUser(
            "google-sub-new", "22500999", "new@example.com", "New User", Role.USER);

    // When
    User savedUser = userRepository.save(user);

    // Then
    assertThat(savedUser.getUserId()).isNotNull();
    assertUser(savedUser, "google-sub-new", "22500999", "new@example.com", "New User");
    assertThat(savedUser.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void 중복이메일저장시_예외발생() {
    // Given
    User user1 =
        TestDataFactory.createUser("sub-dup1", "22500981", "same@example.com", "User 1", Role.USER);
    User user2 =
        TestDataFactory.createUser("sub-dup2", "22500982", "same@example.com", "User 2", Role.USER);

    userRepository.save(user1);
    flushAndClear();

    // When & Then
    assertThatThrownBy(
            () -> {
              userRepository.save(user2);
              flushAndClear();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void 중복학번저장시_예외발생() {
    // Given
    User user1 =
        TestDataFactory.createUser("sub-dup3", "22500983", "user1@example.com", "User 1", Role.USER);
    User user2 =
        TestDataFactory.createUser("sub-dup4", "22500983", "user2@example.com", "User 2", Role.USER);

    userRepository.save(user1);
    flushAndClear();

    // When & Then
    assertThatThrownBy(
            () -> {
              userRepository.save(user2);
              flushAndClear();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void 중복SUB저장시_예외발생() {
    // Given
    User user1 =
        TestDataFactory.createUser(
            "same-sub", "22500984", "user3@example.com", "User 1", Role.USER);
    User user2 =
        TestDataFactory.createUser(
            "same-sub", "22500985", "user4@example.com", "User 2", Role.USER);

    userRepository.save(user1);
    flushAndClear();

    // When & Then
    assertThatThrownBy(
            () -> {
              userRepository.save(user2);
              flushAndClear();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void 다양한역할사용자저장시_모든역할저장성공() {
    // Given
    User admin =
        TestDataFactory.createUser(
            "admin-sub", "00000001", "admin@example.com", "Admin User", Role.ADMIN);
    User member =
        TestDataFactory.createUser(
            "member-sub", "00000002", "member@example.com", "Member User", Role.MEMBER);
    User regularUser =
        TestDataFactory.createUser(
            "user-sub", "00000003", "user@example.com", "Regular User", Role.USER);

    // When
    User savedAdmin = userRepository.save(admin);
    User savedMember = userRepository.save(member);
    User savedUser = userRepository.save(regularUser);

    // Then
    assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
    assertThat(savedMember.getRole()).isEqualTo(Role.MEMBER);
    assertThat(savedUser.getRole()).isEqualTo(Role.USER);
  }
}