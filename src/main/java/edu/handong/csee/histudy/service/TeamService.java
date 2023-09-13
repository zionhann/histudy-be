package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
        List<User> users = userRepository.findUnassignedApplicants();
        int latestGroupTag = (int) studyGroupRepository.count();
        AtomicInteger tag = new AtomicInteger(latestGroupTag + 1);

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

        // Third matching
        List<StudyGroup> matchedCourseSecond = matchCourseSecond(users, tag);

        // Results
        List<StudyGroup> matchedStudyGroups = new ArrayList<>(teamsWithFriends);
        matchedStudyGroups.addAll(teamsWithoutFriends);
        matchedStudyGroups.addAll(matchedCourseSecond);

        // Remove users who have already been matched
        users.removeAll(matchedCourseSecond.stream()
                .map(StudyGroup::getMembers)
                .flatMap(Collection::stream)
                .toList());

        return new TeamDto.MatchResults(matchedStudyGroups, userService.getInfoFromUser(users));
    }

    public List<StudyGroup> matchFriendFirst(List<User> users, AtomicInteger tag) {
        // First matching
        // Make teams with friends
        return users.stream()
                .map(User::getSentRequests)
                .flatMap(Collection::stream)
                .filter(Friendship::isAccepted)
                .map(f -> f.makeTeam(tag))
                .distinct()
                .toList();
    }

    public List<StudyGroup> matchCourseFirst(List<User> users, AtomicInteger tag) {
        List<StudyGroup> results = new ArrayList<>();
        Set<User> targetUsers = new HashSet<>(users);

        List<UserCourse> userCourses = targetUsers.stream()
                .flatMap(u ->
                        u.getCourseSelections().stream())
                .sorted(Comparator.comparingInt(UserCourse::getPriority))
                .toList();

        List<Integer> sortedKeys = userCourses.stream()
                .collect(Collectors.groupingBy(UserCourse::getPriority))
                .keySet().stream()
                .sorted()
                .toList();

        sortedKeys.forEach(priority -> {
            Map<Course, List<User>> courseToUserMap = userCourses.stream()
                    .filter(uc ->
                            uc.getPriority().equals(priority)
                                    && uc.getUser().isNotInAnyGroup())
                    .collect(Collectors.groupingBy(
                            UserCourse::getCourse,
                            Collectors.mapping(UserCourse::getUser, Collectors.toList())));

            courseToUserMap.forEach((course, _users) -> {
                List<StudyGroup> matchedGroupList = createGroup(_users, tag);
                results.addAll(matchedGroupList);
            });
        });
        return results;
    }

    private List<StudyGroup> createGroup(List<User> group, AtomicInteger tag) {
        List<StudyGroup> matchedGroupList = new ArrayList<>();

        while (group.size() >= 5) {
            // If the group has more than 5 elements, split the group
            // Split the group into 5 elements
            // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] -> [1, 2, 3, 4, 5], [6, 7, 8, 9, 10], [11]
            List<User> subGroup = new ArrayList<>(group.subList(0, 5));

            // Create a team with only 5 elements
            StudyGroup studyGroup = new StudyGroup(tag.getAndIncrement(), subGroup);
            matchedGroupList.add(studyGroup);

            // Remove the elements that have already been added to the team
            group.removeAll(subGroup);
        }
        if (group.size() >= 3) {
            // If the remaining elements are 3 ~ 4
            // Create a team with 3 ~ 4 elements
            StudyGroup studyGroup = new StudyGroup(tag.getAndIncrement(), group);
            matchedGroupList.add(studyGroup);
        }
        return matchedGroupList;
    }

    private List<StudyGroup> matchCourseSecond(List<User> users, AtomicInteger tag) {
        List<StudyGroup> results = new ArrayList<>();
        Set<User> targetUsers = new HashSet<>(users);

        Map<Course, PriorityQueue<User>> courseToUserByPriority = preparePriorityQueueOfUsers(targetUsers);

        // Make teams with 3 ~ 5 elements
        courseToUserByPriority.forEach((course, queue) -> {
            List<User> group = queue.stream()
                    .filter(User::isNotInAnyGroup)
                    .sorted(queue.comparator())
                    .collect(Collectors.toList());

            List<StudyGroup> matchedGroupList = createGroup(group, tag);
            results.addAll(matchedGroupList);
        });
        return results;
    }

    private Map<Course, PriorityQueue<User>> preparePriorityQueueOfUsers(Set<User> targetUsers) {
        // Group users by course
        Map<Course, List<UserCourse>> courseToUserCourses = targetUsers.stream()
                .flatMap(u -> u.getCourseSelections().stream())
                .collect(Collectors.groupingBy(
                        UserCourse::getCourse,
                        Collectors.mapping(
                                Function.identity(),
                                Collectors.toList())));

        Map<Course, PriorityQueue<User>> courseToUsersByPriority = new HashMap<>();
        courseToUserCourses.forEach((_course, _userCourses) -> {
                    _userCourses.sort(Comparator.comparingInt(UserCourse::getPriority));

                    List<User> sortedUsers = _userCourses.stream()
                            .map(UserCourse::getUser)
                            .toList();

                    PriorityQueue<User> userPriorityQueue = new PriorityQueue<>(
                            sortedUsers.size(),
                            Comparator.comparingInt(sortedUsers::indexOf));

                    userPriorityQueue.addAll(sortedUsers);
                    courseToUsersByPriority.put(_course, userPriorityQueue);
                }
        );
        return courseToUsersByPriority;
    }
}
