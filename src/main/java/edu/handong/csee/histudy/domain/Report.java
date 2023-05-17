package edu.handong.csee.histudy.domain;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
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

    @Nonnull
    private String title;
    private String content;
    @Nonnull
    private LocalTime startTime;
    @Nonnull
    private LocalTime endTime;
    private long totalMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<Participates> participants = new ArrayList<>();

    @Builder
    public Report(String title,
                  String content,
                  LocalTime startTime,
                  LocalTime endTime,
                  Group group,
                  List<User> participants) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;

        totalMinutes = Duration.between(startTime, endTime).toMinutes();
        writtenBy(group);
        addParticipants(participants);
        group.increase(totalMinutes);
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
