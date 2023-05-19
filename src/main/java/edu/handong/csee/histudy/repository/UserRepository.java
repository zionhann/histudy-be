package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findUserByAccessToken(String accessToken);

    Optional<User> findUserBySid(String sid);
}
