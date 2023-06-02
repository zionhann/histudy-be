package edu.handong.csee.histudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamIdDto {

    @Schema(description = "Team ID", example = "1", type = "number")
    private long groupId;
}
