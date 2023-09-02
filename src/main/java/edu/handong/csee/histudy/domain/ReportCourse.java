package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupReport groupReport;
    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @Builder
    public ReportCourse(GroupReport report, Course course) {
        this.course = course;
        this.groupReport = report;
    }
}
