package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.exception.ForbiddenException;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "스터디 신청 API")
@SecurityRequirement(name = "USER")
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class ApplyFormController {

    private final UserService userService;

    @Operation(summary = "스터디 신청")
    @PostMapping
    public ResponseEntity<ApplyFormDto> applyForStudy(
            @RequestBody ApplyForm form,
            @RequestAttribute Claims claims) {
        if (Role.isAuthorized(claims, Role.USER)) {
            return userService.apply(form, claims.getSubject())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        throw new ForbiddenException();
    }
}
