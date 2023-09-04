package edu.handong.csee.histudy.algo;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.TeamDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
public class MatchingAlgorithmTests {

    @Autowired
    TeamService teams;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void init() {
        Random random = new Random();
        String[] names = {"김가영", "이민준", "박서연", "최예준", "정지우", "황하윤", "서재민", "윤지윤", "박민서", "한민재"};

        for (int i = 1; i <= 300; i++) {
            String sub = "SUB_" + i;

            // Generate a random SID
            int sidPrefix = random.nextInt(5);  // Randomly selects 0, 1, 2, 3, or 4
            String sid = switch (sidPrefix) {
                case 0 -> "21800";
                case 1 -> "21900";
                case 2 -> "22000";
                case 3 -> "22100";
                case 4 -> "22200";
                default -> "";
            };
            sid += String.format("%03d", i);
            String email = sid + "@test.com";
            String name = names[random.nextInt(names.length)];
            Role role = Role.USER;

            User user = new User(sub, sid, email, name, role);
            userRepository.save(user);
        }
        List<User> users = userRepository.findAll();
        List<Course> courses = courseRepository.saveAll(getCourses());

        for (int i = 0; i < users.size(); i++) {
            User currentUser = users.get(i);
            // generate different random numbers within the range of courses.size()
            // value of random number should be different from each other
            List<Course> userCourses = random.ints(0, courses.size())
                    .distinct()
                    .limit(3)
                    .mapToObj(courses::get)
                    .toList();

            currentUser.selectCourse(userCourses);

            if (random.nextBoolean()) {
                List<User> friends = random.ints(0, users.size())
                        .distinct()
                        .limit(3)
                        .mapToObj(users::get)
                        .toList();
                currentUser.addUser(friends);
            }
        }
    }

    private static List<Course> getCourses() {

        Course course1 = new Course("Introduction to Computer Science", "CS101", "John Smith", 2023, 1);
        Course course2 = new Course("Data Structures", "CS201", "Emily Johnson", 2023, 1);
        Course course3 = new Course("Algorithms", "CS301", "David Lee", 2023, 2);
        Course course4 = new Course("Database Systems", "CS401", "Sarah Thompson", 2023, 2);
        Course course5 = new Course("Operating Systems", "CS501", "Michael Johnson", 2023, 1);
        Course course6 = new Course("Computer Networks", "CS601", "Jennifer Davis", 2023, 2);
        Course course7 = new Course("Artificial Intelligence", "CS701", "Robert Wilson", 2023, 1);
        Course course8 = new Course("Machine Learning", "CS801", "Elizabeth Adams", 2023, 2);
        Course course9 = new Course("Computer Graphics", "CS901", "Daniel Brown", 2023, 1);
        Course course10 = new Course("Software Engineering", "CS1001", "Jessica Martinez", 2023, 2);
        Course course11 = new Course("Web Development", "CS1101", "Thomas Wilson", 2023, 1);
        Course course12 = new Course("Cryptography", "CS1201", "Laura Thompson", 2023, 2);
        Course course13 = new Course("Natural Language Processing", "CS1301", "James Anderson", 2023, 1);
        Course course14 = new Course("Parallel Computing", "CS1401", "Sophia Martin", 2023, 2);
        Course course15 = new Course("Computer Security", "CS1501", "William Davis", 2023, 1);

        List<Course> courseList = new ArrayList<>();
        courseList.add(course1);
        courseList.add(course2);
        courseList.add(course3);
        courseList.add(course4);
        courseList.add(course5);
        courseList.add(course6);
        courseList.add(course7);
        courseList.add(course8);
        courseList.add(course9);
        courseList.add(course10);
        courseList.add(course11);
        courseList.add(course12);
        courseList.add(course13);
        courseList.add(course14);
        courseList.add(course15);
        return courseList;
    }

