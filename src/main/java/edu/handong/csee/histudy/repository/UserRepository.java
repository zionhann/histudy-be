package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserBySid(String sid);

    @Query("select u from User u " +
            "where u.name like %:keyword% " +
            "or u.sid like %:keyword% " +
            "or u.email like %:keyword%")
    List<User> findUserByNameOrSidOrEmail(@Param("keyword") String keyword);

    Optional<User> findUserByEmail(String email);

    List<User> findAllByStudyGroupIsNullAndCourseSelectionsIsNotEmpty();

    @Query("select u from User u where u.studyGroup is null and u.courseSelections is not empty")
    List<User> findUsersByTeamIsNull();

    @Query("select u from User u where u.sub = ?1")
    Optional<User> findUserBySub(String sub);
}
