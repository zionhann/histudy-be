package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.repository.StudyApplicantRepository;
import edu.handong.csee.histudy.util.DFS;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
  private final StudyGroupRepository studyGroupRepository;
  private final UserRepository userRepository;
  private final AcademicTermRepository academicTermRepository;
  private final StudyApplicantRepository studyApplicantRepository;
  private final StudyReportRepository studyReportRepository;

  private final ImagePathMapper imagePathMapper;

  public List<TeamDto> getTeams(String email) {
    AcademicTerm currentTerm =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);
    List<StudyGroup> groups = studyGroupRepository.findAllByAcademicTerm(currentTerm);

    return groups.stream()
        .map(
            group -> {
              List<StudyReport> reports =
                  studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(group);
              List<StudyApplicant> applicants = studyApplicantRepository.findAllByStudyGroup(group);

              Map<User, StudyApplicant> applicantMap =
                  applicants.stream()
                      .collect(Collectors.toMap(StudyApplicant::getUser, Function.identity()));

              return new TeamDto(group, reports, applicantMap);
            })
        .toList();
  }

  public TeamReportDto getTeamReports(long id, String email) {
    StudyGroup studyGroup = studyGroupRepository.findById(id).orElseThrow();
    List<UserDto.UserBasic> users =
        studyGroup.getMembers().stream()
            .map(StudyApplicant::getUser)
            .map(UserDto.UserBasic::new)
            .toList();

    List<StudyReport> studyReports =
        studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(studyGroup);
    List<ReportDto.ReportBasic> reports =
        studyReports.stream()
            .map(
                report -> {
                  Map<Long, String> imgFullPaths =
                      imagePathMapper.parseImageToMapWithFullPath(report.getImages());
                  return new ReportDto.ReportBasic(report, imgFullPaths);
                })
            .toList();

    return new TeamReportDto(
        studyGroup.getStudyGroupId(),
        studyGroup.getTag(),
        users,
        calculateTotalMinutes(studyReports),
        reports);
  }

  private long calculateTotalMinutes(List<StudyReport> reports) {
    return reports.stream().mapToLong(StudyReport::getTotalMinutes).sum();
  }

  public List<UserDto.UserMeWithMasking> getTeamUsers(String email) {
    User user = userRepository.findUserByEmail(email).orElseThrow(UserNotFoundException::new);
    AcademicTerm currentTerm =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);
    StudyGroup studyGroup = studyGroupRepository.findByUserAndTerm(user, currentTerm).orElseThrow();

    return studyGroup.getMembers().stream()
        .map(StudyApplicant::getUser)
        .map(_user -> new UserDto.UserMeWithMasking(_user, studyGroup.getTag()))
        .toList();
  }

  public TeamRankDto getAllTeams() {
    AcademicTerm currentTerm =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);
    List<StudyGroup> currentStudyGroups = studyGroupRepository.findAllByAcademicTerm(currentTerm);

    List<TeamRankDto.TeamInfo> teams =
        currentStudyGroups.stream()
            .map(
                group -> {
                  List<StudyReport> reports =
                      studyReportRepository.findAllByStudyGroupOrderByCreatedDateDesc(group);

                  String path =
                      reports.stream()
                          .findFirst()
                          .flatMap(
                              report ->
                                  report.getImages().stream()
                                      .max(Comparator.comparing(ReportImage::getCreatedDate))
                                      .map(ReportImage::getPath))
                          .orElse(null);
                  String fullPath = imagePathMapper.getFullPath(path);

                  return new TeamRankDto.TeamInfo(group, reports, fullPath);
                })
            .sorted(Comparator.comparing(TeamRankDto.TeamInfo::getTotalMinutes).reversed())
            .toList();
    return new TeamRankDto(teams);
  }

  public void matchTeam() {
    AcademicTerm current =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);
    List<StudyApplicant> allApplicants = studyApplicantRepository.findUnassignedApplicants(current);

    if (allApplicants.isEmpty()) {
      return;
    }

    int latestGroupTag = studyGroupRepository.countMaxTag(current).orElse(0);
    AtomicInteger tag = new AtomicInteger(latestGroupTag + 1);

    // First matching - friend-based
    List<StudyGroup> friendGroups = matchFriendFirst(allApplicants, tag, current);
    List<StudyGroup> allMatchedGroups = new ArrayList<>(friendGroups);

    // Second matching - course-based with priority (remaining unassigned applicants)
    Set<StudyGroup> courseFirstGroups = matchCourseFirst(allApplicants, tag, current);
    allMatchedGroups.addAll(courseFirstGroups);

    // Third matching - remaining course-based (final unassigned applicants)
    Set<StudyGroup> courseSecondGroups = matchCourseSecond(allApplicants, tag, current);
    allMatchedGroups.addAll(courseSecondGroups);

    if (!allMatchedGroups.isEmpty()) {
      studyGroupRepository.saveAll(allMatchedGroups);
    }
  }

  public List<StudyGroup> matchFriendFirst(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    if (applicants.isEmpty()) {
      return new ArrayList<>();
    }
    return new DFS<>(buildFriendshipMap(applicants))
        .execute().stream()
            .map(friends -> StudyGroup.of(tag.getAndIncrement(), current, friends))
            .toList();
  }

  private Map<StudyApplicant, List<StudyApplicant>> buildFriendshipMap(
      List<StudyApplicant> applicants) {
    Map<User, StudyApplicant> userToApplicant =
        applicants.stream().collect(Collectors.toMap(StudyApplicant::getUser, Function.identity()));

    return applicants.stream()
        .flatMap(applicant -> applicant.getPartnerRequests().stream())
        .filter(StudyPartnerRequest::isAccepted)
        .collect(
            Collectors.groupingBy(
                StudyPartnerRequest::getSender,
                Collectors.mapping(
                    r -> userToApplicant.get(r.getReceiver()), Collectors.toList())));
  }

  public Set<StudyGroup> matchCourseFirst(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    if (applicants.isEmpty()) {
      return new HashSet<>();
    }

    Set<StudyGroup> results = new HashSet<>();

    List<PreferredCourse> preferredCourses =
        applicants.stream()
            .filter(applicant -> !applicant.hasStudyGroup())
            .flatMap(applicant -> applicant.getPreferredCourses().stream())
            .sorted(Comparator.comparingInt(PreferredCourse::getPriority))
            .toList();

    List<Integer> sortedKeys =
        preferredCourses.stream()
            .collect(Collectors.groupingBy(PreferredCourse::getPriority))
            .keySet()
            .stream()
            .sorted()
            .toList();

    sortedKeys.forEach(
        priority -> {
          Map<Course, List<StudyApplicant>> courseToUserMap =
              preferredCourses.stream()
                  .filter(
                      uc -> uc.getPriority().equals(priority) && !uc.getApplicant().hasStudyGroup())
                  .collect(
                      Collectors.groupingBy(
                          PreferredCourse::getCourse,
                          Collectors.mapping(PreferredCourse::getApplicant, Collectors.toList())));

          courseToUserMap.forEach(
              (course, _applicants) -> {
                Set<StudyGroup> matchedGroups = createGroup(_applicants, tag, current);
                results.addAll(matchedGroups);
              });
        });
    return results;
  }

  private Set<StudyGroup> createGroup(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    Set<StudyGroup> matchedGroups = new HashSet<>();

    while (applicants.size() >= 5) {
      // If the group has more than 5 elements, split the group
      // Split the group into 5 elements
      // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] -> [1, 2, 3, 4, 5], [6, 7, 8, 9, 10], [11]
      List<StudyApplicant> subGroup = List.copyOf(applicants.subList(0, 5));

      // Create a group with only 5 elements
      StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, subGroup);
      matchedGroups.add(studyGroup);

      // Remove the elements that have already been added to the group
      applicants.removeAll(subGroup);
    }
    if (applicants.size() >= 3) {
      // If the remaining elements are 3 ~ 4
      // Create a group with 3 ~ 4 elements
      StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, applicants);
      matchedGroups.add(studyGroup);
    }
    return matchedGroups;
  }

  private Set<StudyGroup> matchCourseSecond(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    if (applicants.isEmpty()) {
      return new HashSet<>();
    }

    Set<StudyGroup> matchedGroups = new HashSet<>();

    Map<Course, PriorityQueue<StudyApplicant>> courseToUserByPriority =
        preparePriorityQueueOfUsers(applicants);

    // Make groups with 3 ~ 5 elements
    courseToUserByPriority.forEach(
        (course, queue) -> {
          List<StudyApplicant> group =
              queue.stream()
                  .filter(applicant -> !applicant.hasStudyGroup())
                  .sorted(queue.comparator())
                  .collect(Collectors.toList());
          Set<StudyGroup> groups = createGroup(group, tag, current);
          matchedGroups.addAll(groups);
        });
    return matchedGroups;
  }

  private Map<Course, PriorityQueue<StudyApplicant>> preparePriorityQueueOfUsers(
      List<StudyApplicant> applicants) {
    // Group users by course
    Map<Course, List<PreferredCourse>> courseToUserCourses =
        applicants.stream()
            .flatMap(applicant -> applicant.getPreferredCourses().stream())
            .collect(
                Collectors.groupingBy(
                    PreferredCourse::getCourse,
                    Collectors.mapping(Function.identity(), Collectors.toList())));

    Map<Course, PriorityQueue<StudyApplicant>> courseToUsersByPriority = new HashMap<>();
    courseToUserCourses.forEach(
        (_course, _userCourses) -> {
          _userCourses.sort(Comparator.comparingInt(PreferredCourse::getPriority));
          List<StudyApplicant> sortedForms =
              _userCourses.stream().map(PreferredCourse::getApplicant).toList();

          PriorityQueue<StudyApplicant> userPriorityQueue =
              new PriorityQueue<>(
                  sortedForms.size(), Comparator.comparingInt(sortedForms::indexOf));

          userPriorityQueue.addAll(sortedForms);
          courseToUsersByPriority.put(_course, userPriorityQueue);
        });
    return courseToUsersByPriority;
  }
}
