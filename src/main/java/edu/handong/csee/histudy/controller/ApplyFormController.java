package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class ApplyFormController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApplyFormDto> applyForStudy(@RequestBody ApplyForm form,
                                                      @RequestAttribute Claims claims) {
        return userService.apply(form, claims.getSubject())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
}
