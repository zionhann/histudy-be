package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

    public List<TeamDto> getTeams() {

    }
}
