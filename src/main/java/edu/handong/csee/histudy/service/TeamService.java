package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Friendship;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<TeamDto> getTeams(String email) {
        return studyGroupRepository.findAll(
                        Sort.by(Sort.DEFAULT_DIRECTION, "tag"))
                .stream()
                .map(TeamDto::new)
                .toList();
    }

    public int deleteTeam(TeamIdDto dto, String email) {
        if (studyGroupRepository.existsById(dto.getGroupId())) {
            studyGroupRepository.deleteById(dto.getGroupId());
            return 1;
        }
        return 0;
    }

    public TeamReportDto getTeamReports(long id, String email) {
        StudyGroup studyGroup = studyGroupRepository.findById(id).orElseThrow();
        List<UserDto.UserBasic> users = studyGroup.getMembers().stream()
                .map(u -> UserDto.UserBasic.builder()
                        .id(u.getId())
                        .sid(u.getSid())
                        .name(u.getName())
                        .build()).toList();
        List<ReportDto.ReportBasic> reports = studyGroup.getReports()
                .stream()
                .map(ReportDto.ReportBasic::new).toList();
        return new TeamReportDto(studyGroup.getId(), users, studyGroup.getTotalMinutes(), reports);
    }

    public List<UserDto.UserMe> getTeamUsers(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        if (user.getStudyGroup() != null) {
            return user.getStudyGroup().getMembers()
                    .stream()
                    .map(UserDto.UserMe::new)
                    .toList();
        } else return Collections.emptyList();
    }

    public TeamRankDto getAllTeams() {
        List<TeamRankDto.TeamInfo> teams = studyGroupRepository
                .findAll(Sort.by(Sort.Direction.DESC, "totalMinutes"))
                .stream()
                .map(TeamRankDto.TeamInfo::new)
                .toList();
        return new TeamRankDto(teams);
    }

    public TeamDto.MatchResults matchTeam() {
        // Get users who are not in a team
        List<User> users = userRepository.findAllByStudyGroupIsNullAndCourseSelectionsIsNotEmpty();
        AtomicInteger tag = new AtomicInteger(1);

        // First matching
        List<StudyGroup> teamsWithFriends = matchFriendFirst(users, tag);

        // Remove users who have already been matched
        users.removeAll(teamsWithFriends.stream()
                .map(StudyGroup::getMembers)
                .flatMap(Collection::stream)
                .toList());

        // Second matching
        List<StudyGroup> teamsWithoutFriends = matchCourseFirst(users, tag);

        // Remove users who have already been matched
        users.removeAll(teamsWithoutFriends.stream()
                .map(StudyGroup::getMembers)
                .flatMap(Collection::stream)
                .toList());

        // Results
        List<StudyGroup> matchedStudyGroups = new ArrayList<>(teamsWithFriends);
        matchedStudyGroups.addAll(teamsWithoutFriends);

        return new TeamDto.MatchResults(matchedStudyGroups, userService.getInfoFromUser(users));
    }

    public List<StudyGroup> matchFriendFirst(List<User> users, AtomicInteger tag) {
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

    public List<StudyGroup> matchCourseFirst(List<User> users, AtomicInteger tag) {
        List<StudyGroup> teamsWithoutFriends = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            // Set priority: identical to the index of the choice
            final int priority = i;

            // Group users by course
            Map<Course, List<User>> entries = users.stream()
                    .collect(Collectors.groupingBy(
                            u -> u.getCourseSelections()
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

                        // Create a team with only 5 elements
                        StudyGroup studyGroup = new StudyGroup(tag.getAndIncrement(), subGroup);
                        teamsWithoutFriends.add(studyGroup);

                        // Remove the elements that have already been added to the team
                        group.removeAll(subGroup);
                    }
                }

                if (group.size() >= 3) {
                    // If the remaining elements are 3 ~ 5
                    // Create a team with 3 ~ 5 elements
                    StudyGroup studyGroup = new StudyGroup(tag.getAndIncrement(), group);
                    teamsWithoutFriends.add(studyGroup);
                }
            });
            // Remove users who have already been matched
            users.removeAll(teamsWithoutFriends.stream()
                    .flatMap(t -> t.getMembers().stream())
                    .toList());
        }
        return teamsWithoutFriends;
    }
}
