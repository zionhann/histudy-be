package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer tag;
    private long totalMinutes;

    @OneToMany(mappedBy = "team")
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Enrollment> enrolls = new ArrayList<>();

    public Team(Integer tag) {
        this.tag = tag;
    }

    public void increase(long totalMinutes) {
        this.totalMinutes += totalMinutes;
    }

    public void select(List<Course> courses) {
        if (!enrolls.isEmpty()) {
            this.enrolls.clear();
        }
        courses
                .forEach(course -> {
                    Enrollment enrollment = new Enrollment(this, course);
                    enrolls.add(enrollment);
                    course.getEnrolls().add(enrollment);
                });
    }

    @PreRemove
    void preRemove() {
        this.users.forEach(User::removeTeam);
        this.users.clear();
    }

    public void update(long newTotalMinutes, long oldTotalMinutes) {
        this.totalMinutes += newTotalMinutes - oldTotalMinutes;
    }

    private List<Course> commonCourses() {
        Map<Course, Long> courseCountMap = users.stream()
                .flatMap(u -> u.getChoices().stream())
                .map(Choice::getCourse)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return courseCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void enroll(List<User> group) {
        if (!enrolls.isEmpty()) {
            this.enrolls
                    .forEach(e -> e.getCourse()
                            .getEnrolls()
                            .remove(e));
            this.enrolls.clear();
        }
        group.forEach(u -> u.belongTo(this));
        commonCourses()
                .forEach(course -> {
                    Enrollment enrollment = new Enrollment(this, course);
                    enrolls.add(enrollment);
                    course.getEnrolls().add(enrollment);
                });
    }
}
