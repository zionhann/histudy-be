package edu.handong.csee.histudy.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyFormV2 {

    @Schema(description = "List of friend added to apply form", type = "array", example = "[1, 2]")
    private List<Long> friendIds;

    @Schema(description = "List of course added to apply form", type = "array", example = "[1, 2]")
    private List<Long> courseIds;
}
