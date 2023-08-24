package edu.handong.csee.histudy.algo;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

//    @BeforeEach
    void init() {
        Random random = new Random();
        String[] names = {"김가영", "이민준", "박서연", "최예준", "정지우", "황하윤", "서재민", "윤지윤", "박민서", "한민재"};

        for (int i = 0; i <= 100; i++) {
            String sub = "SUB_" + i;

            // Generate a random SID
            int sidPrefix = random.nextInt(5);  // Randomly selects 0, 1, 2, 3, or 4
            String sid = "";
            switch (sidPrefix) {
                case 0:
                    sid = "21800";
                    break;
                case 1:
                    sid = "21900";
                    break;
                case 2:
                    sid = "22000";
                    break;
                case 3:
                    sid = "22100";
                    break;
                case 4:
                    sid = "22200";
                    break;
            }
            sid += String.format("%03d", random.nextInt(1000));

            // Generate an email with the SID as the prefix
            String emailPrefix = sid;

            // Generate a random email domain
            String emailDomain = "@test.com";

            // Generate a random name
            String name = names[random.nextInt(names.length)];

            // Combine the email prefix and domain
            String email = emailPrefix + emailDomain;

            Role role = Role.USER;

            User user = new User(sub, sid, email, name, role);
            userRepository.save(user);
        }

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

        List<User> users = userRepository.findAll();
        List<Course> courses = courseRepository.saveAll(courseList);


        for (int i = 0; i < users.size(); i++) {
            User currentUser = users.get(i);

            currentUser.select(List.of(courses.get(random.nextInt(courses.size())), courses.get(random.nextInt(courses.size())), courses.get(random.nextInt(courses.size()))));

            if (random.nextBoolean()) {
                currentUser.add(List.of(users.get(random.nextInt(users.size())), users.get(random.nextInt(users.size())), users.get(random.nextInt(users.size()))));
            }
        }
        printUsers();
    }

    @Test
    @Rollback(value = false)
    void run() {
        init();
    }

    @DisplayName("팀당 인원 수는 3명 이상 6명 미만이다.")
    @Test
    void MatchingAlgorithmTests_13() {
        AtomicInteger tag = new AtomicInteger(1);
        List<User> users = userRepository.findAll();

        List<Team> teams = this.teams.matchCourseFirst(users, tag);
        List<List<User>> list = teams.stream()
                .map(Team::getUsers)
                .toList();

        list.forEach(team -> {
            assertThat(team.size()).isGreaterThanOrEqualTo(3);
            assertThat(team.size()).isLessThan(6);
        });
    }

    void printUsers() {
        List<User> all = userRepository.findAll();
        System.out.println("========================================");
        System.out.println("Members:");
        all.forEach(user -> System.out.println(user.getName() +
                "(" + user.getChoices().get(0).getCourse().getName() + ", " +
                user.getChoices().get(1).getCourse().getName() + ", " +
                user.getChoices().get(2).getCourse().getName() + ")"));
        System.out.println("========================================");
    }

    void printUsers(List<Team> teams, String message) {
        teams.forEach(team -> {
            System.out.println("========================================");
            System.out.println(message + " " + team.getTag() + ":");
            System.out.println("Members:");
            team.getUsers().forEach(user -> {
                System.out.println(user.getName() +
                        "(" + user.getChoices().get(0).getCourse().getName() + ", " +
                        user.getChoices().get(1).getCourse().getName() + ", " +
                        user.getChoices().get(2).getCourse().getName() + ")");
            });
            System.out.println("Friends:");
            team.getUsers().forEach(u -> u.getFriendships().stream().filter(Friendship::isAccepted).forEach(friendship ->
                    System.out.println(friendship.getSent().getName() + " -> " + friendship.getReceived().getName())));
            System.out.println("Common courses:");
            team.getEnrolls().forEach(enroll -> System.out.println(enroll.getCourse().getName()));
            System.out.println("========================================");
        });
    }
}
