package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Course;
import java.util.List;
import java.util.Optional;

public interface CourseRepository {
  List<Course> findAllByNameContainingIgnoreCase(String keyword);

  List<Course> findAllByAcademicTermIsCurrentTrue();

  List<Course> saveAll(List<Course> entities);

  boolean existsById(Long id);

  void deleteById(Long id);

  Optional<Course> findById(Long id);
}
