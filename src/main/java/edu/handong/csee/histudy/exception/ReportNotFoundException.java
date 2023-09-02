package edu.handong.csee.histudy.exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException() {
        super("해당 보고서가 존재하지 않습니다.");
    }
}
