package edu.handong.csee.histudy.exception;

public class FileTransferException extends RuntimeException {
    public FileTransferException() {
        super("이미지를 저장하는데 실패했습니다.");
    }
}
