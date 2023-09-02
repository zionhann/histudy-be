package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.GroupCourse;
import edu.handong.csee.histudy.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupCourseRepository extends JpaRepository<GroupCourse, Long> {

    @Query("select distinct gc from GroupCourse gc join fetch gc.course where gc.studyGroup = :studyGroup")
    List<GroupCourse> findAllByStudyGroup(@Param("studyGroup") StudyGroup studyGroup);
}
