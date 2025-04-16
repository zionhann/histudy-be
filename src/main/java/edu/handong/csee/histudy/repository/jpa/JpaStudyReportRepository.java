package edu.handong.csee.histudy.repository.jpa;

import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.StudyReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudyReportRepository extends JpaRepository<StudyReport, Long> {

  List<StudyReport> findAllByStudyGroupOrderByCreatedDateDesc(StudyGroup studyGroup);
}
