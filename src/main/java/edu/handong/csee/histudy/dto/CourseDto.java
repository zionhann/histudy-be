package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Course;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseDto {

    private List<Info> courses;

    @AllArgsConstructor
    @Getter
    public static class Info {
        private String name;
        private String prof;
        private String code;
        private int year;
        private int semester;

        public Info(Course course) {
            this.name = course.getName();
            this.prof = course.getProfessor();
            this.code = course.getCode();
            this.year = course.getCourseYear();
            this.semester = course.getSemester();
        }
    }
}
