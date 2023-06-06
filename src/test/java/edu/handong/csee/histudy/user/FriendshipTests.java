package edu.handong.csee.histudy.user;

import edu.handong.csee.histudy.domain.Friendship;
import edu.handong.csee.histudy.domain.FriendshipStatus;
import edu.handong.csee.histudy.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("dev")
@Transactional
public class FriendshipTests {

    @DisplayName("내가 친구로 등록(요청)한 상대를 알 수 있다.")
    @Test
    public void FriendshipTests_17() {
        // given
        User userA = User.builder()
                .name("userA")
                .build();
        User userB = User.builder()
                .name("userB")
                .build();

        // when
        userA.add(List.of(userB));
        Friendship friendship = userA.getFriendships()
                .stream()
                .findAny()
                .orElseThrow();

        // then
        assertThat(friendship.getSent()).isEqualTo(userA);
        assertThat(friendship.getReceived()).isEqualTo(userB);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @DisplayName("나를 친구로 등록(요청)한 상대를 알 수 있다.")
    @Test
    public void FriendshipTests_42() {
        // given
        User userA = User.builder()
                .name("userA")
                .build();
        User userB = User.builder()
                .name("userB")
                .build();

        // when
        userA.add(List.of(userB));
        Friendship friendship = userB.getFriendships()
                .stream()
                .findAny()
                .orElseThrow();

        // then
        assertThat(friendship.getSent()).isEqualTo(userA);
        assertThat(friendship.getReceived()).isEqualTo(userB);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @DisplayName("등록(요청)한 상대에 대해 요청상태(대기, 수락, 거절) 등을 알 수 있다: 수락")
    @Test
    public void FriendshipTests_66() {
        // given
        User userA = User.builder()
                .name("userA")
                .build();
        User userB = User.builder()
                .name("userB")
                .build();

        // when
        userA.add(List.of(userB));
        userB.getFriendships()
                .stream()
                .filter(friendship -> friendship.getSent().equals(userA))
                .findAny()
                .ifPresent((Friendship::accept));

        Friendship friendship = userB.getFriendships()
                .stream()
                .findAny()
                .orElseThrow();

        // then
        assertThat(friendship.getSent()).isEqualTo(userA);
        assertThat(friendship.getReceived()).isEqualTo(userB);
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @DisplayName("등록(요청)한 상대에 대해 요청상태(대기, 수락, 거절) 등을 알 수 있다: 거절")
    @Test
    public void FriendshipTests_96() {
        // given
        User userA = User.builder()
                .name("userA")
                .build();
        User userB = User.builder()
                .name("userB")
                .build();

        // when
        userA.add(List.of(userB));
        userB.getFriendships()
                .stream()
                .filter(friendship -> friendship.getSent().equals(userA))
                .findAny()
                .ifPresent(Friendship::disconnect);

        // then
        assertThat(userB.getFriendships()).isEmpty();
        assertThat(userA.getFriendships()).isEmpty();
    }

    @DisplayName("기존에 등록한 강의 정보나 친구가 있는 경우 지우고 업데이트한다")
    @Test
    void FriendshipTests_122() {
        // given
        User userA = User.builder()
                .name("userA")
                .build();
        User userB = User.builder()
                .name("userB")
                .build();
        User userC = User.builder()
                .name("userC")
                .build();

        // when
        userA.add(List.of(userB));
        userA.add(List.of(userC));

        // then
        assertThat(userA.getFriendships()).hasSize(1);
        assertThat(userA.getFriendships().get(0).getReceived()).isEqualTo(userC);
    }
}
