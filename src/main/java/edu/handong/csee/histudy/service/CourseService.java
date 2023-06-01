package edu.handong.csee.histudy.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import edu.handong.csee.histudy.domain.Choice;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.CourseIdDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public String uploadFile(MultipartFile file) throws IOException {
        BOMInputStream bomInputStream = new BOMInputStream(file.getInputStream());
        if (bomInputStream.hasBOM()) {
            try (Reader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)) {
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
        } else {
            try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
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
        }
        return "SUCCESS";
    }

    public List<CourseDto.Info> getCourses() {
        return courseRepository
                .findAll()
                .stream()
                .map(CourseDto.Info::new)
                .toList();
    }

    public List<CourseDto.Info> search(String keyword) {
        return courseRepository.findAllByNameContainingIgnoreCase(keyword)
                .stream()
                .map(CourseDto.Info::new)
                .toList();
    }

    public List<CourseDto.Info> getTeamCourses(String email) {
        List<Long> courseIds = userRepository.findUserByEmail(email).stream()
                .map(User::getTeam)
                .flatMap(t -> t.getUsers().stream())
                .flatMap(u -> u.getChoices().stream())
                .map(Choice::getCourse)
                .collect(Collectors.groupingBy(Course::getId, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() >= 2)
                .map(Map.Entry::getKey)
                .toList();

        return courseRepository.findAllById(courseIds)
                .stream()
                .map(CourseDto.Info::new)
                .toList();
    }

    public int deleteCourse(CourseIdDto dto) {
        if (courseRepository.existsById(dto.getId())) {
            courseRepository.deleteById(dto.getId());
            return 1;
        }
        return 0;
    }
}
