package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    public Enrollment(Team team, Course course) {
        this.team = team;
        this.course = course;
    }
}
