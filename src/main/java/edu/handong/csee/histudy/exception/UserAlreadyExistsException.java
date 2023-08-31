package edu.handong.csee.histudy.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("이미 존재하는 사용자입니다.");
    }
}
