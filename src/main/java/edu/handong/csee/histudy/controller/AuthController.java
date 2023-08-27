package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.TokenForm;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.jwt.GrantType;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.jwt.TokenInfo;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "인증 API")
@CrossOrigin(
        origins = "${custom.origin.allowed}",
        allowedHeaders = "POST, GET, DELETE, PATCH, OPTIONS",
        allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(summary = "로그인")
    @GetMapping("/login")
    public ResponseEntity<UserDto.UserLogin> login(@RequestParam String sub) {
        Optional<User> userOr = userService.isPresent(sub);

        if (userOr.isPresent()) {
            User user = userOr.get();
            JwtPair tokens = jwtService.issueToken(user.getEmail(), user.getName());

            return ResponseEntity.ok(
                    UserDto.UserLogin.builder()
                            .isRegistered(true)
                            .tokenType("Bearer ")
                            .tokens(tokens)
                            .build());
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(UserDto.UserLogin.builder()
                        .isRegistered(false)
                        .build());
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/token")
    public ResponseEntity<TokenInfo> issueAccessToken(@RequestBody TokenForm tokenForm) {
        Optional<String> refreshTokenOr = Optional.ofNullable(tokenForm.getRefreshToken());
        Optional<Claims> claimsOr = jwtService.validate(refreshTokenOr);

        if (refreshTokenOr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        } else if (claimsOr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        Claims claims = claimsOr.get();
        String email = claims.getSubject();
        String name = claims.get("name", String.class);
        String accessToken = jwtService.issueToken(email, name, GrantType.ACCESS_TOKEN);

        return ResponseEntity.ok(new TokenInfo(GrantType.ACCESS_TOKEN, accessToken));
    }
}
