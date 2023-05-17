package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<Participates> participants = new ArrayList<>();

    @Builder
    public Report(String title, String content, LocalTime startTime, LocalTime endTime) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void addParticipants(List<User> users) {
        List<Participates> participates = users.stream()
                .map(user -> new Participates(user, this))
                .toList();
        this.participants.addAll(participates);
    }

    public void writtenBy(Group group) {
        this.group = group;
        group.getReports().add(this);
    }
}
