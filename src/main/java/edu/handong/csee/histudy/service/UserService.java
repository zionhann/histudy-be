package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.UserInfo;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.Team;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.MissingEmailException;
import edu.handong.csee.histudy.exception.MissingSubException;
import edu.handong.csee.histudy.exception.UserAlreadyExistsException;
import edu.handong.csee.histudy.exception.UserNotFoundException;
import edu.handong.csee.histudy.repository.ChoiceRepository;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.TeamRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ChoiceRepository choiceRepository;
    private final TeamRepository teamRepository;

    public List<User> search(Optional<String> keyword) {
        if (keyword.isEmpty() || keyword.get().isBlank()) {
            return userRepository.findAll(Sort.by(Sort.Direction.ASC, "sid"));
        }
        return userRepository.findUserByNameOrSidOrEmail(keyword.get());
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

    public void signUp(UserInfo userInfo) {
        userRepository
                .findUserBySub(userInfo.getSub())
                .ifPresentOrElse(
                        __ -> {
                            throw new UserAlreadyExistsException();
                        },
                        () ->
                                userRepository.save(User.builder()
                                        .sid(userInfo.getSid())
                                        .email(userInfo.getEmail())
                                        .name(userInfo.getName())
                                        .sub(userInfo.getSub())
                                        .role(Role.USER)
                                        .build()));
    }

    public User getUser(Optional<String> subOr) {
        String sub = subOr.orElseThrow(MissingSubException::new);
        return userRepository
                .findUserBySub(sub)
                .orElseThrow(UserNotFoundException::new);
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
                .map(UserDto.UserInfo::new)
                .toList();
    }

    public Optional<ApplyFormDto> getUserInfo(String email) {
        return userRepository.findUserByEmail(email)
                .map(ApplyFormDto::new);
    }

    public UserDto.UserMe getUserMe(Optional<String> emailOr) {
        String email = emailOr.orElseThrow(MissingEmailException::new);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return new UserDto.UserMe(user);
    }

    public List<UserDto.UserInfo> getUnmatchedUsers() {
        return getInfoFromUser(userRepository.findUsersByTeamIsNull());
    }

    public UserDto.UserInfo deleteUserForm(String sid) {
        User user = userRepository.findUserBySid(sid).orElseThrow();

        user.getChoices().clear();
        user.add(Collections.emptyList());

        return new UserDto.UserInfo(user);
    }

    public UserDto.UserInfo editUser(UserDto.UserEdit dto) {
        User user = userRepository.findById(dto.getId()).orElseThrow();
        Team team = teamRepository.findByTag(dto.getTeam()).orElseThrow();
        user.edit(dto, team);
        return new UserDto.UserInfo(user);
    }
}
