package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

}
