package edu.handong.csee.histudy.service.repository.fake;

import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.repository.CourseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeCourseRepository implements CourseRepository {

  private final List<Course> store = new ArrayList<>();
  private Long sequence = 1L;

  @Override
  public List<Course> findAllByNameContainingIgnoreCase(String keyword) {
    return store.stream()
        .filter(course -> course.getName().toLowerCase().contains(keyword))
        .collect(Collectors.toList());
  }

  @Override
  public List<Course> findAllByAcademicTermIsCurrentTrue() {
    return store.stream().filter(c -> c.getAcademicTerm().getIsCurrent()).toList();
  }

  @Override
  public List<Course> saveAll(List<Course> entities) {
    entities.forEach(course -> ReflectionTestUtils.setField(course, "courseId", sequence++));
    store.addAll(entities);
    return entities;
  }

  @Override
  public boolean existsById(Long id) {
    return store.stream().anyMatch(c -> c.getCourseId().equals(id));
  }

  @Override
  public void deleteById(Long id) {
    store.removeIf(c -> c.getCourseId().equals(id));
  }

  @Override
  public Optional<Course> findById(Long id) {
    return store.stream().filter(c -> c.getCourseId().equals(id)).findFirst();
  }

  public List<Course> findAll() {
    return new ArrayList<>(store);
  }
}
