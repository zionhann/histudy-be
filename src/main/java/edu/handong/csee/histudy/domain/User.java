package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    @Id
    private String id;

    @Column(unique = true)
    private String sid;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String accessToken;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @OneToMany(mappedBy = "user")
    private List<Participates> participates;

    @Builder
    public User(String id, String sid, String email, String name, String accessToken, Role role) {
        this.id = id;
        this.sid = sid;
        this.email = email;
        this.name = name;
        this.accessToken = accessToken;
        this.role = role;
    }

    public void belongTo(Team team) {
        this.team = team;
        team.getUsers().add(this);
    }
}
