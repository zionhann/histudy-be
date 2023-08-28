package edu.handong.csee.histudy.domain;

import edu.handong.csee.histudy.dto.UserDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

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
    private Team team;

    @OneToMany(mappedBy = "user")
    private List<Participates> participates = new ArrayList<>();

    @OneToMany(mappedBy = "sent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> friendships = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choice> choices = new ArrayList<>();

    @Builder
    public User(String sub, String sid, String email, String name, Role role) {
        this.sub = sub;
        this.sid = sid;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public void belongTo(Team team) {
        this.team = team;
        team.getUsers().add(this);
        this.role = Role.MEMBER;
    }

    public void add(List<User> users) {
        if (!friendships.isEmpty()) {
            List<Friendship> old = new ArrayList<>();

            for (Friendship friendship : friendships) {
                if (friendship.getSent().equals(this)) old.add(friendship);
            }
            old.forEach(Friendship::disconnect);
        }
        users
                .forEach(u -> u.friendships.stream()
                        .filter(f -> f.getReceived().equals(this))
                        .findFirst()
                        .ifPresentOrElse(
                                Friendship::accept,
                                () -> {
                                    Friendship friendship = new Friendship(this, u);
                                    friendship.connect();
                                }
                        ));
    }

    public void select(List<Course> courses) {
        if (!choices.isEmpty()) {
            this.choices.clear();
        }
        courses
                .forEach(c -> {
                    Choice choice = new Choice(this, c);
                    this.choices.add(choice);
                    c.getChoices().add(choice);
                });
    }

    public void removeTeam() {
        this.team = null;
    }

    public void edit(UserDto.UserEdit dto, Team team) {
        this.sid = dto.getSid();
        this.name = dto.getName();
        this.team = team;
        team.getUsers().add(this);
    }
}
