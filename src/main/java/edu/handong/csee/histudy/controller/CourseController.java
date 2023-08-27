package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.dto.CourseDto;
import edu.handong.csee.histudy.dto.CourseIdDto;
import edu.handong.csee.histudy.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "강의 관리 API")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @SecurityRequirement(name = "Admin")
    @Operation(summary = "강의 목록 업로드")
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

    @SecurityRequirement(name = "Admin")
    @Operation(summary = "강의 삭제")
    @PostMapping("/delete")
    public int deleteCourse(@RequestBody CourseIdDto dto) {
        return courseService.deleteCourse(dto);
    }

    @SecurityRequirements({
            @SecurityRequirement(name = "Admin"),
            @SecurityRequirement(name = "General")
    })
    @Operation(summary = "강의 목록 조회")
    @GetMapping
    public ResponseEntity<CourseDto> getCourses(@RequestParam(name = "search", required = false) String keyword) {
        List<CourseDto.CourseInfo> courses = (keyword == null)
                ? courseService.getCourses()
                : courseService.search(keyword);

        return ResponseEntity.ok(new CourseDto(courses));
    }
}
