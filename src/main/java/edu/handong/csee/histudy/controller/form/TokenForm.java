package edu.handong.csee.histudy.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class TokenForm {

    @Schema(description = "Token Type", example = "refresh_token")
    private final String grantType;

    @Schema(description = "Token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U")
    private final String refreshToken;
}
