package edu.handong.csee.histudy.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public String uploadFile(MultipartFile file) {
        try(Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(),"UTF-8"))) {
            CsvToBean<Course> csvToBean = new CsvToBeanBuilder<Course>(reader)
                    .withType(Course.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            List<Course> courses = csvToBean.parse();
            courseRepository.saveAll(courses);
        } catch (IOException e) {
            return "FAILED";
        }
        return "SUCCESS";
    }
}
