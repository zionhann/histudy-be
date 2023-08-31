package edu.handong.csee.histudy.exception;

public class MissingEmailException extends MissingParameterException {
    public MissingEmailException() {
        super("이메일이 존재하지 않습니다.");
    }
}
