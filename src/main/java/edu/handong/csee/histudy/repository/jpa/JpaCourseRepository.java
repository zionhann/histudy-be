package edu.handong.csee.histudy.repository.jpa;

import edu.handong.csee.histudy.domain.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {

  List<Course> findAllByNameContainingIgnoreCase(String keyword);

  List<Course> findAllByAcademicTermIsCurrentTrue();
}
