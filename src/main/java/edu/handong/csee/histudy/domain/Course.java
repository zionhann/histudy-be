package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String code;
    private String professor;
    private int year;
    private int semester;

    @Builder
    public Course(String name, String code, String professor, int year, int semester) {
        this.name = name;
        this.code = code;
        this.professor = professor;
        this.year = year;
        this.semester = semester;
    }
}
