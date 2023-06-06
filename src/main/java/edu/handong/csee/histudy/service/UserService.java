package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.UserInfo;
import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.CourseIdNameDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<User> search(String keyword) {
        return userRepository.findUserByNameOrSidOrEmail(keyword);
    }

    public Optional<ApplyFormDto> apply(ApplyForm form, String email) {
        List<User> friends = form.getFriendIds()
                .stream()
                .map(userRepository::findUserBySid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<Course> courses = form.getCourseIds()
                .stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return userRepository.findUserByEmail(email)
                .map(u -> {
                    u.add(friends);
                    u.select(courses);
                    return new ApplyFormDto(u);
                });
    }

    public boolean signUp(UserInfo userInfo) {
        Optional<User> userOr = userRepository.findById(userInfo.getSub());

        if (userOr.isEmpty()) {
            User user = User.builder()
                    .id(userInfo.getSub())
                    .sid(userInfo.getSid())
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<User> isPresent(String sub) {
        return userRepository.findById(sub);
    }

    public List<UserDto.UserInfo> getUsers(String email) {
        List<User> users = userRepository.findAll();
        return getInfoFromUser(users);
    }

    public List<UserDto.UserInfo> getAppliedUsers() {
        List<User> users = userRepository.findAll()
                .stream()
                .filter(u -> !u.getChoices().isEmpty())
                .toList();
        return getInfoFromUser(users);
    }

    public List<UserDto.UserInfo> getInfoFromUser(List<User> users) {
        return users
                .stream()
                .map(u -> {
                    List<User> friends = new ArrayList<>();
                    friends.addAll(u.getFriendships()
                            .stream()
                            .filter(Friendship::isAccepted)
                            .map(Friendship::getReceived)
                            .toList());
                    friends.addAll(u.getFriendships()
                            .stream()
                            .filter(Friendship::isAccepted)
                            .map(Friendship::getSent)
                            .toList());
                    List<UserDto.UserBasic> buddies = friends.stream().map(f -> UserDto.UserBasic.builder()
                            .id(f.getId())
                            .sid(f.getSid())
                            .name(f.getName())
                            .build()).toList();
                    List<CourseIdNameDto> courses = u.getChoices()
                            .stream()
                            .map(Choice::getCourse)
                            .map(Course::toIdNameDto)
                            .toList();
                    long totalMinutes = u.getParticipates()
                            .stream()
                            .map(Participates::getReport)
                            .mapToLong(Report::getTotalMinutes)
                            .sum();
                    return UserDto.UserInfo.builder()
                            .id(u.getId())
                            .sid(u.getSid())
                            .name(u.getName())
                            .friends(buddies)
                            .courses(courses)
                            .totalMinutes(totalMinutes)
                            .build();
                }).toList();
    }

    public Optional<ApplyFormDto> getUserInfo(String email) {
        return userRepository.findUserByEmail(email)
                .map(ApplyFormDto::new);
    }
}
