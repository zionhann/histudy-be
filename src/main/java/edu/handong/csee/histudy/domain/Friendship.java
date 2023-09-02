package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sent;

    @ManyToOne(fetch = FetchType.LAZY)
    private User received;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    public Friendship(User sent, User received) {
        this.sent = sent;
        this.received = received;
        this.status = FriendshipStatus.PENDING;
    }

    public void accept() {
        if (this.status == FriendshipStatus.PENDING) {
            this.status = FriendshipStatus.ACCEPTED;
        }
    }

    public void cancel() {
        if (this.status == FriendshipStatus.ACCEPTED) {
            this.status = FriendshipStatus.PENDING;
        }
    }

    public void disconnect() {
        sent.getFriendships().remove(this);
        if (this.status == FriendshipStatus.ACCEPTED) {
            this.status = FriendshipStatus.PENDING;
            User temp = sent;
            sent = received;
            received = temp;
        } else {
            received.getFriendships().remove(this);
        }
    }

    public boolean isAccepted() {
        return status.equals(FriendshipStatus.ACCEPTED);
    }

    public void connect() {
        sent.getFriendships().add(this);
        received.getFriendships().add(this);
    }

    public StudyGroup makeTeam(AtomicInteger tag) {
        if (sent.getStudyGroup() != null && received.getStudyGroup() != null) {
            assert sent.getStudyGroup().equals(received.getStudyGroup());
            return sent.getStudyGroup();
        } else if (sent.getStudyGroup() != null) {
            // (a <-> b) -> c]
            return sent.getStudyGroup()
                    .join(List.of(received));
        } else if (received.getStudyGroup() != null) {
            // (a <-> b) <- c
            return received.getStudyGroup()
                    .join(List.of(sent));
        }
        return new StudyGroup(tag.getAndIncrement(), List.of(sent, received));
    }

    public User getFriendOf(User u) {
        return (this.sent.equals(u))
                ? this.received
                : this.sent;
    }
}
