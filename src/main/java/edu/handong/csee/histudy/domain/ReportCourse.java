package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCourse extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupReport groupReport;
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupCourse groupCourse;

    @Builder
    public ReportCourse(GroupReport report, GroupCourse groupCourse) {
        this.groupCourse = groupCourse;
        this.groupReport = report;
    }
}
