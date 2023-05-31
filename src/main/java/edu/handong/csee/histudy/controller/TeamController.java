package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeamController {
    private final TeamRepository teamRepository;

    public ResponseEntity<List<TeamDto>> getTeams() {
        return ResponseEntity.ok(new ArrayList<>());
    }
}
