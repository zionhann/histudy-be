package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeamController {
    private final TeamRepository teamRepository;
    
}
