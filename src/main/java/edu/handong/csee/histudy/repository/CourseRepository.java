package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course,Long> {
}
