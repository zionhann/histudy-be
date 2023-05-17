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
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer tag;
    private long totalMinutes;

    @OneToMany(mappedBy = "group")
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<User> users = new ArrayList<>();

    public Group(Integer tag) {
        this.tag = tag;
    }

    public void increase(long totalMinutes) {
        this.totalMinutes += totalMinutes;
    }
}
