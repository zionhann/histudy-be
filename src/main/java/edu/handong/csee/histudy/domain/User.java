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
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @OneToMany(mappedBy = "user")
    private List<Participates> participates;

    @Builder
    public User(String id, String sid, String email, String name, Role role) {
        this.id = id;
        this.sid = sid;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public void belongTo(Group group) {
        this.group = group;
        group.getUsers().add(this);
    }
}
