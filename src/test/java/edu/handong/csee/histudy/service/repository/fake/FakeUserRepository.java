package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

public class FakeUserRepository implements UserRepository {

  private final List<User> store = new ArrayList<>();
  private Long sequence = 1L;

  @Override
  public Optional<User> findUserBySid(String sid) {
    return store.stream().filter(c -> c.getSid().equals(sid)).findFirst();
  }

  @Override
  public List<User> findUserByNameOrSidOrEmail(String keyword) {
    return store.stream()
        .filter(
            e ->
                e.getName().contains(keyword)
                    || e.getSid().contains(keyword)
                    || e.getEmail().contains(keyword))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<User> findUserByEmail(String email) {
    return store.stream().filter(e -> e.getEmail().equals(email)).findFirst();
  }

  @Override
  public Optional<User> findUserBySub(String sub) {
    return store.stream().filter(e -> e.getSub().equals(sub)).findFirst();
  }

  @Override
  public Optional<User> findById(Long id) {
    return store.stream().filter(e -> e.getUserId().equals(id)).findFirst();
  }

  @Override
  public List<User> findAll(Sort sort) {
    return store.stream()
        .sorted(
            (u1, u2) ->
                sort.stream()
                    .mapToInt(
                        order -> {
                          String property = order.getProperty();
                          int comparison =
                              switch (property) {
                                case "name" -> u1.getName().compareTo(u2.getName());
                                case "sid" -> u1.getSid().compareTo(u2.getSid());
                                case "email" -> u1.getEmail().compareTo(u2.getEmail());
                                case "userId" -> u1.getUserId().compareTo(u2.getUserId());
                                default ->
                                    throw new IllegalArgumentException(
                                        "Invalid sorting property: " + property);
                              };
                          return order.isAscending() ? comparison : -comparison;
                        })
                    .filter(comparison -> comparison != 0)
                    .findFirst()
                    .orElse(0))
        .collect(Collectors.toList());
  }

  @Override
  public User save(User user) {
    try {
      var field = User.class.getDeclaredField("userId");
      field.setAccessible(true);
      field.set(user, sequence++);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set userId via reflection", e);
    }
    store.add(user);
    return user;
  }
}
