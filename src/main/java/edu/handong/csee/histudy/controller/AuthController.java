package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/login")
    public ResponseEntity<UserDto.Login> login(@RequestParam String sub) {
        Optional<User> userOr = userService.isPresent(sub);

        if (userOr.isPresent()) {
            User user = userOr.get();
            JwtPair tokens = jwtService.issueToken(user.getEmail(), user.getName());

            return ResponseEntity.ok(
                    UserDto.Login.builder()
                            .isRegistered(true)
                            .tokenType("Bearer ")
                            .tokens(tokens)
                            .build());
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(UserDto.Login.builder()
                        .isRegistered(false)
                        .build());
    }
}
