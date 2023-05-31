package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.ApplyForm;
import edu.handong.csee.histudy.controller.form.BuddyForm;
import edu.handong.csee.histudy.domain.Course;
import edu.handong.csee.histudy.controller.form.UserInfo;
import edu.handong.csee.histudy.domain.Friendship;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public List<Friendship> sendRequest(User sender, BuddyForm form) {
        userRepository
                .findAllById(form.getBuddies())
                .forEach(sender::add);

        return sender.getSentRequests();
    }

    @Transactional(readOnly = true)
    public User token2User(String accessToken) {
        return userRepository
                .findUserByAccessToken(accessToken)
                .orElseThrow();
    }

    public void acceptRequest(User user, String sid) {
        user.getReceivedRequests()
                .stream()
                .filter(friendship -> friendship.getSent().getSid().equals(sid))
                .findAny()
                .ifPresent(Friendship::accept);
    }

    public void declineRequest(User user, String sid) {
        user.getReceivedRequests()
                .stream()
                .filter(friendship -> friendship.getSent().getSid().equals(sid))
                .findAny()
                .ifPresent(Friendship::decline);
    }

    public List<User> search(String keyword) {
        return userRepository.findUserByNameOrSidOrEmail(keyword);
    }

    public boolean apply(ApplyForm form, String sub) {
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
        Optional<User> userOr = userRepository.findById(sub);

        userOr.ifPresent(u -> {
            u.add(friends);
            u.select(courses);
        });

        return !courses.isEmpty() && userOr.isPresent();
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
}
