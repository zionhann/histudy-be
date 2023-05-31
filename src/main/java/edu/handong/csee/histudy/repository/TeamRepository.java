package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}