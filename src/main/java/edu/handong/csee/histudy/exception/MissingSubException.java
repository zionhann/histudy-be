package edu.handong.csee.histudy.exception;

public class MissingSubException extends MissingParameterException {
    public MissingSubException() {
        super("Sub이 존재하지 않습니다.");
    }
}
