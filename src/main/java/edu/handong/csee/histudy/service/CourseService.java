package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.GroupCourse;
import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.CourseIdDto;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.util.CSVResolver;
import edu.handong.csee.histudy.util.CourseCSV;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  public void readCourseCSV(MultipartFile file) throws IOException {
    CSVResolver resolver = CSVResolver.of(file.getInputStream());
    List<CourseCSV> courseData = resolver.createCourseCSV();
    List<Course> courses = courseData.stream().map(CourseCSV::toEntity).toList();
    courseRepository.saveAll(courses);
  }

  public List<CourseDto.CourseInfo> getCourses() {
    return courseRepository.findAll().stream().map(CourseDto.CourseInfo::new).toList();
  }

  public List<CourseDto.CourseInfo> search(String keyword) {
    return courseRepository.findAllByNameContainingIgnoreCase(keyword).stream()
        .map(CourseDto.CourseInfo::new)
        .toList();
  }

  public List<CourseDto.CourseInfo> getTeamCourses(String email) {
    List<Course> courses =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(UserNotFoundException::new)
            .getStudyGroup()
            .getGroupCourses()
            .stream()
            .map(GroupCourse::getCourse)
            .toList();

    return courses.stream().map(CourseDto.CourseInfo::new).toList();
  }

  public int deleteCourse(CourseIdDto dto) {
    if (courseRepository.existsById(dto.getId())) {
      courseRepository.deleteById(dto.getId());
      return 1;
    }
    return 0;
  }
}
