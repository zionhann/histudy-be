package edu.handong.csee.histudy.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("권한이 없습니다.");
    }
}
