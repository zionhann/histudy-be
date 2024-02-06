package edu.handong.csee.histudy.util;

import edu.handong.csee.histudy.domain.Course;
import lombok.Builder;
import org.apache.commons.csv.CSVRecord;

@Builder
public class CourseCSV {
  private String clazz;
  private String code;
  private String professor;
  private int year;
  private int semester;

  public static CourseCSV of(CSVRecord record) {
    return CourseCSV.builder()
        .clazz(record.get("class"))
        .code(record.get("code"))
        .professor(record.get("professor"))
        .year(Integer.parseInt(record.get("year")))
        .semester(Integer.parseInt(record.get("semester")))
        .build();
  }

  public Course toEntity() {
    return Course.builder()
        .name(clazz)
        .code(code)
        .professor(professor)
        .courseYear(year)
        .semester(semester)
        .build();
  }
}
