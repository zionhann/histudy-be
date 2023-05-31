package edu.handong.csee.histudy.controller.form;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class TokenForm {
    private final String grantType;
    private final String refreshToken;
}
