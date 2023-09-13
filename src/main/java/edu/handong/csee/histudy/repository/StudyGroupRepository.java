package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Optional<StudyGroup> findByTag(int tag);

    @Modifying
    @Query("delete from StudyGroup s where s.tag = -1")
    void deleteEmptyGroup();
}