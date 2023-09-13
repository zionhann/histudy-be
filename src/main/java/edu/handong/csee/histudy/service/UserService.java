package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.UserForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ApplyFormDto;
import edu.handong.csee.histudy.dto.UserDto;
import edu.handong.csee.histudy.exception.*;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserCourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final StudyGroupRepository studyGroupRepository;

    public List<User> search(Optional<String> keyword) {
        if (keyword.isEmpty() || keyword.get().isBlank()) {
            return userRepository.findAll(Sort.by(Sort.Direction.ASC, "sid"));
        }
        return userRepository.findUserByNameOrSidOrEmail(keyword.get());
    }

    public ApplyFormDto apply(ApplyForm form, String email) {
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

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        user.addUser(friends);
        user.selectCourse(courses);
        return new ApplyFormDto(user);
    }

    public User apply(
            List<Long> friendsIds,
            List<Long> courseIds,
            String email
    ) {
        List<User> friends = friendsIds
                .stream()
                .map(id ->
                        userRepository.findById(id)
                                .orElseThrow(UserNotFoundException::new))
                .toList();
        List<Course> courses = courseIds
                .stream()
                .map(id ->
                        courseRepository.findById(id)
                                .orElseThrow(CourseNotFoundException::new))
                .toList();

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        user.addUser(friends);
        user.selectCourse(courses);

        return user;
    }

    public void signUp(UserForm userForm) {
        userRepository
                .findUserBySub(userForm.getSub())
                .ifPresentOrElse(
                        __ -> {
                            throw new UserAlreadyExistsException();
                        },
                        () ->
                                userRepository.save(User.builder()
                                        .sid(userForm.getSid())
                                        .email(userForm.getEmail())
                                        .name(userForm.getName())
                                        .sub(userForm.getSub())
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
        List<User> users = userRepository.findAllApplicants();
        return getInfoFromUser(users);
    }

    public List<UserDto.UserInfo> getInfoFromUser(List<User> users) {
        return users
                .stream()
                .map(UserDto.UserInfo::new)
                .toList();
    }

    public User getUserInfo(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserDto.UserMe getUserMe(Optional<String> emailOr) {
        String email = emailOr.orElseThrow(MissingEmailException::new);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return new UserDto.UserMe(user);
    }

    public List<UserDto.UserInfo> getUnmatchedUsers() {
        return getInfoFromUser(userRepository.findAllByStudyGroupIsNull());
    }

    public UserDto.UserInfo deleteUserForm(String sid) {
        User user = userRepository.findUserBySid(sid)
                .orElseThrow(UserNotFoundException::new);
        user.resetPreferences();

        return new UserDto.UserInfo(user);
    }

    public UserDto.UserInfo editUser(UserDto.UserEdit form) {
        User user = userRepository.findById(form.getId())
                .orElseThrow(UserNotFoundException::new);

        Optional.ofNullable(form.getTeam())
                .ifPresentOrElse(
                        tag ->
                                studyGroupRepository
                                        .findByTag(tag)
                                        .orElse(new StudyGroup(tag))
                                        .join(List.of(user)),
                        () -> {
                            user.leaveGroup();
                            studyGroupRepository.deleteEmptyGroup();
                        }
                );
        user.edit(form);
        return new UserDto.UserInfo(user);
    }

    public List<UserDto.UserInfo> getAppliedWithoutGroup() {
        return getInfoFromUser(userRepository.findUnassignedApplicants());
    }
}
