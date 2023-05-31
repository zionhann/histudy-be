package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/course")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<String> importCourses(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Empty file");
        } else {
            String status = courseService.uploadFile(file);
            if (status.equals("FAILED"))
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed in server");
            else
                return ResponseEntity.ok(status);
        }
    }

    @GetMapping
    public ResponseEntity<CourseDto> getCourses(@RequestParam(name = "search", required = false) String keyword) {
        List<CourseDto.Info> courses = (keyword == null)
                ? courseService.getCourses()
                : courseService.search(keyword);

        return ResponseEntity.ok(new CourseDto(courses));
    }
    @GetMapping("/team")
    public ResponseEntity<List<CourseDto>> getTeamCourses(@RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken) {
        return ResponseEntity.ok(courseService.getTeamCourses(accessToken));
    }
}
