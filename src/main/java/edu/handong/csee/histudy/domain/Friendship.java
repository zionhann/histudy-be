package edu.handong.csee.histudy.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        }
        else {
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

    public Team makeTeam(AtomicInteger tag) {
        if (sent.getTeam() != null && received.getTeam() != null) {
            assert sent.getTeam().equals(received.getTeam());
            return sent.getTeam();
        } else if (sent.getTeam() != null) {
            // (a <-> b) -> c]
            received.belongTo(sent.getTeam());
            return sent.getTeam();
        } else if (received.getTeam() != null) {
            // (a <-> b) <- c
            sent.belongTo(received.getTeam());
            return received.getTeam();
        }
        Team team = new Team(tag.getAndIncrement());
        sent.belongTo(team);
        received.belongTo(team);
        return team;
    }

    public User getFriendOf(User u) {
        return (this.sent.equals(u))
                ? this.received
                : this.sent;
    }
}
