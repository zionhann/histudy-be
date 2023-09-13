package edu.handong.csee.histudy.domain;

import edu.handong.csee.histudy.dto.UserDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElse;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sub;

    @Column(unique = true)
    private String sid;

    @Column(unique = true)
    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "user")
    private List<ReportUser> reportParticipation = new ArrayList<>();

    @OneToMany(mappedBy = "sent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> sentRequests = new ArrayList<>();

    @OneToMany(mappedBy = "received", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> receivedRequests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourse> courseSelections = new ArrayList<>();

    @Builder
    public User(String sub, String sid, String email, String name, Role role) {
        this.sub = sub;
        this.sid = sid;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public void belongTo(StudyGroup studyGroup) {
        if (isInSameGroup(studyGroup)) {
            return;
        } else if (isAlreadyInOtherGroup(studyGroup)) {
            leaveGroup();
        }
        this.studyGroup = studyGroup;
        studyGroup.getMembers().add(this);
        this.role = Role.MEMBER;
    }

    public void addUser(List<User> users) {
        if (!this.sentRequests.isEmpty()) {
            // Remove all sent requests
            this.sentRequests.forEach(Friendship::removeFromReceivedRequests);
            this.sentRequests.clear();

            // Change all accepted requests to pending
            this.receivedRequests.stream()
                    .filter(Friendship::isAccepted)
                    .forEach(Friendship::unfriend);
        }
        users.forEach(u -> {
            FriendshipStatus status = this.receivedRequests.stream()
                    .filter(req ->
                            req.isSentFrom(u))
                    .findFirst()
                    .map(req -> {
                        req.accept();
                        return FriendshipStatus.ACCEPTED;
                    }).orElse(FriendshipStatus.PENDING);

            Friendship friendship = new Friendship(this, u, status);
            this.sentRequests.add(friendship);
            u.receivedRequests.add(friendship);
        });
    }

    public void selectCourse(List<Course> courses) {
        if (!courseSelections.isEmpty()) {
            this.courseSelections.clear();
        }
        courses.stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                Function.identity(),
                                courses::indexOf
                        )
                )
                .forEach((c, i) -> {
                    UserCourse userCourse = new UserCourse(this, c, i);
                });
    }

    public void removeTeam() {
        this.studyGroup = null;
    }

    public void edit(UserDto.UserEdit dto) {
        this.sid = requireNonNullElse(dto.getSid(), this.sid);
        this.name = requireNonNullElse(dto.getName(), this.name);
    }

    public void resetPreferences() {
        if (this.studyGroup != null) {
            throw new IllegalStateException("그룹에 속해있는 유저는 신청내역을 초기화할 수 없습니다.");
        }
        this.addUser(Collections.emptyList());
        this.selectCourse(Collections.emptyList());
    }

    public boolean isNotInAnyGroup() {
        return this.studyGroup == null;
    }

    public void leaveGroup() {
        if (this.studyGroup == null) {
            return;
        }
        this.studyGroup.getMembers().remove(this);
        this.studyGroup.assignCommonCourses();
        this.studyGroup = null;
        this.role = Role.USER;
    }

    private boolean isAlreadyInOtherGroup(StudyGroup studyGroup) {
        return this.studyGroup != null && !this.studyGroup.equals(studyGroup);
    }

    public boolean isInSameGroup(StudyGroup studyGroup) {
        return this.studyGroup != null && this.studyGroup.equals(studyGroup);
    }

    public String getSidWithMasking() {
        return this.sid.substring(0, 3) + "****" + this.sid.substring(7);
    }
}
