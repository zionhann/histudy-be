package edu.handong.csee.histudy.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenForm {

    @Schema(description = "Token Type", example = "refresh_token")
    private String grantType;

    @Schema(description = "Token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U")
    private String refreshToken;
}
