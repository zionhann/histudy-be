package edu.handong.csee.histudy.util;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Course;
import lombok.Builder;
import org.apache.commons.csv.CSVRecord;

@Builder
public class CourseCSV {
  private String title;
  private String code;
  private String professor;

  public static CourseCSV of(CSVRecord record) {
    return CourseCSV.builder()
        .title(record.get("title"))
        .code(record.get("code"))
        .professor(record.get("prof"))
        .build();
  }

  public Course toCourse(AcademicTerm academicTerm) {
    return Course.builder()
        .name(title)
        .code(code)
        .professor(professor)
        .academicTerm(academicTerm)
        .build();
  }
}
