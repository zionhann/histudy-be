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
public class Friendship extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sent;

    @ManyToOne(fetch = FetchType.LAZY)
    private User received;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    public Friendship(User sent, User received, FriendshipStatus status) {
        this.sent = sent;
        this.received = received;
        this.status = status;
    }

    public void accept() {
        if (this.status == FriendshipStatus.PENDING) {
            this.status = FriendshipStatus.ACCEPTED;
        }
    }

    public void unfriend() {
        if (this.status == FriendshipStatus.ACCEPTED) {
            this.status = FriendshipStatus.PENDING;
        }
    }

    public boolean isAccepted() {
        return status.equals(FriendshipStatus.ACCEPTED);
    }

    public boolean isPending() {
        return status.equals(FriendshipStatus.PENDING);
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

    public boolean isSentFrom(User u) {
        return this.sent.equals(u);
    }

    public void removeFromReceivedRequests() {
        this.received.getReceivedRequests().remove(this);
    }
}