    /**
     * Enable only when QA testing is needed.
     */
//    @Test
//    @Rollback(value = false)
//    void run() {
//        init();
//    }
    @DisplayName("팀당 인원 수는 3명 이상 6명 미만이다.")
    @Test
    void MatchingAlgorithmTests_13() {
        AtomicInteger tag = new AtomicInteger(1);
        List<User> users = userRepository.findAll();

        List<StudyGroup> studyGroups = this.teams.matchCourseFirst(users, tag);
        List<List<User>> list = studyGroups.stream()
                .map(StudyGroup::getMembers)
                .toList();

        list.forEach(team -> {
            assertThat(team.size()).isGreaterThanOrEqualTo(3);
            assertThat(team.size()).isLessThan(6);
        });
    }

    @DisplayName("MatchingAlgorithmTests_140")
    @Test
    void MatchingAlgorithmTests_140() {
        // Given
        TeamDto.MatchResults results = teams.matchTeam();
        List<User> all = userRepository.findAll();
        AtomicInteger counter = new AtomicInteger(1);

        all
                .stream()
                .filter(usr -> usr.getStudyGroup() != null)
                .sorted(Comparator.comparingInt(usr -> usr.getStudyGroup().getTag()))
                .forEach(usr -> {
                    Integer tag = usr.getStudyGroup().getTag();
                    System.out.println(counter.getAndIncrement() + ". " + usr.getName() + ": " + "Team " + tag);
                });

        System.out.println("all.size() = " + all.size());
        System.out.println("matched Teams = " + results.getMatchedTeams().size());
        System.out.println("results.getUnmatchedUsers().size() = " + results.getUnmatchedUsers().size());

        // When

        // Then
    }

    @DisplayName("MatchingAlgorithmTests_165")
    @Test
    void MatchingAlgorithmTests_165() {
        // Given
        TeamDto.MatchResults results = teams.matchTeam();

        // When
        results
                .getMatchedTeams()
                .stream().sorted(Comparator.comparingInt(TeamDto.TeamMatching::getTag))
                .forEach(team -> {
                    System.out.println("========================================");
                    System.out.println("Team " + team.getTag() + ":");
                    System.out.println("Members:");
                    team.getUsers().forEach(user -> System.out.println(user.getName()));
                    System.out.println("========================================");
                });
        System.out.println("results.getUnmatchedUsers().size() = " + results.getUnmatchedUsers().size());

        // Then
    }

    @DisplayName("MatchingAlgorithmTests_186")
    @Test
    void MatchingAlgorithmTests_186() {
        List<User> all = userRepository.findAll();
        System.out.println("========================================");
        System.out.println("Members:");
        all.forEach(user -> System.out.println(user.getName() +
                "(" + user.getCourseSelections().get(0).getCourse().getName() + ", " +
                user.getCourseSelections().get(1).getCourse().getName() + ", " +
                user.getCourseSelections().get(2).getCourse().getName() + ")"));
        System.out.println("========================================");
    }

    @DisplayName("MatchingAlgorithmTests_199")
    @Test
    void MatchingAlgorithmTests_199() {
        // Given
        teams.matchTeam();
        List<StudyGroup> all = studyGroupRepository.findAll();

        all.stream().sorted(Comparator.comparingInt(StudyGroup::getTag))
                .forEach(team -> {
                    System.out.println("========================================");
                    System.out.println("Team " + team.getTag() + ":");
                    System.out.println("Members:");
                    team.getMembers().forEach(user -> {
                        user.getCourseSelections().sort(Comparator.comparingInt(UserCourse::getPriority));
                        System.out.println(user.getName() +
                                "(" + user.getCourseSelections().get(0).getCourse().getName() + ", " +
                                user.getCourseSelections().get(1).getCourse().getName() + ", " +
                                user.getCourseSelections().get(2).getCourse().getName() + ")");
                    });
                    System.out.println("Friends:");
                    team.getMembers().forEach(u -> u.getSentRequests().stream().filter(Friendship::isAccepted).forEach(friendship ->
                            System.out.println(friendship.getSent().getName() + " -> " + friendship.getReceived().getName())));
                    System.out.println("Common courses:");
                    team.getGroupCourses().forEach(enroll -> System.out.println(enroll.getCourse().getName()));
                    System.out.println("========================================");
                });
    }
}
