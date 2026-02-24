package edu.handong.csee.histudy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.GroupCourse;
import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.StudyPartnerRequest;
import edu.handong.csee.histudy.domain.TermType;
import edu.handong.csee.histudy.domain.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
class TeamServiceMatchingAlgorithmTest extends TeamServiceTestSupport {

  @Test
  void 친구기반매칭_다양한친구관계() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      courses.add(new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term));
    }
    courseRepository.saveAll(courses);

    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 20; i++) {
      users.add(
          User.builder()
              .sub("user" + i)
              .sid("225" + String.format("%05d", i))
              .email("user" + i + "@test.com")
              .name("Student " + i)
              .build());
    }
    userRepository.saveAll(users);

    List<StudyApplicant> applicants = new ArrayList<>();
    applicants.add(
        StudyApplicant.of(
            term, users.get(0), List.of(users.get(1)), List.of(courses.get(0), courses.get(1))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(1), List.of(users.get(0)), List.of(courses.get(0), courses.get(2))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(2), List.of(users.get(3)), List.of(courses.get(1), courses.get(2))));
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(3),
            List.of(users.get(2), users.get(4)),
            List.of(courses.get(1), courses.get(3))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(4), List.of(users.get(3)), List.of(courses.get(2), courses.get(3))));
    applicants.add(
        StudyApplicant.of(
            term, users.get(5), List.of(users.get(6), users.get(7)), List.of(courses.get(4))));
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(6),
            List.of(users.get(5), users.get(8)),
            List.of(courses.get(4), courses.get(5))));
    applicants.add(
        StudyApplicant.of(
            term,
            users.get(7),
            List.of(users.get(5), users.get(9)),
            List.of(courses.get(5), courses.get(6))));
    applicants.add(StudyApplicant.of(term, users.get(8), List.of(users.get(6)), List.of(courses.get(6))));
    applicants.add(StudyApplicant.of(term, users.get(9), List.of(users.get(7)), List.of(courses.get(7))));
    for (int i = 10; i < 15; i++) {
      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(i % 10))));
    }
    studyApplicantRepository.saveAll(applicants);
    applicants.forEach(applicant -> applicant.getPartnerRequests().forEach(StudyPartnerRequest::accept));

    teamService.matchTeam();

    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);
    assertTrue(allGroups.size() >= 3, "Should have at least 3 friend groups");
    assertTrue(
        allGroups.stream()
            .anyMatch(group -> group.getMembers().size() == 2 && containsUsers(group, users.get(0), users.get(1))),
        "Should have User1-User2 friend pair");
    assertTrue(
        allGroups.stream()
            .anyMatch(
                group -> group.getMembers().size() == 3 && containsUsers(group, users.get(2), users.get(3), users.get(4))),
        "Should have User3-User4-User5 friend group");
    assertTrue(
        allGroups.stream()
            .anyMatch(
                group ->
                    group.getMembers().size() == 5
                        && containsUsers(group, users.get(5), users.get(6), users.get(7), users.get(8), users.get(9))),
        "Should have User6-User10 large friend group");

    Set<User> friendUsers = new HashSet<>(users.subList(0, 10));
    long friendGroupMembers =
        allGroups.stream()
            .map(StudyGroup::getMembers)
            .flatMap(List::stream)
            .map(StudyApplicant::getUser)
            .filter(friendUsers::contains)
            .count();
    assertEquals(10, friendGroupMembers, "Friend groups should contain exactly 10 people");
  }

  @Test
  void 과목선호도기반매칭_우선순위반영() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    List<Course> courses = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      courses.add(new Course("Course " + i, "CRS" + String.format("%03d", i), "Prof " + i, term));
    }
    courseRepository.saveAll(courses);

    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      users.add(
          User.builder()
              .sub("user" + i)
              .sid("225" + String.format("%05d", i))
              .email("user" + i + "@test.com")
              .name("Student " + i)
              .build());
    }
    userRepository.saveAll(users);

    List<StudyApplicant> applicants = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      applicants.add(
          StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(0), courses.get(1), courses.get(2))));
    }
    for (int i = 8; i < 11; i++) {
      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(1), courses.get(0))));
    }
    for (int i = 11; i < 14; i++) {
      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(2), courses.get(1))));
    }
    for (int i = 14; i < 18; i++) {
      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), List.of(courses.get(2), courses.get(3))));
    }

    Random random = new Random(42);
    for (int i = 18; i < 30; i++) {
      List<Course> preferences = new ArrayList<>();
      preferences.add(courses.get(i % 10));
      if (random.nextDouble() > 0.3) preferences.add(courses.get((i + 1) % 10));
      if (random.nextDouble() > 0.6) preferences.add(courses.get((i + 2) % 10));
      applicants.add(StudyApplicant.of(term, users.get(i), List.of(), preferences));
    }
    studyApplicantRepository.saveAll(applicants);

    teamService.matchTeam();

    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);
    long mathGroups =
        allGroups.stream()
            .filter(group -> group.getCourses().stream().anyMatch(gc -> gc.getCourse().equals(courses.get(0))))
            .count();
    assertTrue(mathGroups >= 1, "Should have Course 1 groups");

    long largeGroups = allGroups.stream().filter(group -> group.getMembers().size() == 5).count();
    long mediumGroups = allGroups.stream().filter(group -> group.getMembers().size() == 4).count();
    long smallGroups = allGroups.stream().filter(group -> group.getMembers().size() == 3).count();
    assertTrue(largeGroups >= 1, "Should prioritize creating 5-person groups");
    assertTrue((largeGroups * 5 + mediumGroups * 4 + smallGroups * 3) >= 15, "Should assign most students to groups");

    boolean hasInvalidGroups = allGroups.stream().anyMatch(group -> group.getMembers().size() < 2);
    assertFalse(hasInvalidGroups, "Should not create groups smaller than 2 people");
  }

  @Test
  void 대규모종합매칭_100명학생_10과목_다양한선호도와친구요청() {
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    List<Course> courses = new ArrayList<>();
    String[] courseNames = {
      "Mathematics", "Physics", "Chemistry", "Computer Science", "Biology",
      "History", "Literature", "Psychology", "Economics", "Philosophy"
    };
    for (int i = 0; i < 10; i++) {
      courses.add(
          new Course(courseNames[i], "CRS" + String.format("%03d", i + 1), "Professor " + (i + 1), term));
    }
    courseRepository.saveAll(courses);

    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      users.add(
          User.builder()
              .sub("student" + i)
              .sid("225" + String.format("%05d", i))
              .email("student" + i + "@university.edu")
              .name("Student " + i)
              .build());
    }
    userRepository.saveAll(users);

    List<StudyApplicant> applicants = new ArrayList<>();
    Random random = new Random(12345);
    for (int i = 0; i < 100; i++) {
      User currentUser = users.get(i);
      List<Course> coursePreferences = new ArrayList<>();
      int numPreferences = random.nextInt(3) + 1;
      Set<Integer> selectedCourseIndices = new HashSet<>();
      for (int j = 0; j < numPreferences; j++) {
        int courseIndex;
        do {
          courseIndex = random.nextInt(10);
        } while (selectedCourseIndices.contains(courseIndex));
        selectedCourseIndices.add(courseIndex);
        coursePreferences.add(courses.get(courseIndex));
      }

      List<User> friendRequests = new ArrayList<>();
      if (random.nextDouble() > 0.3) {
        int numFriends = random.nextInt(3) + 1;
        Set<Integer> selectedFriendIndices = new HashSet<>();
        selectedFriendIndices.add(i);
        for (int j = 0; j < numFriends && selectedFriendIndices.size() < Math.min(100, numFriends + 1); j++) {
          int friendIndex;
          do {
            friendIndex = random.nextInt(100);
          } while (selectedFriendIndices.contains(friendIndex));
          selectedFriendIndices.add(friendIndex);
          friendRequests.add(users.get(friendIndex));
        }
      }
      applicants.add(StudyApplicant.of(term, currentUser, friendRequests, coursePreferences));
    }
    studyApplicantRepository.saveAll(applicants);

    applicants.forEach(
        applicant ->
            applicant
                .getPartnerRequests()
                .forEach(request -> {
                  if (random.nextDouble() > 0.2) {
                    request.accept();
                  }
                }));

    teamService.matchTeam();

    List<StudyGroup> allGroups = studyGroupRepository.findAllByAcademicTerm(term);
    long totalAssignedStudents = allGroups.stream().mapToLong(group -> group.getMembers().size()).sum();
    assertTrue(totalAssignedStudents >= 60, "Should assign at least 60% of students to groups");

    Map<Integer, Long> groupSizeDistribution =
        allGroups.stream().collect(Collectors.groupingBy(group -> group.getMembers().size(), Collectors.counting()));
    long friendGroups = groupSizeDistribution.getOrDefault(2, 0L);
    long size3Groups = groupSizeDistribution.getOrDefault(3, 0L);
    long size4Groups = groupSizeDistribution.getOrDefault(4, 0L);
    long size5Groups = groupSizeDistribution.getOrDefault(5, 0L);

    assertTrue((size5Groups + size4Groups + size3Groups + friendGroups) > 0, "Should create various group sizes");
    assertTrue((size5Groups + size4Groups + size3Groups) > 0, "Should create at least one course-sized group (3-5 members)");

    Map<Course, Long> courseDistribution =
        allGroups.stream()
            .flatMap(group -> group.getCourses().stream())
            .collect(Collectors.groupingBy(GroupCourse::getCourse, Collectors.counting()));
    assertTrue(courseDistribution.size() >= 5, "Should create groups for multiple different courses");

    Set<User> assignedUsers =
        allGroups.stream()
            .flatMap(group -> group.getMembers().stream())
            .map(StudyApplicant::getUser)
            .collect(Collectors.toSet());
    assertEquals(assignedUsers.size(), totalAssignedStudents, "No student should be assigned to multiple groups");
  }
}
