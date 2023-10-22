package edu.handong.csee.histudy.controller.form;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportForm {

    @Schema(description = "Report Title", example = "Week 15 Report")
    private String title;

    @Schema(description = "Report Content", example = "This is a report for week 15.")
    private String content;

    @Schema(description = "Total minutes of the report", type = "number", example = "60")
    private Long totalMinutes;

    /**
     * Contains student ID(PK)
     */
    @Schema(description = "Participant ID(PK) of the report", type = "array", example = "[1, 2]")
    private List<Long> participants = new ArrayList<>();

    /**
     * 이미지 URL
     *
     * @see edu.handong.csee.histudy.controller.TeamController#uploadImage(Optional, MultipartFile, Claims)
     */
    @Schema(description = "Image URLs of the report", type = "array", example = "[\"/path/to/image1.png\", \"/path/to/image2.png\"]")
    private List<String> images;

    /**
     * Contains course ID
     */
    @Schema(description = "Course IDs of the report", type = "array", example = "[1, 2]")
    private List<Long> courses = new ArrayList<>();
}
