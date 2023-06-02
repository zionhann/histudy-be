package edu.handong.csee.histudy.controller;

import edu.handong.csee.histudy.controller.form.UserInfo;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.jwt.JwtPair;
import edu.handong.csee.histudy.service.JwtService;
import edu.handong.csee.histudy.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<UserDto.UserLogin> createUser(@RequestBody UserInfo userInfo) {
        if (userService.signUp(userInfo)) {
            JwtPair tokens = jwtService.issueToken(userInfo.getEmail(), userInfo.getName());

            return ResponseEntity.ok(UserDto.UserLogin.builder()
                    .isRegistered(true)
                    .tokenType("Bearer ")
                    .tokens(tokens)
                    .build());
        }
        return ResponseEntity.badRequest().build();
    }

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

    @GetMapping("/me/forms")
    public ResponseEntity<ApplyFormDto> getMyApplicationForm(@RequestAttribute Claims claims) {
        Optional<ApplyFormDto> userInfo = userService.getUserInfo(claims.getSubject());

        return userInfo
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/manageUsers")
    public List<UserDto.UserInfo> userList(@RequestAttribute Claims claims) {
        return userService.getUsers(claims.getSubject());
    }
}
