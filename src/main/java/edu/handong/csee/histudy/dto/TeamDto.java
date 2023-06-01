package edu.handong.csee.histudy.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TeamDto {
    private Long group; // id
    private List<UserDto.Info> members;
    private int reports; // report count
    private long times; // totalMinutes
}
