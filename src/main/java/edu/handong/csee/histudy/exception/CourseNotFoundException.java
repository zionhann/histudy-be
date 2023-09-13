package edu.handong.csee.histudy.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException() {
        super("해당 강의를 찾을 수 없습니다.");
    }
}
