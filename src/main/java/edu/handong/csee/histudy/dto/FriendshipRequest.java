package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.RequestStatus;
import edu.handong.csee.histudy.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class FriendshipRequest {
    private final User user;
    private final RequestStatus status;
}
