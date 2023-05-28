package edu.handong.csee.histudy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private String name;
    private String professor;
    private String code;
    private int courseYear;
    private int semester;
}
