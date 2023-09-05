package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StudyGroup extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer tag;
    private long totalMinutes;

    @OneToMany(mappedBy = "studyGroup")
    private List<GroupReport> reports = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup")
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL)
    private List<GroupCourse> groupCourses = new ArrayList<>();

    public StudyGroup(Integer tag, List<User> members) {
        this.tag = tag;
        join(members);
    }

    public void increase(long totalMinutes) {
        this.totalMinutes += totalMinutes;
    }

    public void select(List<Course> _courses) {
        if (!groupCourses.isEmpty()) {
            this.groupCourses.clear();
        }
        _courses
                .forEach(course -> {
                    GroupCourse groupCourse = new GroupCourse(this, course);
                    groupCourses.add(groupCourse);
                    course.getGroupCourses().add(groupCourse);
                });
    }

    @PreRemove
    void preRemove() {
        this.members.forEach(User::removeTeam);
        this.members.clear();
    }

    public void update(long newTotalMinutes, long oldTotalMinutes) {
        this.totalMinutes += (newTotalMinutes - oldTotalMinutes);
    }

    private List<Course> getCommonCourses() {
        if (!this.groupCourses.isEmpty()) {
            this.groupCourses.clear();
        }
        Map<Course, Long> courseCountMap = this.members.stream()
                .flatMap(u -> u.getCourseSelections().stream())
                .map(UserCourse::getCourse)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Course> commonCourses = courseCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .sorted(Map.Entry.<Course, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        return (commonCourses.isEmpty())
                ? this.members.stream()
                .map(User::getCourseSelections)
                .flatMap(Collection::stream)
                .map(UserCourse::getCourse)
                .toList()
                : commonCourses;
    }

    protected StudyGroup join(List<User> users) {
        users.forEach(u -> u.belongTo(this));
        getCommonCourses()
                .forEach(course -> {
                    GroupCourse groupCourse = new GroupCourse(this, course);
                    groupCourses.add(groupCourse);
                    course.getGroupCourses().add(groupCourse);
                });
        return this;
    }
}
