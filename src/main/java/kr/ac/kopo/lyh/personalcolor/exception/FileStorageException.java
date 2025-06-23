package kr.ac.kopo.lyh.personalcolor.exception;

/**
 * 파일 저장, 로드, 삭제 등의 파일 관련 작업에서 발생하는 예외를 처리하는 커스텀 예외 클래스
 */
public class FileStorageException extends RuntimeException {

    /**
     * 메시지만 포함하는 생성자
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인 예외를 포함하는 생성자
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}