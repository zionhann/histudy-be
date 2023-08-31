package edu.handong.csee.histudy.exception;

public class MissingTokenException extends MissingParameterException {

    public MissingTokenException() {
        super("토큰이 존재하지 않습니다.");
    }
}
