package edu.handong.csee.histudy.repository.impl;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.repository.jpa.JpaUserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
  private final JpaUserRepository repository;

  @Override
  public Optional<User> findUserBySid(String sid) {
    return repository.findUserBySid(sid);
  }

  @Override
  public List<User> findUserByNameOrSidOrEmail(String keyword) {
    return repository.findUserByNameOrSidOrEmail(keyword);
  }

  @Override
  public Optional<User> findUserByEmail(String email) {
    return repository.findUserByEmail(email);
  }

  @Override
  public Optional<User> findUserBySub(String sub) {
    return repository.findUserBySub(sub);
  }

  @Override
  public Optional<User> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public List<User> findAll(Sort sort) {
    return repository.findAll(sort);
  }

  @Override
  public User save(User user) {
    return repository.save(user);
  }
}
