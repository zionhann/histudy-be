package edu.handong.csee.histudy.domain;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Course extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CsvBindByName
    private String code;
    @CsvBindByName(column = "class")
    private String name;
    @CsvBindByName
    private String professor;
    @CsvBindByName(column = "year")
    private int courseYear;
    @CsvBindByName
    private int semester;

    @Builder
    public Course(String name, String code, String professor, int courseYear, int semester) {
        this.name = name;
        this.code = code;
        this.professor = professor;
        this.courseYear = courseYear;
        this.semester = semester;
    }
}
