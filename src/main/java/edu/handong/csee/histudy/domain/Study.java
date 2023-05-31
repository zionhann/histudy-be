package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Study {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Report report;
    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @Builder
    public Study(Report report, Course course) {
        this.course = course;
        this.report = report;
    }

}
