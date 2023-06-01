package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public TeamReportDto getTeamReports(long id, String email) {
        Team team = teamRepository.findById(id).orElseThrow();
        List<UserDto.Basic> users = team.getUsers().stream()
                                                        .map(u -> UserDto.Basic.builder()
                                                                .id(u.getId())
                                                                .sid(u.getSid())
                                                                .name(u.getName())
                                                                .build()).toList();
        List<ReportDto.Basic> reports = team.getReports()
                                            .stream()
                                            .map(ReportDto.Basic::new).toList();
        return new TeamReportDto(team.getId(),users,team.getTotalMinutes(),reports);
    }

}
