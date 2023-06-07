package edu.handong.csee.histudy.repository;

import edu.handong.csee.histudy.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByTag(int tag);
}