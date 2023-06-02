package edu.handong.csee.histudy.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfo {

    @Schema(description = "User ID", example = "1234567890")
    private String sub;

    @Schema(description = "User Name", example = "John Doe")
    private String name;

    @Schema(description = "User Email", example = "jd@example.com")
    private String email;

    @Schema(description = "User Student ID", example = "21800111")
    private String sid; // Student ID
}
