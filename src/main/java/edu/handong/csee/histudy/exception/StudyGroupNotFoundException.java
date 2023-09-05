package edu.handong.csee.histudy.exception;

public class StudyGroupNotFoundException extends RuntimeException {
    public StudyGroupNotFoundException() {
        super("해당하는 스터디 그룹을 찾을 수 없습니다.");
    }
}
