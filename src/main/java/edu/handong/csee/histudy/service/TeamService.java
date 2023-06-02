package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public List<TeamDto> getTeams(String email) {
        List<Team> teams = teamRepository.findAll();
        List<TeamDto> result = new ArrayList<>();
        teams.forEach(t -> {
            List<User> users = t.getUsers();
            List<UserDto.UserInfo> userInfos = users.stream()
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
                        List<UserDto.UserBasic> buddies = friends.stream().map(f -> UserDto.UserBasic.builder()
                                .id(f.getId())
                                .sid(f.getSid())
                                .name(f.getName())
                                .build()).toList();
                        List<CourseIdNameDto> courses = u.getChoices()
                                .stream()
                                .map(Choice::getCourse)
                                .map(Course::toIdNameDto)
                                .toList();
                        return UserDto.UserInfo.builder()
                                .id(u.getId())
                                .sid(u.getSid())
                                .name(u.getName())
                                .friends(buddies)
                                .courses(courses)
                                .build();
                    }).toList();
            result.add(new TeamDto(t.getId(), userInfos, t.getReports().size(), t.getTotalMinutes()));
        });
        return result;
    }

    public int deleteTeam(TeamIdDto dto, String email) {
        if (teamRepository.existsById(dto.getGroupId())) {
            teamRepository.deleteById(dto.getGroupId());
            return 1;
        }
        return 0;
    }

    public TeamReportDto getTeamReports(long id, String email) {
        Team team = teamRepository.findById(id).orElseThrow();
        List<UserDto.UserBasic> users = team.getUsers().stream()
                .map(u -> UserDto.UserBasic.builder()
                        .id(u.getId())
                        .sid(u.getSid())
                        .name(u.getName())
                        .build()).toList();
        List<ReportDto.ReportBasic> reports = team.getReports()
                .stream()
                .map(ReportDto.ReportBasic::new).toList();
        return new TeamReportDto(team.getId(), users, team.getTotalMinutes(), reports);
    }

    public List<UserDto.UserBasic> getTeamUsers(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        return user.getTeam().getUsers()
                .stream()
                .map(UserDto.UserBasic::new)
                .toList();
    }

    public TeamRankDto getAllTeams() {
        List<TeamRankDto.TeamInfo> teams = teamRepository
                .findAll(Sort.by(Sort.Direction.DESC, "totalMinutes"))
                .stream()
                .map(TeamRankDto.TeamInfo::new)
                .toList();
        return new TeamRankDto(teams);
    }
}
