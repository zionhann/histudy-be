package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse,Long> {
}
