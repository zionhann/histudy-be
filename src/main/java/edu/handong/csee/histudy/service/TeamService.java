package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.CourseIdNameDto;
import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.dto.TeamIdDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

    public List<TeamDto> getTeams(String email) {
        List<Team> teams = teamRepository.findAll();
        List<TeamDto> result = new ArrayList<>();
        teams.forEach(t -> {
            List<User> users = t.getUsers();
            List<UserDto.Info> userInfos = users.stream()
                    .map(u -> {
                        List<User> friends = new ArrayList<>();
                        friends.addAll(u.getSentRequests()
                                .stream()
                                .filter(Friendship::isAccepted)
                                .map(Friendship::getReceived)
                                .toList());
                        friends.addAll(u.getReceivedRequests()
                                .stream()
                                .filter(Friendship::isAccepted)
                                .map(Friendship::getSent)
                                .toList());
                        List<UserDto.Basic> buddies = friends.stream().map(f -> UserDto.Basic.builder()
                                .id(f.getId())
                                .sid(f.getSid())
                                .name(f.getName())
                                .build()).toList();
                        List<CourseIdNameDto> courses = u.getChoices()
                                .stream()
                                .map(Choice::getCourse)
                                .map(Course::toIdNameDto)
                                .toList();
                        return UserDto.Info.builder()
                                .id(u.getId())
                                .sid(u.getSid())
                                .name(u.getName())
                                .friends(buddies)
                                .courses(courses)
                                .build();
                    }).toList();
            result.add(new TeamDto(t.getId(),userInfos,t.getReports().size(),t.getTotalMinutes()));
        });
        return result;
    }
    public int deleteTeam(TeamIdDto dto, String email) {
        if(teamRepository.existsById(dto.getGroupId())) {
            teamRepository.deleteById(dto.getGroupId());
            return 1;
        }
        return 0;
    }
}
