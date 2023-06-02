package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
        if (!courses.isEmpty()) {
            this.enrolls.clear();
        }
        List<Enrollment> targetCourses = courses
                .stream()
                .map(course -> new Enrollment(this, course))
                .toList();
        enrolls.addAll(targetCourses);
    }

    @PreRemove
    void preRemove() {
        this.users.forEach(User::removeTeam);
        this.users.clear();
    }

    public void update(long newTotalMinutes, long oldTotalMinutes) {
        this.totalMinutes += newTotalMinutes - oldTotalMinutes;
    }
}
