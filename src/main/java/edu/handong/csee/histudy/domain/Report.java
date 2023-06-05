package edu.handong.csee.histudy.domain;

import edu.handong.csee.histudy.controller.form.ReportForm;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Report extends BaseTime{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private long totalMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participates> participants = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Study> studies = new ArrayList<>();

    @Builder
    public Report(String title,
                  String content,
                  long totalMinutes,
                  Team team,
                  List<User> participants,
                  List<String> images,
                  List<Course> courses) {
        this.title = title;
        this.content = content;
        this.totalMinutes = totalMinutes;

        this.writtenBy(team);
        this.add(participants);
        this.insert(images);
        this.study(courses);
        team.increase(totalMinutes);
    }

    private void study(List<Course> courses) {
        if (!studies.isEmpty()) {
            studies.clear();
        }
        List<Study> studies = courses.stream()
                .map(course -> new Study(this, course))
                .toList();
        this.studies.addAll(studies);
    }

    private void add(List<User> users) {
        if (!participants.isEmpty()) {
            participants.clear();
        }
        users.forEach(user -> {
            Participates participates = new Participates(user, this);
            user.getParticipates().add(participates);
            this.participants.add(participates);
        });
    }

    private void writtenBy(Team team) {
        this.team = team;
        team.getReports().add(this);
    }

    private void insert(List<String> images) {
        if (images == null) {
            return;
        } else if (!images.isEmpty()) {
            this.images.clear();
        }
        List<Image> paths = images.stream()
                .map(img -> new Image(img, this))
                .toList();
        this.images.addAll(paths);
    }

    public boolean update(ReportForm form, List<User> participants, List<Course> courses) {
        this.title = requireNonNullElse(form.getTitle(), this.title);
        this.content = requireNonNullElse(form.getContent(), this.content);
        this.totalMinutes = requireNonNullElse(form.getTotalMinutes(), this.totalMinutes);

        this.add(participants);
        this.insert(form.getImages());
        this.study(courses);
        team.update(totalMinutes, this.totalMinutes);

        return true;
    }
}
