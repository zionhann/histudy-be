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
    List<StudyGroup> friendGroups = groupByFriends(allApplicants, tag, current);
    List<StudyGroup> allMatchedGroups = new ArrayList<>(friendGroups);

    // Second matching - course-based with priority (remaining unassigned applicants)
    List<StudyGroup> courseFirstGroups = groupByCoursePreference(allApplicants, tag, current);
    allMatchedGroups.addAll(courseFirstGroups);

    if (!allMatchedGroups.isEmpty()) {
      studyGroupRepository.saveAll(allMatchedGroups);
    }
  }

  protected List<StudyGroup> groupByFriends(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    if (applicants.isEmpty()) {
      return new ArrayList<>();
    }
    Map<StudyApplicant, List<StudyApplicant>> friendshipMap = buildFriendshipMap(applicants);
    int minGroupSize = 2;

    return new DFS<>(friendshipMap, minGroupSize)
        .execute().stream()
            .map(friends -> StudyGroup.of(tag.getAndIncrement(), current, friends))
            .toList();
  }

  private Map<StudyApplicant, List<StudyApplicant>> buildFriendshipMap(
      List<StudyApplicant> applicants) {
    Map<User, StudyApplicant> userToApplicant =
        applicants.stream().collect(Collectors.toMap(StudyApplicant::getUser, Function.identity()));

    return applicants.stream()
        .filter(applicant -> !applicant.hasStudyGroup())
        .flatMap(applicant -> applicant.getPartnerRequests().stream())
        .filter(StudyPartnerRequest::isAccepted)
        .filter(request -> userToApplicant.containsKey(request.getReceiver()))
        .collect(
            Collectors.groupingBy(
                StudyPartnerRequest::getSender,
                Collectors.mapping(
                    r -> userToApplicant.get(r.getReceiver()), Collectors.toList())));
  }

  protected List<StudyGroup> groupByCoursePreference(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    if (applicants.isEmpty()) {
      return new ArrayList<>();
    }
    List<PreferredCourse> coursePreferences =
        applicants.stream()
            .filter(a -> !a.hasStudyGroup())
            .flatMap(a -> a.getPreferredCourses().stream())
            .sorted(
                Comparator.comparingInt(PreferredCourse::getPriority)
                    .thenComparing(p -> p.getCourse().getCourseId()))
            .toList();

    Map<Priority, Map<Course, List<StudyApplicant>>> priorityCourseMap = new LinkedHashMap<>();

    coursePreferences.forEach(
        pc -> {
          Priority priority = Priority.of(pc.getPriority());
          Course course = pc.getCourse();

          priorityCourseMap
              .computeIfAbsent(priority, p -> new LinkedHashMap<>())
              .computeIfAbsent(course, c -> new ArrayList<>())
              .add(pc.getApplicant());
        });

    int minGroupSize = 3;
    int maxGroupSize = 5;

    return priorityCourseMap.values().stream()
        .flatMap(courseMap -> courseMap.values().stream())
        .map(_applicants -> _applicants.stream().filter(a -> !a.hasStudyGroup()).toList())
        .map(remaining -> groupBySize(remaining, tag, current, 0, minGroupSize, maxGroupSize))
        .flatMap(Collection::stream)
        .toList();
  }

  private List<StudyGroup> groupBySize(
      List<StudyApplicant> applicants,
      AtomicInteger tag,
      AcademicTerm current,
      int startIndex,
      int minSize,
      int maxSize) {
    List<StudyGroup> results = new ArrayList<>();
    int remaining = applicants.size() - startIndex;

    if (remaining < minSize) {
      return results;
    }
    int endIndex = startIndex + Math.min(remaining, maxSize);
    List<StudyApplicant> groupedApplicants = applicants.subList(startIndex, endIndex);

    StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, groupedApplicants);
    results.add(studyGroup);

    List<StudyGroup> studyGroups =
        groupBySize(applicants, tag, current, endIndex, minSize, maxSize);
    results.addAll(studyGroups);

    return results;
  }
}
