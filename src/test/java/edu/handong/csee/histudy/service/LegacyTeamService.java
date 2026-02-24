package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * Legacy implementation of TeamService from commit 83000f2
 * Used for performance comparison testing
 */
public class LegacyTeamService {
  public LegacyTeamService() {}

  public Set<StudyGroup> matchFriendFirst(
      List<StudyApplicant> applicants, AtomicInteger tag, AcademicTerm current) {
    if (applicants.isEmpty()) {
      return new HashSet<>();
    }

    return applicants.stream()
        .filter(applicant -> !applicant.hasStudyGroup())
        .map(StudyApplicant::getPartnerRequests)
        .flatMap(Collection::stream)
        .filter(StudyPartnerRequest::isAccepted)
        // Keep original legacy behavior for fair algorithm comparison.
        .map(
            partnerRequest -> {
              StudyApplicant receiver = applicants.stream()
                  .filter(a -> a.getUser().equals(partnerRequest.getReceiver()))
                  .findFirst()
                  .orElse(null);
              if (receiver == null) return null;
              return StudyGroup.of(tag.getAndIncrement(), current, List.of(partnerRequest.getSender(), receiver));
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
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
    List<StudyApplicant> mutableApplicants = new ArrayList<>(applicants);

    while (mutableApplicants.size() >= 5) {
      // If the group has more than 5 elements, split the group
      // Split the group into 5 elements
      // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] -> [1, 2, 3, 4, 5], [6, 7, 8, 9, 10], [11]
      List<StudyApplicant> subGroup = List.copyOf(mutableApplicants.subList(0, 5));

      // Create a group with only 5 elements
      StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, subGroup);
      matchedGroups.add(studyGroup);

      // Remove the elements that have already been added to the group
      mutableApplicants.removeAll(subGroup);
    }
    if (mutableApplicants.size() >= 3) {
      // If the remaining elements are 3 ~ 4
      // Create a group with 3 ~ 4 elements
      StudyGroup studyGroup = StudyGroup.of(tag.getAndIncrement(), current, mutableApplicants);
      matchedGroups.add(studyGroup);
    }
    return matchedGroups;
  }

  public Set<StudyGroup> matchCourseSecond(
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
