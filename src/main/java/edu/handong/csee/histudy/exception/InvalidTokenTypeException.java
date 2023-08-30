package edu.handong.csee.histudy.exception;

import io.jsonwebtoken.JwtException;

public class InvalidTokenTypeException extends JwtException {

    public InvalidTokenTypeException() {
        super("유효하지 않은 토큰 타입입니다.");
    }
}
