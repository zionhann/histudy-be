package edu.handong.csee.histudy.domain;

import com.opencsv.bean.CsvBindByName;
import edu.handong.csee.histudy.dto.CourseDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CsvBindByName
    private String code;
    @CsvBindByName(column = "class")
    private String name;
    @CsvBindByName
    private String professor;
    @CsvBindByName
    private int year;
    @CsvBindByName
    private int semester;

    @Builder
    public Course(String name, String code, String professor, int year, int semester) {
        this.name = name;
        this.code = code;
        this.professor = professor;
        this.year = year;
        this.semester = semester;
    }
    public CourseDto toDto() {
        CourseDto dto = new CourseDto();
        dto.setName(this.name);
        dto.setCode(this.code);
        dto.setProfessor(this.professor);
        dto.setYear(this.year);
        dto.setSemester(this.semester);

        return dto;
    }
}
