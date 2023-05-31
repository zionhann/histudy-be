package edu.handong.csee.histudy.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import edu.handong.csee.histudy.domain.Choice;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.CourseIdDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public String uploadFile(MultipartFile file) throws IOException {
        BOMInputStream bomInputStream = new BOMInputStream(file.getInputStream());
        if(bomInputStream.hasBOM()) {
            try(Reader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)) {
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
        else {
            try(Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
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
    public List<CourseDto> getCourses() {
        return courseRepository
                .findAll()
                .stream()
                .map(Course::toDto)
                .collect(Collectors.toList());
    }
    public List<CourseDto> getTeamCourses(String accessToken) {
        User user = userRepository.findUserByAccessToken(accessToken).orElseThrow();
        Team team = user.getTeam();
        List<User> users = team.getUsers();
        Set<Course> courses = users.stream().flatMap(u -> u.getChoices().stream().map(Choice::getCourse)).collect(Collectors.toSet());
        return courses.stream().map(Course::toDto).collect(Collectors.toList());
    }

    public int deleteCourse(CourseIdDto dto) {
        if(courseRepository.existsById(dto.getId())) {
            courseRepository.deleteById(dto.getId());
            return 1;
        }
        return 0;
    }
}
