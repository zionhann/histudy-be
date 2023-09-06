package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupCourse extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @OneToMany(mappedBy = "groupCourse")
    private List<ReportCourse> reportCourses = new ArrayList<>();

    public GroupCourse(StudyGroup studyGroup, Course course) {
        this.studyGroup = studyGroup;
        this.course = course;

        studyGroup.getGroupCourses().add(this);
    }

    public boolean isNotInUse() {
        return this.reportCourses.isEmpty();
    }
}
