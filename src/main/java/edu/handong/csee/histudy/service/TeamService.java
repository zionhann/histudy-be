package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
  private final StudyGroupRepository studyGroupRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final ImagePathMapper imagePathMapper;
  private final AcademicTermRepository academicTermRepository;

  public List<TeamDto> getTeams(String email) {
    return studyGroupRepository.findAll(Sort.by(Sort.DEFAULT_DIRECTION, "tag")).stream()
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
    List<UserDto.UserBasic> users =
        studyGroup.getMembers().stream().map(UserDto.UserBasic::new).toList();
    List<ReportDto.ReportBasic> reports =
        studyGroup.getReports().stream()
            .map(
                report -> {
                  Map<Long, String> imgFullPaths =
                      imagePathMapper.parseImageToMapWithFullPath(report.getImages());
                  return new ReportDto.ReportBasic(report, imgFullPaths);
                })
            .toList();

    return new TeamReportDto(
        studyGroup.getId(), studyGroup.getTag(), users, studyGroup.getTotalMinutes(), reports);
  }

  public List<UserDto.UserMeWithMasking> getTeamUsers(String email) {
    User user = userRepository.findUserByEmail(email).orElseThrow(UserNotFoundException::new);

    return user.getStudyGroup().getMembers().stream().map(UserDto.UserMeWithMasking::new).toList();
  }

  public TeamRankDto getAllTeams() {
    AcademicTerm currentTerm =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);
    List<TeamRankDto.TeamInfo> teams =
        studyGroupRepository.findAllByAcademicTermOrderByDesc(currentTerm).stream()
            .map(
                group -> {
                  String path =
                      group.getReports().stream()
                          .reduce((first, second) -> second)
                          .flatMap(
                              (report ->
                                  report.getImages().stream().findFirst().map(Image::getPath)))
                          .orElse(null);
                  String fullPath = imagePathMapper.getFullPath(path);
                  return new TeamRankDto.TeamInfo(group, fullPath);
                })
            .toList();
    return new TeamRankDto(teams);
  }

  public TeamDto.MatchResults matchTeam() {
    // Get users who are not in a team
    List<User> users = userRepository.findUnassignedApplicants();
    AcademicTerm current =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);

    int latestGroupTag = studyGroupRepository.countMaxTag(current).orElse(0);
    AtomicInteger tag = new AtomicInteger(latestGroupTag + 1);

    // First matching
    List<StudyGroup> teamsWithFriends = matchFriendFirst(users, tag, current);

    // Remove users who have already been matched
    users.removeAll(
        teamsWithFriends.stream().map(StudyGroup::getMembers).flatMap(Collection::stream).toList());

    // Second matching
    List<StudyGroup> teamsWithoutFriends = matchCourseFirst(users, tag, current);

    // Remove users who have already been matched
    users.removeAll(
        teamsWithoutFriends.stream()
            .map(StudyGroup::getMembers)
            .flatMap(Collection::stream)
            .toList());

    // Third matching
    List<StudyGroup> matchedCourseSecond = matchCourseSecond(users, tag, current);

    // Results
    List<StudyGroup> matchedStudyGroups = new ArrayList<>(teamsWithFriends);
    matchedStudyGroups.addAll(teamsWithoutFriends);
    matchedStudyGroups.addAll(matchedCourseSecond);

    // Remove users who have already been matched
    users.removeAll(
        matchedCourseSecond.stream()
            .map(StudyGroup::getMembers)
            .flatMap(Collection::stream)
            .toList());

    return new TeamDto.MatchResults(matchedStudyGroups, userService.getInfoFromUser(users));
  }

  public List<StudyGroup> matchFriendFirst(
      List<User> users, AtomicInteger tag, AcademicTerm current) {
    // First matching
    // Make teams with friends
    return users.stream()
        .map(User::getSentRequests)
        .flatMap(Collection::stream)
        .filter(Friendship::isAccepted)
        .map(f -> f.makeTeam(tag, current))
        .distinct()
        .toList();
  }

  public List<StudyGroup> matchCourseFirst(
      List<User> users, AtomicInteger tag, AcademicTerm current) {
    List<StudyGroup> results = new ArrayList<>();
    Set<User> targetUsers = new HashSet<>(users);

    List<UserCourse> userCourses =
        targetUsers.stream()
            .flatMap(u -> u.getCourseSelections().stream())
            .sorted(Comparator.comparingInt(UserCourse::getPriority))
            .toList();

    List<Integer> sortedKeys =
        userCourses.stream()
            .collect(Collectors.groupingBy(UserCourse::getPriority))
            .keySet()
            .stream()
            .sorted()
            .toList();

    sortedKeys.forEach(
        priority -> {
          Map<Course, List<User>> courseToUserMap =
              userCourses.stream()
                  .filter(uc -> uc.getPriority().equals(priority) && uc.getUser().isNotInAnyGroup())
                  .collect(
                      Collectors.groupingBy(
                          UserCourse::getCourse,
                          Collectors.mapping(UserCourse::getUser, Collectors.toList())));

          courseToUserMap.forEach(
              (course, _users) -> {
                List<StudyGroup> matchedGroupList = createGroup(_users, tag, current);
                results.addAll(matchedGroupList);
              });
        });
    return results;
  }

  private List<StudyGroup> createGroup(List<User> group, AtomicInteger tag, AcademicTerm current) {
    List<StudyGroup> matchedGroupList = new ArrayList<>();

    while (group.size() >= 5) {
      // If the group has more than 5 elements, split the group
      // Split the group into 5 elements
      // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] -> [1, 2, 3, 4, 5], [6, 7, 8, 9, 10], [11]
      List<User> subGroup = new ArrayList<>(group.subList(0, 5));

      // Create a team with only 5 elements
      StudyGroup studyGroup = new StudyGroup(tag.getAndIncrement(), subGroup, current);
      matchedGroupList.add(studyGroup);

      // Remove the elements that have already been added to the team
      group.removeAll(subGroup);
    }
    if (group.size() >= 3) {
      // If the remaining elements are 3 ~ 4
      // Create a team with 3 ~ 4 elements
      StudyGroup studyGroup = new StudyGroup(tag.getAndIncrement(), group, current);
      matchedGroupList.add(studyGroup);
    }
    return matchedGroupList;
  }

  private List<StudyGroup> matchCourseSecond(
      List<User> users, AtomicInteger tag, AcademicTerm current) {
    List<StudyGroup> results = new ArrayList<>();
    Set<User> targetUsers = new HashSet<>(users);

    Map<Course, PriorityQueue<User>> courseToUserByPriority =
        preparePriorityQueueOfUsers(targetUsers);

    // Make teams with 3 ~ 5 elements
    courseToUserByPriority.forEach(
        (course, queue) -> {
          List<User> group =
              queue.stream()
                  .filter(User::isNotInAnyGroup)
                  .sorted(queue.comparator())
                  .collect(Collectors.toList());

          List<StudyGroup> matchedGroupList = createGroup(group, tag, current);
          results.addAll(matchedGroupList);
        });
    return results;
  }

  private Map<Course, PriorityQueue<User>> preparePriorityQueueOfUsers(Set<User> targetUsers) {
    // Group users by course
    Map<Course, List<UserCourse>> courseToUserCourses =
        targetUsers.stream()
            .flatMap(u -> u.getCourseSelections().stream())
            .collect(
                Collectors.groupingBy(
                    UserCourse::getCourse,
                    Collectors.mapping(Function.identity(), Collectors.toList())));

    Map<Course, PriorityQueue<User>> courseToUsersByPriority = new HashMap<>();
    courseToUserCourses.forEach(
        (_course, _userCourses) -> {
          _userCourses.sort(Comparator.comparingInt(UserCourse::getPriority));

          List<User> sortedUsers = _userCourses.stream().map(UserCourse::getUser).toList();

          PriorityQueue<User> userPriorityQueue =
              new PriorityQueue<>(
                  sortedUsers.size(), Comparator.comparingInt(sortedUsers::indexOf));

          userPriorityQueue.addAll(sortedUsers);
          courseToUsersByPriority.put(_course, userPriorityQueue);
        });
    return courseToUsersByPriority;
  }
}
