package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseIdNameDto {

    @Schema(description = "Course ID", example = "1", type = "number")
    private long id;

    @Schema(description = "Course Name", example = "Java Programming")
    private String name;

    public CourseIdNameDto(Course course) {
        this.id = course.getId();
        this.name = course.getName();
    }
}
