package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.UserInfo;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "일반 사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<UserDto.UserLogin> createUser(@RequestBody UserInfo userInfo) {
        if (userService.signUp(userInfo)) {
            JwtPair tokens = jwtService.issueToken(userInfo.getEmail(), userInfo.getName(), Role.USER);

            return ResponseEntity.ok(UserDto.UserLogin.builder()
                    .isRegistered(true)
                    .tokenType("Bearer ")
                    .tokens(tokens)
                    .role(Role.USER.name())
                    .build());
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "유저 검색")
    @GetMapping
    public ResponseEntity<UserDto> searchUser(@RequestParam(name = "search") String keyword) {
        if (keyword == null) {
            return ResponseEntity.badRequest().build();
        }
        List<UserDto.UserMatching> users = userService.search(keyword)
                .stream()
                .filter(u -> u.getRole().equals(Role.USER))
                .map(UserDto.UserMatching::new)
                .toList();

        return ResponseEntity.ok(
                new UserDto(users));
    }

    @Operation(summary = "내 정보 조회")
    @SecurityRequirements({
            @SecurityRequirement(name = "USER"),
            @SecurityRequirement(name = "MEMBER"),
            @SecurityRequirement(name = "ADMIN")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto.UserMe> getMyInfo(@RequestAttribute Claims claims) {
        Optional<UserDto.UserMe> info = userService.getUserMe(claims.getSubject());

        return info
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "스터디 그룹 신청 정보 조회")
    @SecurityRequirement(name = "USER")
    @GetMapping("/me/forms")
    public ResponseEntity<ApplyFormDto> getMyApplicationForm(@RequestAttribute Claims claims) {
        Optional<ApplyFormDto> userInfo = userService.getUserInfo(claims.getSubject());

        return userInfo
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "전체 유저 스터디 신청 정보 조회")
    @SecurityRequirement(name = "ADMIN")
    @GetMapping("/manageUsers")
    public List<UserDto.UserInfo> userList(@RequestAttribute Claims claims) {
        return userService.getUsers(claims.getSubject());
    }
}
