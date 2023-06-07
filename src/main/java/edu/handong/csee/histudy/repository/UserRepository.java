package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findUserBySid(String sid);

    @Query("select u from User u " +
            "where u.name like %:keyword% " +
            "or u.sid like %:keyword% " +
            "or u.email like %:keyword%")
    List<User> findUserByNameOrSidOrEmail(String keyword);

    Optional<User> findUserByEmail(String email);

    List<User> findAllByTeamIsNullAndChoicesIsNotEmpty();

    @Query("select u from User u where u.team is null")
    List<User> findUsersByTeamIsNull();
    Optional<User> findUserBySub(String sub);

    Optional<User> findById(long id);
}
