package edu.handong.csee.histudy.dto;

import edu.handong.csee.histudy.domain.Choice;
import edu.handong.csee.histudy.domain.Friendship;
import edu.handong.csee.histudy.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyFormDto {

    private List<UserDto.Basic> friends;
    private List<CourseDto.Info> courses;

    public ApplyFormDto(User entity) {
        this.friends = entity.getSentRequests()
                .stream()
                .map(Friendship::getReceived)
                .map(UserDto.Basic::new)
                .toList();
        this.courses = entity.getChoices()
                .stream()
                .map(Choice::getCourse)
                .map(CourseDto.Info::new)
                .toList();
    }
}
