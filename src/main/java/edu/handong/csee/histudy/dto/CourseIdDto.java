package edu.handong.csee.histudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseIdDto {

    @Schema(description = "Course ID", example = "1", type = "number")
    private Long id;
}
