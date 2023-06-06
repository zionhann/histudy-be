package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
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
                        friends.addAll(u.getFriendships()
                                .stream()
                                .filter(Friendship::isAccepted)
                                .map(Friendship::getReceived)
                                .toList());
                        friends.addAll(u.getFriendships()
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

    public TeamDto.MatchResults matchTeam() {
        // Get users who are not in a team
        List<User> users = userRepository.findAllByTeamIsNull();
        AtomicInteger tag = new AtomicInteger(1);

        // First matching
        List<Team> teamsWithFriends = matchFriendFirst(users, tag);

        // Remove users who have already been matched
        users.removeAll(teamsWithFriends.stream()
                .map(Team::getUsers)
                .flatMap(Collection::stream)
                .toList());

        // Second matching
        List<Team> teamsWithoutFriends = matchCourseFirst(users, tag);

        // Remove users who have already been matched
        users.removeAll(teamsWithoutFriends.stream()
                .map(Team::getUsers)
                .flatMap(Collection::stream)
                .toList());

        // Results
        List<Team> matchedTeams = new ArrayList<>(teamsWithFriends);
        matchedTeams.addAll(teamsWithoutFriends);

        return new TeamDto.MatchResults(matchedTeams, users);
    }

    public List<Team> matchFriendFirst(List<User> users, AtomicInteger tag) {
        // First matching
        // Make teams with friends
        return users.stream()
                .map(User::getFriendships)
                .flatMap(Collection::stream)
                .filter(Friendship::isAccepted)
                .map(f -> f.makeTeam(tag))
                .distinct()
                .toList();
    }

    public List<Team> matchCourseFirst(List<User> users, AtomicInteger tag) {
        List<Team> teamsWithoutFriends = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            // Set priority: identical to the index of the choice
            final int priority = i;

            // Group users by course
            Map<Course, List<User>> entries = users.stream()
                    .collect(Collectors.groupingBy(
                            u -> u.getChoices()
                                    .get(priority)
                                    .getCourse()));

            // Make teams with 3 ~ 5 elements
            entries.forEach((course, group) -> {
                if (group.size() > 5) {
                    // If the group has more than 5 elements, split the group
                    while (group.size() / 5 > 0) {
                        // Split the group into 5 elements
                        // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] -> [1, 2, 3, 4, 5], [6, 7, 8, 9, 10], [11]
                        List<User> subGroup = group.subList(0, 5);

                        // Create a team with 3 ~ 5 elements
                        Team team = new Team(tag.getAndIncrement());
                        team.enroll(subGroup);
                        teamsWithoutFriends.add(team);

                        // Remove the elements that have already been added to the team
                        group.removeAll(subGroup);
                    }
                }

                if (group.size() >= 3) {
                    // If the remaining elements are 3 ~ 5
                    // Create a team with 3 ~ 5 elements
                    Team team = new Team(tag.getAndIncrement());
                    team.enroll(group);
                    teamsWithoutFriends.add(team);
                }
            });
            // Remove users who have already been matched
            users.removeAll(teamsWithoutFriends.stream()
                    .flatMap(t -> t.getUsers().stream())
                    .toList());
        }
        return teamsWithoutFriends;
    }
}
