package edu.handong.csee.histudy.util;

import edu.handong.csee.histudy.domain.AcademicTerm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.TermType;
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

  public Course toCourse(AcademicTerm academicTerm) {
    return Course.builder()
        .name(clazz)
        .code(code)
        .professor(professor)
        .academicTerm(academicTerm)
        .build();
  }

  public AcademicTerm toAcademicTerm() {
    return AcademicTerm.builder().year(year).semester(TermType.parse(semester)).build();
  }
}
