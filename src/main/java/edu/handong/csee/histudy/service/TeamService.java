package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.*;
import edu.handong.csee.histudy.exception.NoCurrentTermFoundException;
import edu.handong.csee.histudy.exception.NoStudyApplicationFound;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.repository.StudyApplicantRepository;
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

              Map<User, StudyApplicant> formMap =
                  applicants.stream()
                      .filter(form -> isFormRelevantToGroup(form, group))
                      .collect(Collectors.toMap(StudyApplicant::getUser, Function.identity()));

              return new TeamDto(group, reports, formMap);
            })
        .toList();
  }

  private boolean isFormRelevantToGroup(StudyApplicant form, StudyGroup group) {
    return form.getStudyGroup().equals(group);
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
        studyGroup.getMembers().stream()
            .map(GroupMember::getUser)
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
        .map(GroupMember::getUser)
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
    // Get users who are not in a team
    AcademicTerm current =
        academicTermRepository.findCurrentSemester().orElseThrow(NoCurrentTermFoundException::new);
    List<StudyApplicant> applicants = studyApplicantRepository.findUnassignedApplicants(current);

    int latestGroupTag = studyGroupRepository.countMaxTag(current).orElse(0);
    AtomicInteger tag = new AtomicInteger(latestGroupTag + 1);

    // First matching
    matchFriendFirst(applicants, tag, current);

    // Remove users who have already been matched
    applicants.removeIf(StudyApplicant::isMarkedAsGrouped);

    // Second matching
    matchCourseFirst(applicants, tag, current);

    // Remove users who have already been matched
    applicants.removeIf(StudyApplicant::isMarkedAsGrouped);

    // Third matching
    matchCourseSecond(applicants, tag, current);
  }

  public List<StudyGroup> matchFriendFirst(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    // First matching
    // Make teams with friends
    return applicants.stream()
        .map(StudyApplicant::getPartnerRequests)
        .flatMap(Collection::stream)
        .filter(StudyPartnerRequest::isAccepted)
        .map(
            partnerRequest -> {
              StudyApplicant receiver =
                  studyApplicantRepository
                      .findByUserAndTerm(partnerRequest.getReceiver(), current)
                      .orElseThrow(NoStudyApplicationFound::new);
              return StudyGroup.of(tag, current, partnerRequest.getSender(), receiver);
            })
        .distinct()
        .toList();
  }

  public List<StudyGroup> matchCourseFirst(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    List<StudyGroup> results = new ArrayList<>();

    List<PreferredCourse> preferredCourses =
        applicants.stream()
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
                      uc ->
                          uc.getPriority().equals(priority)
                              && uc.getApplicant().isNotMarkedAsGrouped())
                  .collect(
                      Collectors.groupingBy(
                          PreferredCourse::getCourse,
                          Collectors.mapping(PreferredCourse::getApplicant, Collectors.toList())));

          courseToUserMap.forEach(
              (course, applicant) -> {
                List<StudyGroup> matchedGroupList = createGroup(applicant, tag, current);
                results.addAll(matchedGroupList);
              });
        });
    return results;
  }

  private List<StudyGroup> createGroup(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    List<StudyGroup> matchedGroupList = new ArrayList<>();

    while (applicants.size() >= 5) {
      // If the group has more than 5 elements, split the group
      // Split the group into 5 elements
      // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] -> [1, 2, 3, 4, 5], [6, 7, 8, 9, 10], [11]
      List<StudyApplicant> subGroup = List.copyOf(applicants.subList(0, 5));

      // Create a group with only 5 elements
      StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, subGroup);
      matchedGroupList.add(studyGroup);

      // Remove the elements that have already been added to the group
      applicants.removeAll(subGroup);
    }
    if (applicants.size() >= 3) {
      // If the remaining elements are 3 ~ 4
      // Create a group with 3 ~ 4 elements
      StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, applicants);
      matchedGroupList.add(studyGroup);
    }
    return matchedGroupList;
  }

  private void matchCourseSecond(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {

    Map<Course, PriorityQueue<StudyApplicant>> courseToUserByPriority =
        preparePriorityQueueOfUsers(applicants);

    // Make groups with 3 ~ 5 elements
    courseToUserByPriority.forEach(
        (course, queue) -> {
          List<StudyApplicant> group =
              queue.stream()
                  .filter(StudyApplicant::isNotMarkedAsGrouped)
                  .sorted(queue.comparator())
                  .collect(Collectors.toList());
          createGroup(group, tag, current);
        });
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
