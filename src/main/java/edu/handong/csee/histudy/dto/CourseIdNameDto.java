package edu.handong.csee.histudy.dto;

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
}
