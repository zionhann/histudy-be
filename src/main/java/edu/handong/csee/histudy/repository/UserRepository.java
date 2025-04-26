package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

public interface UserRepository {
  Optional<User> findUserBySid(String sid);

  List<User> findUserByNameOrSidOrEmail(String keyword);

  Optional<User> findUserByEmail(String email);

  Optional<User> findUserBySub(String sub);

  Optional<User> findById(Long id);

  List<User> findAll(Sort sort);

  User save(User entity);
}
