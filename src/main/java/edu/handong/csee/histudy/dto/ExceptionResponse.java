package edu.handong.csee.histudy.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class ExceptionResponse {
    private Integer code;
    private String error;
    private String message;
    private String trace;

    @Builder
    public ExceptionResponse(HttpStatus status, String message, String trace) {
        this.code = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.trace = trace;
    }
}
