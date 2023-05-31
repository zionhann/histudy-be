package edu.handong.csee.histudy.controller.form;

import lombok.*;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyForm {

    private List<String> friendIds;

    private List<Long> courseIds;
}
