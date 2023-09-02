package edu.handong.csee.histudy.domain;

import com.opencsv.bean.CsvBindByName;
import edu.handong.csee.histudy.dto.CourseIdNameDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    @CsvBindByName(column = "year")
    private int courseYear;
    @CsvBindByName
    private int semester;

    @OneToMany(mappedBy = "course", orphanRemoval = true)
    private List<UserCourse> userCourses = new ArrayList<>();

//    @OneToMany(mappedBy = "course", orphanRemoval = true)
//    private List<ReportCourse> reportCourses = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<GroupCourse> groupCourses = new ArrayList<>();

    @Builder
    public Course(String name, String code, String professor, int courseYear, int semester) {
        this.name = name;
        this.code = code;
        this.professor = professor;
        this.courseYear = courseYear;
        this.semester = semester;
    }

    public CourseIdNameDto toIdNameDto() {
        CourseIdNameDto dto = new CourseIdNameDto();
        dto.setId(this.id);
        dto.setName(this.name);
        return dto;
    }
}
