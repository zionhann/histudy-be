package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer tag;
    private long totalMinutes;

    @OneToMany(mappedBy = "team")
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<User> users = new ArrayList<>();

    public Team(Integer tag) {
        this.tag = tag;
    }

    public void increase(long totalMinutes) {
        this.totalMinutes += totalMinutes;
    }

    @PreRemove
    void preRemove() {
        this.users.forEach(User::removeTeam);
        this.users.clear();
    }

    public void update(long newTotalMinutes, long oldTotalMinutes) {
        this.totalMinutes += newTotalMinutes - oldTotalMinutes;
    }
}
