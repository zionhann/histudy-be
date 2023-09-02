package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.GroupReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupReportRepository extends JpaRepository<GroupReport, Long> {

}
